# -*- coding: utf-8 -*-
from vial import Vial, render_template, serve_static
from bezpieczne_narzedzia import *

def checkLogs(headers, body, data):
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)

	if(login != None):
		return checkLogsDB(login), 200, {}

	return "", 200, {}

def changeSuccesful(headers, body, data, oldPassword, newPassword):
	oldPassword = data["oldPassword"].value
	newPassword = data["newPassword"].value
	
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)

	if(login != None):
		if(checkUser(login, oldPassword)):
			changePasswordDB(login, newPassword)
			return render_template("templates/changeSuccesful.html"), 200, {}
		else:
			return render_template("templates/hackerman.html"), 200, {}

	return render_template("templates/index.html"), 200, {}

def checkPassword(headers, body, data):
	oldPassword = data["oldPassword"]	
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)

	if(login != None):
		if(checkUser(login, oldPassword)):
			return "good", 200, {}
		else:
			return "bad", 200, {}

	return render_template("templates/index.html"), 200, {}


def changePassword(headers, body, data):
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)

	if(login != None):
		return render_template("templates/changePassword.html"), 200, {}

	return render_template("templates/index.html"), 200, {}

def removePublicFile(headers, body, data):	
	fileName = data["fileName"].value

	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)

	if(login != None):
		removeFile(login, fileName, "public")
		return "", 200, {}

	return render_template("templates/index.html"), 200, {}

def uploadPublicFile(headers, body, data):
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)
		savePublicFile(login, data["publicFile"].filename, data["publicFile"])

	files = getFiles()
	if(login != None):
		return render_template("templates/publicFiles.html", files=files, login=login), 200, {}

	return render_template("templates/publicFiles.html", files=files), 200, {}

def showPublicFile(headers, body, data, login, fileName):
	filePath = getFilePath(login, fileName, "public")	
	fileBody, code, fileHeader = serve_static(headers, body, data, filePath)
	fileExt = getFileExt(fileName)
	return render_template("templates/publicCode.html", fileBody=fileBody, brush=fileExt), 200, {}

def publicFiles(headers, body, data):
	files = getFiles()

	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)

	if(login != None):
		return render_template("templates/publicFiles.html", files=files, login=login), 200, {}	

	return render_template("templates/publicFiles.html", files=files), 200, {}	

def removePrivateFile(headers, body, data):
	fileName = data["fileName"].value
	
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)

	if(login != None):
		removeFile(login, fileName)
		return "", 200, {}

	return render_template("templates/index.html"), 200, {}

def downloadPrivateFile(headers, body, data, fileName):
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)

	if(login != None):
		headers = {
		"request-method": "GET",
		"Content-Description": "File Transfer",
		"Content-Type": "application/octet-stream",
		"Content-Disposition": "attachment, filename="+fileName,
		"Content-Transfer-Encoding": "binary",
		"Expires": "0",
		"Cache-Control": "must-revalidate",
		"Pragma": "public",
		"Content-Length": str(getFileSize(login, fileName)).encode("utf-8")}

		filePath = getFilePath(login, fileName)
		return servePrivateFile(headers, body, data, filePath)

	return render_template("templates/index.html"), 200, {}

def uploadPrivateFile(headers, body, data):
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)
		savePrivateFile(login, data["privateFile"].filename, data["privateFile"])

	if(login != None):
		files = getFiles(login)
		return render_template("templates/user.html", login=login, files=files), 200, {}

	return render_template("templates/index.html"), 200, {}

def index(headers, body, data):
	login = None
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		login = checkCurrSession(sessionID)
	
	if(login != None):	
		files = getFiles(login)
		return render_template("templates/user.html", login=login, files=files), 200, {}

	return render_template("templates/index.html"), 200, {}

def login(headers, body, data):
	login = data["login"].value
	password = data["password"].value

	login = login.lower()
	IP = headers['remote-addr']
	blockedPage = "Adres : " + IP + " jest zablokowany z powodu zbyt wielu błędnych prób logowania."
	if(isBlocked(IP)):
		return to_unicode(blockedPage), 200, {}

	if(checkUser(login, password)):
		sessionID = createSessionID(login)
		cookie = {"Set-Cookie": sessionID+"; Max-Age=300"}
		files = getFiles(login)
	
		addLog(login, IP, status=0)
		return render_template("templates/user.html", login=login, files=files), 200, cookie

	addLog(login, IP, status=1)
	if(underAttack(login, IP)):
		blockIP(IP)	
		return to_unicode(blockedPage), 200, {}

	return render_template("templates/index.html"), 200, {}

def logout(headers, body, data):
	if(headers["http-cookie"] is not None):	
		sessionID = headers["http-cookie"].split(" ")[-1]
		removeSession(sessionID)

	return render_template("templates/index.html"), 200, {}

def register(headers, body, data):
	return render_template("templates/register.html"), 200, {}

def recover(headers, body, data):
	return render_template("templates/recover.html"), 200, {}

def recoverSend(headers, body, data):
	if("login" in data):
		login = data["login"].value
	else:
		return render_template("templates/recover.html"), 200, {}

	mail = getMail(login)
	newPassword = getNewPassword(login)
	sendNewPassword(mail, newPassword)
	return render_template("templates/index.html"), 200, {}

def checkStrength(headers, body, data):
	password = data["pass"].value
	return checkEntropy(password), 200, {}

def checkCredentials(headers, body, data):
	login = data["login"].value
	password = data["password"].value

	login = login.lower()
	if(checkEntropy(password) < 0):
		return "bad", 200, {}

	if( not checkLogin(login)):
		return "bad", 200, {}

	return "good", 200, {}

def registerSuccesful(headers, body, data):
	login = data["login"].value
	password = data["password"].value

	if("mail" in data):
		mail = data["mail"].value
	else:
		mail = ""

	login = login.lower()
	if( not checkLogin(login)):
		return render_template("templates/hackerman.html"), 200, {}

	saveCredentials(login, password, mail)
	createUserFolder(login)

	return render_template("templates/registerSuccesful.html"), 200, {}

routes = {
	# rejestracja/sesja/login
	"/": index,
	"/login": login,
	"/logout": logout,
	"/register": register,
	"/recover": recover,
	"/recoverSend": recoverSend,
	"/checkStrength": checkStrength,
	"/checkCredentials": checkCredentials,
	"/registerSuccesful": registerSuccesful,

	# prywatne pliki
	"/uploadPrivateFile": uploadPrivateFile,
	"/downloadPrivateFile?file={fileName}": downloadPrivateFile,
	"/removePrivateFile": removePrivateFile,

	# publiczne pliki/snippety
	"/publicFiles": publicFiles,
	"/showPublicFile?login={login}&file={fileName}": showPublicFile,
	"/uploadPublicFile": uploadPublicFile,
	"/removePublicFile": removePublicFile,

	# odzyskiwanie konta
	"/changePassword": changePassword,
	"/changeSuccesful": changeSuccesful,
	"/checkPassword": checkPassword,

	"/checkLogs": checkLogs,
}

app = Vial(routes, prefix="", static="/static").wsgi_app()
