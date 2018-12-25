from src.gui import gui_control
from src.db import db_control

if __name__ == "__main__":

    gui = gui_control.GUIControl()

    ## uncomment to restart database AND CLEAN IT
    #db = db_control.DBControl()
    #db.load_basic_db_structure(clean=True)

    gui.setup()
    gui.show()
