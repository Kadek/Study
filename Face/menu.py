import json
import os

import htmlPy
import camera

from manager import load_notes

class Menu(htmlPy.Object):

	def __init__(self, app):
		super(Menu, self).__init__()
		self.app = app
		self.action = ""

	@htmlPy.Slot()
	def register(self):
		self.action = "register"
		self.app.evaluate_javascript("$('#menu_form').submit()")

	@htmlPy.Slot()
	def login(self):
		self.action = "login"
		self.app.evaluate_javascript("$('#menu_form').submit()")

	@htmlPy.Slot(str)
	def process(self, args):

		user = json.loads(args)["user"]
		if(len(user) == 0):
			self.app.evaluate_javascript("alert('Podaj login.')")
		else:
			camera_instance = camera.Camera("", None)

			if self.action == "register":
				if(camera_instance.create_user_profile(user)):
					self.app.evaluate_javascript("alert('Użytkownik zarejestrowany!')")
					os.makedirs("users/"+user)
				else:
					self.app.evaluate_javascript("alert('Rejestracja nie powiodła się!')")
			elif self.action == "login":
				if(camera_instance.verify_user_profile(user)):
					self.app.template = ("manager.html", {"user":user})
					self.app.height = 500
					self.app.width = 1000
					self.app.title = "Face - manager"
					load_notes(self.app, user)
				else:
					self.app.evaluate_javascript("alert('Logowanie nie powiodło się!')")

		self.action = ""

	@htmlPy.Slot()
	def debug(self):

		if("debug" in self.app.title):
			self.app.title = "Face"
		else:
			self.app.title = "Face - debug"

