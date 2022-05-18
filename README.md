# Http-Chat-Server

## Summary
* Main parts of the project: 
	* Login page (which uses the "login/credentials.txt") which:
		* Creates a cookie file which contains the users credentials (which is located in the cookie file)
		* Created cookie file is checked during the session for validity 
	* Chat page where:
		* Post chat messages 
		* Display chat messages from all users (this works by grabbing each message from "/chat/chat.txt", convert the message to a html line, put it into the "/chat/chat.html", then send the file to the user for display).  

## Requirements
* Java 8 
* Python 3

## Execution/Testing
1. Start HTTPChatServer.java with the terminal command:
	* java [port number]
2. Access server using python file, using either command:
	* python3 "client_test_1-1.py" {address} {port} {username} {password} {message}
	* python3 "client_test_2.py"   {address} {port} {username} {password} {login cookie}{message}
	> The "login cookie" is the cookie file name itself. Resort to the /cookie/ file if confused	
