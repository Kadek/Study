import os
import PySide
import htmlPy
import cv2


# Initial confiurations
BASE_DIR = os.path.abspath(os.path.dirname(__file__))

# GUI initializations
app = htmlPy.AppGUI(title=u"Face", width=500, height=400, plugins=True)

# GUI configurations
app.static_path = os.path.join(BASE_DIR, "static/")
app.template_path = os.path.join(BASE_DIR, "templates/")
app.developer_mode = True

app.window.setWindowIcon(PySide.QtGui.QIcon(BASE_DIR + "/static/img/icon.png"))
# Binding of back-end functionalities with GUI

# Import back-end functionalities
import camera
from menu import Menu
from manager import Manager

# Register back-end functionalities
app.bind(Menu(app))
app.bind(Manager(app))

# Instructions for running application
if __name__ == "__main__":
	# The driver file will have to be imported everywhere in back-end.
	# So, always keep app.start() in if __name__ == "__main__" conditional
	app.template = ("index.html", {})
	camera_stream = camera.Camera(os.path.join(BASE_DIR, "static/img/"), app)
	camera_stream.start()

	app.start()
