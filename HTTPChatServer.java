/***Imports***/
//Teachers/website that Teacher recommended
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//Ours
import java.util.Scanner;
import java.util.*;

public class HTTPChatServer {
    /**Server Starter**/
    public static void main (String[] args) throws Exception {
        //Catches no input
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        /*create server socket given port number*/
        //portNumber := Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    handleClient(client);
                }
            }
        }
    }

    /**Client**/
    private static void handleClient(Socket client) throws IOException {	
        /*Creates a string builder which catches user input*/
    	//Given input: address port username password message
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

        //Filling String Builder
        StringBuilder requestBuilder = new StringBuilder();
		while (br.ready() == true) {
			String line = "";
			while (br.ready() == true)
				line += (char)(br.read());
			requestBuilder.append(line + "\r\n");
		}

		String request = requestBuilder.toString();
		System.out.printf("The request is: \n%s\n", request);

		//Input Value to command
        String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        String method  = requestLine[0];
        String path    = requestLine[1];
        String version = requestLine[2];
        String host    = requestsLines[1].split(" ")[1];

        // build the reponse here
        List<String> headers = new ArrayList<>();
        for (int h = 2; h < requestsLines.length; h++) {
            String header = requestsLines[h];
            headers.add(header);
        }

		/*Log*/
        String accessLog = String.format("Client %s \nmethod %s \npath %s \nversion %s \nhost %s \nheaders %s\n"
			 , client.toString(), method, path, version, host, headers.toString());
        System.out.println(accessLog);

		/*Searching for Files*/
       	Path filePath = getFilePath(path);
        if (Files.exists(filePath)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String contentType = "contentType: ";

			if (method.indexOf("GET") != -1) {
			if ((path + "login.html/").indexOf("/login/login.html") != -1) {
				/**Test 1**/
				filePath = getFilePath(path + "login.html/");
				contentType += guessContentType(filePath);
				baos.write(Files.readAllBytes(filePath));

			} else {
				/**Test 4**/
				baos.write(loadChatMessages().getBytes());
			}
			} else {
			if ((path + "login.html/").indexOf("/login/login.html/") != -1) {
				/**Test 2**/
				//Handling the Username + Password
				// A) The credentials is the last entry of the headers no matter what
				// B) The string manipulation is so we can avoid a user who has:
				//    username = "username=" and "&password="
				String s = (headers.get(headers.size() - 1)).toString();
				if ((s = correctCredentials(s.substring("username=".length(), s.length()))) != null) {
					//Correct credentials. Create cookie, and return it
					contentType = ("Set-Cookie:" + createCookie(s));
					filePath = getFilePath("/chat/chat.html/");
				} else {
				//Incorrect credentials, don't give cookie
					filePath = getFilePath("/login/error.html/");
					contentType += guessContentType(filePath);
				}
				baos.write(Files.readAllBytes(filePath));
			} else {
				/**Test 3**/
				String username = "";
				if ((username = checkCookie(headers.toString())) != null) {
					//Would've loaded messages
					//baos.write(loadChatMessages().getBytes());
					filePath = getFilePath(path + "chat.html/");
					baos.write(Files.readAllBytes(filePath));
					writeChatMessage(username, (headers.get(headers.size() - 1)).toString());
				}
				else {
					filePath = getFilePath("/login/error.html/");
					contentType += guessContentType(filePath);
					baos.write(Files.readAllBytes(filePath));
				}
			}
	    }
	    
            //Send content
	    byte [] constant = baos.toByteArray();
	    sendResponse(client, "200 OK", contentType, constant);
        } else {
            // 404
            byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
            sendResponse(client, "404 Not Found", "text/html", notFoundContent);
        }	
    }

    /**Cookies**/
    private static String checkCookie (String cookieID) {
		//Cookie are stored in /cookie/.
		//We get the directory so we can look at all the files.
		File directory = new File(System.getProperty("user.dir") + "/cookie/");
		File filesList[] = directory.listFiles();
		
		//With the directory we check if one of the stored cookie file exists
		for (File file : filesList) {
			if (cookieID.indexOf("Cookie: cookie_id=" + file.getName()) != -1) {
				try {
					return new String (Files.readAllBytes(getFilePath("/cookie/" + file.getName())));
				} catch (IOException e) {
					System.out.println("[DEBUG] error in checkCookie()");
						System.out.println("[DEBUG] File couldn't be read");
						e.printStackTrace();
					System.exit(1);
				}
			}
		}
		return null;
    }

    private static int createCookie (String s) {
		//1) Cookies given random value 
		//2) Cookie checked to make sure no duplicate value has been created
		int cookie = (int)(Math.random() * 99999999 + 11111111);
		boolean existingCookie = true;
		while (existingCookie == true) {
			existingCookie = false;
			File directory = new File(System.getProperty("user.dir") + "/cookie/");
			File filesList[] = directory.listFiles();
			for (File file : filesList) {
				if (file.getName().indexOf(Integer.toString(cookie)) != -1) {
				cookie = (int)(Math.random() * 99999999 + 11111111);
				existingCookie = true;
				break;
				}
			}
		}


		//Cookie file is:
		//  1) created
		//  2) stores the users username 
		//     (that way we can use the username when storing messages
		try {	
			String fileName = System.getProperty("user.dir") + "/cookie/" + cookie;
			File cookieFile = new File(fileName);
			cookieFile.createNewFile();
			
			FileWriter myWriter = new FileWriter(fileName);
			myWriter.write(s);
			myWriter.close();
		} catch (Exception e) {
			System.out.println("[DEBUG] Error in createCookie()");
			System.out.println("[DEBUG] File couldn't be written into or created");
			e.printStackTrace();
			System.exit(1);
		}
		
		return cookie;
		}

		/**Messages**/
		private static void writeChatMessage (String username, String message) {
		//We retrieve the chat.txt file and append to it the concatentation
		//of the users username and their message. 
		try { 
			Writer write;
			write = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/chat/chat.txt", true));
			write.append(username + ": " + message.substring("message=".length(), message.length()) + "\n");
			write.close();
		} catch (Exception e) {
			System.out.println("[DEBUG] Error in writeChatMessage()");
			e.printStackTrace();
			System.exit(1);
		}
    }
    private static String loadChatMessages () {
		//How this method works is by:
		//  1) Retrieve the messages from chat.txt
		//  2) Take the message and convert it into a html line of code
		//  3) Then place the messages into a string buffer	
		//  4) Then place the contents of the chat.html files and place it
		//     into a string buffer of its own
		//  5) Return the buffer that contains the chat.html, but after the 
		//     message: "<p> Chat Space : </p>" -- has been replaced 
		//     with the content from the buffer containg the chat messages
		
		String messages = "<p> Chat Space : </p>\n\t";
		String content = "";
		try {
				File file = new File(System.getProperty("user.dir") + "/chat/chat.txt");
				Scanner myReader = new Scanner(file);
			while (myReader.hasNextLine())
			messages += ("<p>" + myReader.nextLine() + "</p>" + "\n\t");
			myReader.close();

			Path path = getFilePath("/chat/chat.html");
			content = new String (Files.readAllBytes(path));
		 } catch (Exception e) {
			 System.out.println("[DEBUG] Error in loadChatMessages()");
			 e.printStackTrace();
			 System.exit(1);
		 }	

		return content.replace("<p> Chat Space : </p>", messages);
    }

    /**Login**/    
    private static String correctCredentials (String s) {
		//Grabbing the credentials from the credentials.txt file and placing them
		//into a double array for easy searching in the username&password payload.
		ArrayList <ArrayList <String>> usernamepassword = new ArrayList<ArrayList <String>>();
		try {
			File file = new File(System.getProperty("user.dir") + "/login/credentials.txt");
			Scanner myReader = new Scanner(file);
				for (int i = 0; myReader.hasNextLine(); i++) {
					String [] temp = myReader.nextLine().split(",");
					usernamepassword.add(new ArrayList<String>(Arrays.asList(temp)));
				}
			myReader.close();
		} catch (Exception e) {
			System.out.println("[DEBUG] Error in correctCredentials()");
			System.out.println("[DEBUG] Couldn't read list");
			e.printStackTrace();
			System.exit(1);

		}

		//We search through 2D string and see if the payload contains
		//the message and a pasword.
		//How this is done is by:
		//  1) Removeing "username=" before giving it to the array,
		//     that way there isn't a false positive when checking
		//     credentials.
		//  2) Check the message for the username and password
		for (int i = 0; i < usernamepassword.size(); i++) {
			try {
				if (s.indexOf(usernamepassword.get(i).get(0)) != -1) {
					String temp =
					s.substring((usernamepassword.get(i).get(0)).length() + "&password=".length(), s.length());

					if (temp.indexOf(usernamepassword.get(i).get(1)) != -1)
						return usernamepassword.get(i).get(0);
				}
			} catch (StringIndexOutOfBoundsException e) {
			//Here to catch false positives destroying the string
			}
		}
		return null;
    }

    /**Teachers/Websites Methods**/
    private static void sendResponse(Socket client, String status, String contentType, byte[] content) throws IOException {
		OutputStream clientOutput = client.getOutputStream();
			clientOutput.write(("HTTP/1.1 200 OK" + status + "\r\n").getBytes());
			clientOutput.write((contentType + "\r\n").getBytes()); 	             //h# value
		clientOutput.write("\r\n".getBytes());
			clientOutput.write(content); 				             //p# value
			//clientOutput.write("\r\n\r\n".getBytes());

		clientOutput.flush();
			client.close();
    }

    private static Path getFilePath(String path) {
        if ("/".equals(path))
            path = "/index.html";
        return Paths.get("./", path);
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
}
