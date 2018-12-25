# -*- coding: utf-8 -*-
import math, string, os
from os.path import isfile
from itertools import izip_longest
import time

from vial import to_unicode
from sqlite3 import *

# hash
import bcrypt
import hashlib
import random
import binascii

# mail
import smtplib
import urllib2


##########################################
#Zagubiony wędrowcze
#Jeśli wszedłeś w ten kod przez pomyłke to z niego wyjdź
#W innym przypadku jeśli coś skopiujesz, 
#a tym skopiowanym kodzie znajdzie się ścieżka do bazy danych,
#to nie zapomnij jej zmienić!
##########################################

def getDBPath(DB):	
	currDir = os.path.dirname(os.path.abspath(__file__))
	return currDir+"/../serwer/"+DB

def isBlocked(IP):	
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()

	timeStamp = time.time()
	minuty = 1
	timeStamp = time.time() - minuty*60
	
	cur.execute("SELECT * FROM blocked WHERE ip LIKE (?) AND date > (?)", (IP,timeStamp))
	logs = cur.fetchall()

	db.commit()
	db.close()
	if(len(logs) > 0):
		return True
	else:
		return False

def blockIP(IP):	
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()

	timeStamp = time.time()
	
	cur.execute("INSERT INTO blocked (ip, date) VALUES (?,?)", (IP,timeStamp))

	db.commit()
	db.close()

def underAttack(login, IP):	
	attacksLimit = 5
	minuty = 5
	attackTime = time.time() - minuty*60
	
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()
	
	query = """SELECT ip FROM logs 
			WHERE login LIKE (?) AND 
			status = 1 AND
			date >= (?)
			ORDER BY date DESC LIMIT (?)"""
	cur.execute(query, (login, attackTime, attacksLimit))
	logs = cur.fetchall()
	db.close()
	if(len(logs) >= attacksLimit):
		return True
	return False	


def checkLogsDB(login):	
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()
	
	cur.execute("SELECT ip FROM logs WHERE login LIKE (?) AND status = 0 ORDER BY date DESC LIMIT 2", (login,))
	logs = cur.fetchall()
	res = ""
	if(len(logs) >= 2 and logs[0] != logs[1]):
		res = logs[1][0]

	db.close()

	return res

def addLog(login, remoteIP, status):
	timeStamp = time.time()

	db = connect(getDBPath("passes.db"))
	cur = db.cursor()
	
	cur.execute("INSERT INTO logs (login, date, ip, status) VALUES (?,?,?,?)", (login, timeStamp, remoteIP, status))
	db.commit()

	db.close()

def sendNewPassword(mail, newPassword):
	newPassword = to_unicode(newPassword)
	mail = urllib2.unquote(mail[0])
	#fake mail
	sender = "bezpieczna1@gmail.com"

	server = smtplib.SMTP("smtp.gmail.com", 587)
	server.starttls()
	server.login(sender, "bezpiecznaStrona1")

	msg = newPassword
	server.sendmail(sender, mail, msg)
	server.quit()

def getNewPassword(login):
	newPassword = ''.join(random.choice("0123456789ABCDEFGHIJKLMNO") for i in range(16))
	
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()
	
	password, salts = hashPassword(login, newPassword)
	cur.execute("UPDATE passes SET password=(?) WHERE login LIKE (?)", (password, login))
	db.commit()
	
	db.close()
	return newPassword
	
def getMail(login):	
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()
	
	cur.execute("SELECT mail FROM passes WHERE login LIKE (?)", (login, ))
	mail = cur.fetchone()
	db.close()
	return mail

def changePasswordDB(login, newPassword):
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()
	
	password, salts = hashPassword(login, newPassword)

	cur.execute("REPLACE INTO passes (login, password, salts) VALUES (?,?,?)", (login, password, salts))
	db.commit()

	db.close()


def savePublicFile(login, fileName, publicFile):
	currDir = os.path.dirname(os.path.abspath(__file__))
	if(fileName == None or login == None):
		return False
	
	dirPath = currDir+"/../serwer/public/"+login
	if(not os.path.exists(dirPath)):
		os.makedirs(dirPath)

	f = open(currDir+"/../serwer/public/"+login+"/"+fileName, "w")
	f.write(publicFile.value)

	db = connect(getDBPath("publicFiles.db"))
	cur = db.cursor()

	cur.execute("INSERT INTO publicFiles (login, file) VALUES (?,?)", (login, fileName))

	db.commit()
	db.close()
	

def getFileExt(fileName):
	return fileName.split(".")[-1]

def removeFile(login, fileName, mode="private"):	
	currDir = os.path.dirname(os.path.abspath(__file__))
	if(login == None):
		return

	if(mode == "private"):
		filePath = currDir+"/../serwer/usr/"+login+"/"+fileName
		os.remove(filePath)
	else:	
		# remove log from database
		db = connect(getDBPath("publicFiles.db"))
		cur = db.cursor()

		cur.execute("DELETE FROM publicFiles WHERE login LIKE (?) AND file LIKE (?)", (login,fileName))
		db.commit()
		db.close()

		# remove File
		filePath = currDir+"/../serwer/public/"+login+"/"+fileName
		os.remove(filePath)

		# remove dir if empty
		fileDir = currDir+"/../serwer/public/"+login+"/"
		if(len(os.listdir(fileDir)) == 0):
			os.rmdir(fileDir)


# Kopia serve_static z vial.py 
# Zwraca cały headers, potrzebne żeby wymusić download prompt
def servePrivateFile(headers, body, data, filepath):
	if headers['request-method'] not in ['GET', 'HEAD']:
		return 'Only GET/HEAD methods allowed for static resources', 405, {}
	filepath = '.' + filepath
	if not isfile(filepath):
		return 'File "%s" does not exist' % filepath, 404, {}
	try:
		f = open(filepath, 'rb')
	except IOError, e:
		return 'File "%s" is not readable' % filepath, 403, {}
	content = to_unicode(f.read())

	return content, 200, headers

def getFileSize(login, fileName):
	currDir = os.path.dirname(os.path.abspath(__file__))
	fileInfo = os.stat(currDir+"/../serwer/usr/"+login+"/"+fileName)
	return fileInfo.st_size

def getFilePath(login, fileName, mode="private"):
	if(mode == "public"):
		return "/../serwer/public/"+login+"/"+fileName
	else:
		return "/../serwer/usr/"+login+"/"+fileName

def getFiles(login=None):
	currDir = os.path.dirname(os.path.abspath(__file__))
	
	if(login != None):
		files = os.listdir(currDir+"/../serwer/usr/"+login)
	else:	
		db = connect(getDBPath("publicFiles.db"))
		cur = db.cursor()

		cur.execute("SELECT * FROM publicFiles")
		files = cur.fetchall()		

		db.close()
	
	return files

def savePrivateFile(login, fileName, privateFile):
	currDir = os.path.dirname(os.path.abspath(__file__))
	if(fileName == None):
		return False
	f = open(currDir+"/../serwer/usr/"+login+"/"+fileName, "w")
	f.write(privateFile.value)

def removeSession(sessionID):	
	db = connect(getDBPath("sessions.db"))
	cur = db.cursor()

	cur.execute("DELETE FROM sessions WHERE sessionID LIKE (?)", (sessionID,))
	db.commit()
	db.close()

def checkCurrSession(sessionID):	
	db = connect(getDBPath("sessions.db"))
	cur = db.cursor()

	cur.execute("SELECT login FROM sessions WHERE sessionID LIKE (?)", (sessionID,))
	login = cur.fetchone()
	if(login is not None):
		login = login[0]
		login = login.encode("utf-8")
	db.close()

	return login

def createSessionID(login):
	#sessionID = bcrypt.hashpw(login, bcrypt.gensalt())
	sessionID = hashlib.pbkdf2_hmac("sha512", bytes(login), bytes(os.urandom(16)), 100)
	sessionID = binascii.hexlify(sessionID)

	db = connect(getDBPath("sessions.db"))
	cur = db.cursor()

	cur.execute("REPLACE INTO sessions (login, sessionID) VALUES  (?,?)", (login,sessionID))	
	db.commit()
	db.close()

	return sessionID

def createUserFolder(login):
	currDir = os.path.dirname(os.path.abspath(__file__))
	os.makedirs(currDir+"/../serwer/usr/"+login)

def checkUser(login, password):	
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()

	cur.execute("SELECT * FROM passes WHERE login LIKE (?)", (login,))

	credentials = cur.fetchone()
	db.close()
	if(credentials == None):
		return False
	
	challengePassword = credentials[1].encode("utf-8")
	password, salts = hashPassword(login, password, mode="decrypt")

	return password == challengePassword
	

def hashPassword(login, password,  mode="crypt"):
	salts = []	
	saltLength = 16
	nSalts = 10
	hashIter = 10

	if(mode == "crypt"):	

		n = nSalts
		for i in range(n):
			#salts.append(bcrypt.gensalt())
			#password = bcrypt.hashpw(password, salts[i])
			salts.append(binascii.hexlify(os.urandom(saltLength)))
			password = hashlib.pbkdf2_hmac("sha512", bytes(password), bytes(salts[-1]), hashIter)
	
		salts = "".join(salts)
	else:
		db = connect(getDBPath("passes.db"))
		cur = db.cursor()

		cur.execute("SELECT salts FROM passes WHERE login LIKE (?)", (login,))
		salts = cur.fetchone()[0]

		n = saltLength
		# sole są przechowywane w DB jako jeden duży string
		# dzieląc ten string co n = długości soli, odczytujemy wszystkie sole
		saltsSplit = []
		
		# 2*n ponieważ binascii.hexlify wydłuża sól dwa razy
		for salt in (salts[i:i+(2*n)] for i in xrange(0, len(salts), 2*n)):
			saltsSplit.append(salt)
		salts = saltsSplit
	
		for salt in salts:	
			#password = bcrypt.hashpw(password, salt)
			password = hashlib.pbkdf2_hmac("sha512", bytes(password), bytes(salt), hashIter)

		db.close()

	password = binascii.hexlify(password)
	return password, salts	

def saveCredentials(login, password, mail):	
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()

	password, salts = hashPassword(login, password)
	cur.execute("INSERT INTO passes (login, password, mail, salts) VALUES  (?,?,?,?)", (login,password, mail, salts))	
	db.commit()
	db.close()

def checkLogin(login):
	db = connect(getDBPath("passes.db"))
	cur = db.cursor()
	cur.execute("SELECT login FROM passes WHERE login LIKE (?)", (login,))
	dbLogin = cur.fetchall()
	db.close()	
	if(len(dbLogin) > 0):
		return False
	else:
		return True

def checkEntropy(password):
	signSets = []
	signSets.append([97, 122]) # małe litery
	signSets.append([65, 90]) #duże litery
	signSets.append([48, 57]) #cyfry 
	specialChars = set(string.punctuation) #dziwne znaki

	N = 0
	specialFlag = False
	signSetsFlag = [False] * 3
	for char in password:
		controlFlag = False
		if(char in specialChars):
			controlFlag = True
			if( not specialFlag ):
				N += len(specialChars)
				specialFlag = True
		for i in range(3):
			if(ord(char) >= signSets[i][0] and ord(char) <= signSets[i][1]):
				controlFlag = True
				if( not signSetsFlag[i] ):
					N += signSets[i][1] - signSets[i][0] + 1
					signSetsFlag[i] = True

		if(controlFlag == False):
			return str(-1)

	entropy = round(len(password)*math.log(N, 2))
	entropy = str(entropy)
	return entropy

if __name__ == "__main__":
	print("sup")
