import os

import htmlPy

import camera

class Manager(htmlPy.Object):

	def __init__(self, app):
		super(Manager, self).__init__()
		self.app = app

	@htmlPy.Slot(str)
	def active_file(self, file_id):

		code = "$('li').removeClass('active')"
		self.app.evaluate_javascript(code)

		code = "$('#"+file_id+"').addClass('active')"
		self.app.evaluate_javascript(code)

		self.app.evaluate_javascript("$('#note_header').text($('#"+file_id+"').text())")
		self.app.evaluate_javascript("Manager.update_paragraph($('#note_header').text(), $('body').attr('id'))")

	@htmlPy.Slot(str, str)
	def update_paragraph(self, file_name, user):

		try:
			with open("users/" + user + "/" + file_name, "r") as file:
				text = file.read()
		except (PermissionError, FileNotFoundError):
			text = ""

		self.app.evaluate_javascript("$('#note_text').val('"+text+"')")

	@htmlPy.Slot()
	def logout(self):

		self.app.height = 400
		self.app.width = 500
		self.app.title = "Face"
		self.app.template = ("index.html", {})
		BASE_DIR = os.path.abspath(os.path.dirname(__file__))
		camera_stream = camera.Camera(os.path.join(BASE_DIR, "static/img/"), self.app)
		camera_stream.start()

	@htmlPy.Slot()
	def save(self):

		code = """
Manager.real_save(
	$('body').attr('id'),
	$('#note_header').text(),
	$('#note_text').val()
)"""
		self.app.evaluate_javascript(code);

	@htmlPy.Slot(str, str, str)
	def real_save(self, user, file_name, text):

		with open("users/"+user+"/"+file_name, "w") as file:
			file.write(text)

	@htmlPy.Slot()
	def remove(self):

		code = """
Manager.real_remove(
	$('body').attr('id'),
	$('#note_header').text(),
	$('.active').attr('id')
)"""
		self.app.evaluate_javascript(code);

	@htmlPy.Slot(str, str, str)
	def real_remove(self, user, file_name, file_id):

		os.remove("users/"+user+"/"+file_name)

		code = "$('#"+file_id+"').remove()"
		self.app.evaluate_javascript(code)		

		code = "Manager.active_file($('li:first').attr('id'))"
		self.app.evaluate_javascript(code)

	@htmlPy.Slot()
	def add(self):

		code = """
Manager.real_add(
	$('body').attr('id'),
	$('#new_note').val(),
	$('#notes').children().last().attr("id")
)"""
		self.app.evaluate_javascript(code);

	@htmlPy.Slot(str, str, str)
	def real_add(self, user, file_name, last_file_id):

		if(len(last_file_id) > 2):
			file_id = int(last_file_id[0:-2])+1
		else:
			file_id = 1
		file_name = file_name + ".txt"
		file = open("users/"+user+"/"+file_name, "w")
		file.close()

		code = """
var el = document.createElement('li');
el.id = '"""+ str(file_id) +"""el';
el.onclick = function() {
	Manager.active_file($(this).attr('id'));
};

var el_a = document.createElement('a');
el_a.href = '#';

var node = document.createTextNode('"""+ file_name + """');
el_a.appendChild(node);
el.appendChild(el_a);
el.classList.add("list-group-item");

var element = document.getElementById('notes');
element.appendChild(el);"""
		self.app.evaluate_javascript(code);

def load_notes(app, user):

	data = load_file_names(user)
	show_file_names(app, data)
	activate_first_file_name(app)

def load_file_names(user):

	data = os.listdir("users/"+user)
	return data

def show_file_names(app, data):

	count = 1
	for file_name in data:

		code = """
var el = document.createElement('li');
el.id = '"""+ str(count) +"""el';
el.onclick = function() {
	Manager.active_file($(this).attr('id'));
};

var el_a = document.createElement('a');
el_a.href = '#';

var node = document.createTextNode('"""+ file_name + """');
el_a.appendChild(node);
el.appendChild(el_a);
el.classList.add("list-group-item");

var element = document.getElementById('notes');
element.appendChild(el);"""

		app.evaluate_javascript(code)
		count += 1

def activate_first_file_name(app):

	app.evaluate_javascript("alert('Logowanie udane!')")

	code = "Manager.active_file($('li:first').attr('id'))"
	app.evaluate_javascript(code)