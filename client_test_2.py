import requests
import sys
from utils import ERROR_STRING

def getLoginPage(address, port):
    URL_LOGIN = "http://" + str(address) + ":" + str(port) + "/login/"
    res = requests.get(URL_LOGIN)
    return res.headers, res.text

def postLoginPage(address, port, username, password):
    URL_LOGIN = "http://" + str(address) + ":" + str(port) + "/login/"
    payload = {"username":username,
                "password":password}
    res2 = requests.post(URL_LOGIN, 
                     data=payload)
    return res2.headers, res2.text

def postChatPage(address, port, username, password, message, cookie):
    URL_CHAT = "http://" + str(address) + ":" + str(port) + "/chat/"
    payload = {"message":message}
    ck = {"cookie_id":cookie}

    res3 = requests.post(URL_CHAT, 
                        data=payload, 
                        cookies=ck) 

    return res3.headers, res3.text   

def getChatPage(address, port):
    URL_CHAT = "http://" + str(address) + ":" + str(port) + "/chat/"
    res4 = requests.get(URL_CHAT)

    return res4.headers, res4.text 


if __name__ == '__main__':

    argLength = len(sys.argv)
    allArgs = sys.argv

    print("arguments = ", argLength, allArgs)

    ## contents = filename, address, port, username, password, login_cookie, message
    if argLength == 7:
        address = allArgs[1]
        port = allArgs[2]
        username = allArgs[3]
        password = allArgs[4]
        login_cookie = allArgs[5]
        message = allArgs[6]
        # print("received = "address, port, username, password, login_cookie,  message)


        # test3
        h3,p3 = postChatPage(address, port, username, password, message, login_cookie)    
        print("\n\n test3"+"-"*20)
        print(p3)  

        if p3==ERROR_STRING:
            print("Error encountered")
        else:
            # test4
            h4,p4 = getChatPage(address, port) 
            print("\n\n test4"+"-"*20)
            print(p4)                    

    else :
        print("WRONG CALL TO CLIENT SCRIPT... CHECK ARGUMENTS")


