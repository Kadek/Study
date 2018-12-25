import sys, inspect, os

from PyQt5.QtCore import QObject, QUrl
from PyQt5.QtGui import QGuiApplication
from PyQt5.QtQml import QQmlApplicationEngine

from src.gui.gui_models import ModelFactory
from src.gui.gui_qml_control import IndexChangedSlot, InputChangedSlot
from src.db import db_control, db_models
from src.visual import visual_control

from utilities import logger


class GUIControl:

    def __init__(self):

        self.__models = {}
        self.__model_factory = ModelFactory()
        self.__create_logger()
        self.__create_db_control()
        self.__create_visual_control()

    def setup(self):

        self.__create_app()

        # install twisted reactor before importing twisted code
        self.__install_twisted()
        self.__create_comm_control()

        self.__refresh_event_website()

    def show(self):

        sys.exit(self.__app.exec_())

    def notify_website_clicked(self):

        try:
            self.__refresh_event_action()
        except AttributeError:
            self.__logger.error("The GUI module has not been properly initialised")

    def notify_action_clicked(self):

        try:
            self.__refresh_event_parameter()
        except:
            self.__logger.error("The GUI module has not been properly initialised")

    def notify_parameter_changed(self, index, msg):

        self.__update_filled_parameters(index, msg)

    def __create_logger(self):

        self.__logger = logger.create_logger("module_gui")

    def __create_db_control(self):

        self.__db_control = db_control.DBControl()

    def __create_visual_control(self):

        self.__visual_control = visual_control.VisualControl()

    def __create_comm_control(self):

        from src.comm import comm_control
        self.__comm_control = comm_control.CommControl()

    def __install_twisted(self):

        import qt5reactor
        qt5reactor.install()

    def __create_app(self):

        self.__app = QGuiApplication(sys.argv)

        self.__engine = QQmlApplicationEngine()

        self.__attach_slots()

        curr_dir = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
        self.__engine.load(QUrl.fromLocalFile(curr_dir+"\main.qml"))

        self.__attach_events()

    def __attach_slots(self):
        root_context = self.__engine.rootContext()

        self.__website_slot = IndexChangedSlot(callback=self.notify_website_clicked)
        root_context.setContextProperty("websiteSlot", self.__website_slot)

        self.__action_slot = IndexChangedSlot(callback=self.notify_action_clicked)
        root_context.setContextProperty("actionSlot", self.__action_slot)

        self.__parameter_slot = InputChangedSlot(callback=self.notify_parameter_changed)
        root_context.setContextProperty("parameterSlot", self.__parameter_slot)

    def __update_buttons_state(self, states):

        translator = {
            "executeButton": "executeMouseArea",
            "refreshButton": "refreshMouseArea",
            "quitButton": "quitMouseArea"
        }

        for state in states:

            state_name = translator[state]
            button = self.__engine.rootObjects()[0].findChild(QObject, state_name)
            button.setProperty("enabled", states[state])

    def __attach_events(self):

        self.__logger.info("Attaching slots to signals")
        button = self.__engine.rootObjects()[0].findChild(QObject, "refreshMouseArea")
        button.refreshEvent.connect(self.__refresh_event_logs)

        button = self.__engine.rootObjects()[0].findChild(QObject, "executeMouseArea")
        button.executeEvent.connect(self.__execute_event)

        self.__logger.info("Attaching slots to signals successful")

    def __add_models_to_context(self):

        self.__logger.info("Adding models to QML context")
        root_context = self.__engine.rootContext()

        for model_name in list(self.__models):
            root_context.setContextProperty(model_name, self.__models[model_name])

        self.__logger.info("Adding models to QML context successful")

    def __refresh_event_website(self):

        self.__load_website()
        self.__add_models_to_context()
        self.__update_buttons_state({"executeButton": 0, "refreshButton": 1, "quitButton": 1})

    def __refresh_event_action(self):

        self.__load_action()
        self.__add_models_to_context()
        self.__update_buttons_state({"executeButton": 1, "refreshButton": 1, "quitButton": 1})

    def __refresh_event_parameter(self):

        self.__load_parameters()
        self.__add_models_to_context()

    def __refresh_event_logs(self):

        print("Odświeżam logi")

    def __execute_event(self):

        selected_data = self.__gather_selected_data()
        command = self.__create_command(selected_data)

        executor = self.__comm_control
        if "Visualization" in command["name"]:
            executor = self.__visual_control

        executor.use_command(command)

    def __gather_selected_data(self):

        selected_data = {}

        selected_data["current_website"] = self.__website_data[self.__website_slot.index]
        selected_data["current_action"] = self.__action_data[self.__action_slot.index]

        names_list = [parameter.name for parameter in self.__parameter_data]
        input_list = [msg for index, msg in self.__filled_parameter.items()]
        selected_data["filled_parameters"] = zip(names_list, input_list)

        return selected_data

    def __create_command(self, selected_data):

        command = {}

        command["name"] = selected_data["current_website"].name
        command["protocol"] = selected_data["current_website"].protocol
        command["address"] = selected_data["current_website"].address
        command["action"] = selected_data["current_action"].address
        command["parameters"] = dict(selected_data["filled_parameters"])

        return command

    def __load_website(self):

        self.__website_data = self.__db_control.get_objects_of_class(db_models.Website)
        website_names = [website.name for website in self.__website_data]
        website_protocols = [website.protocol for website in self.__website_data]

        website_proper_names = zip(website_names, website_protocols)
        website_proper_names = list(x + " " + y for x, y in website_proper_names)

        website_model = self.__model_factory.create_model("WebsiteModel", {"name": website_proper_names}, "name")
        self.__add_to_models({"websiteModel": website_model})

    def __load_action(self):

        selected_website = self.__website_data[self.__website_slot.index]
        attribs = {"website_id": selected_website.website_id}

        self.__action_data = self.__db_control.get_objects_of_class(db_models.Action, attribs)
        action_addresses = [action.address for action in self.__action_data]

        action_model = self.__model_factory.create_model("ActionModel", {"address": action_addresses}, "address")
        self.__add_to_models({"actionModel": action_model})

    def __load_parameters(self):

        selected_action = self.__action_data[self.__action_slot.index]
        attribs = {"action_id": selected_action.action_id}

        specification_data = self.__db_control.get_objects_of_class(db_models.Specification, attribs)
        parameter_id = [specification.parameter_id for specification in specification_data]
        attribs = {"parameter_id": parameter_id}

        self.__parameter_data = self.__db_control.get_objects_of_class(db_models.Parameter, attribs)
        self.__filled_parameter = {index: "" for index in range(len(self.__parameter_data))}
        parameter_name = [parameter.name for parameter in self.__parameter_data]
        parameter_type = [parameter.type for parameter in self.__parameter_data]

        parameter_dict = {
            "name": parameter_name,
            "type": parameter_type
        }

        parameter_model = self.__model_factory.create_model("ParameterModel", parameter_dict, "name")
        self.__add_to_models({"parameterModel": parameter_model})

    def __update_filled_parameters(self, index, msg):

        self.__filled_parameter[index] = msg

    def __add_to_models(self, new_models):

        for model_name in new_models:
            self.__models[model_name] = new_models[model_name]