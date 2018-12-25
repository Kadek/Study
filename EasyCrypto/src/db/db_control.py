import inspect
import json
import os
from functools import partial

from sqlalchemy import create_engine
from sqlalchemy.exc import ArgumentError, OperationalError
from sqlalchemy.orm import sessionmaker

from src.db import db_models


class DBControl:

    def __init__(self):

        self.__secure_create_engine()
        self.__configure_sessionmaker()
        self.__decorate_session_methods()

    def is_correct_class(self, object_to_map):
        """ Checks if object is an instance of one of the classes in the db_models.py """

        names = inspect.getmembers(db_models, inspect.isclass)
        db_models_classes = tuple(x[1] for x in names)

        for db_models_class in db_models_classes:
            if isinstance(object_to_map, db_models_class):
                return True
        return False

    def load_basic_db_structure(self, clean=False):
        """ Creates the most basic structure of database

        The structure is loaded from db.txt, which holds a JSON

        Parameter clean allows to remove all records from the base
        if a fresh start is needed
        """

        if clean:
            self.__clear_all_tables()

        path = os.path.dirname(os.path.abspath(__file__))
        files = os.listdir(path+"/data")
        for file in files:
            self.__load_object_from_json("{}/data/{}".format(path, file))

    def map_object(self, session, object_to_map):
        """ Maps received object to the database

        Object must be an instance of one of the classes in db_models.py
        """

        session.add(object_to_map)
        session.commit()
        session.query(object_to_map.__class__).all()

    def get_objects_of_class(self, session, class_to_get, attribs={}):
        """ Selects objects from database

        class_to_get - specifies the table
        attribs - results of query are filtered by this dict
        """

        results = session.query(class_to_get)

        results = self.__get_filtered_results(results, class_to_get, attribs)


        return results

    def remove_object(self, session, class_to_remove, attribs):
        """ Removes specific object from database

        class_to_remove - specifies the table
        attribs - records holding attributes given in attribs will be removed
        """

        results = session.query(class_to_remove)
        results = self.__get_filtered_results(results, class_to_remove, attribs)

        for result in results:
            session.delete(result)

        session.commit()

    def is_connected(self):
        """Checks if connection to the db is possible

        Returns truth value whether connection has been created
        """

        conn = self.__engine.connect()
        return conn is not None

    def __decorate_session_methods(self):

        self.map_object = self.__do_in_session(self.__Session)(self.map_object)
        self.get_objects_of_class = self.__do_in_session(self.__Session)(self.get_objects_of_class)
        self.remove_object = self.__do_in_session(self.__Session)(self.remove_object)

    def __do_in_session(self, Session):
        def fun_receiver(fun):
            def ret_fun(*args, **kwargs):
                session = Session()
                res = fun(session, *args, **kwargs)
                session.close()
                return res

            return ret_fun

        return fun_receiver

    def __secure_create_engine(self):

        try:

            self.__try_connection()

        except (ArgumentError, OperationalError):


            self.__create_db()
            self.__create_tables()

            self.__try_connection()

    def __try_connection(self):

        password = self.__load_password()
        self.__engine = create_engine("postgresql://postgres:"+password+"@localhost/easycrypto")
        self.is_connected()

    def __create_db(self):

        password = self.__load_password()
        engine = create_engine("postgresql://postgres:"+password+"@localhost/postgres")
        conn = engine.connect()
        conn.execute("commit")
        conn.execute("create database EasyCrypto")
        conn.close()

    def __create_tables(self):

        db_models.Base.metadata.create_all(self.__engine)

    def __configure_sessionmaker(self):

        self.__Session = sessionmaker(bind=self.__engine)

    def __load_password(self):

        path = os.path.dirname(os.path.abspath(__file__))
        with open("{}/pass.txt".format(path), "r") as f:
            password = f.read()

        return password

    def __load_object_from_json(self, file_name):

        with open(file_name, "r") as f:
            data = json.load(f)

        website = db_models.Website()
        db_models.load_args(website, data["Website"])
        self.map_object(website)

        for action_data in data["Action"]:
            action = db_models.Action()
            db_models.load_args(action, action_data)
            action.website_id = website.website_id
            self.map_object(action)

        if "Parameter" not in data:
            return

        for parameter_data in data["Parameter"]:
            parameter = db_models.Parameter()
            db_models.load_args(parameter, parameter_data)
            self.map_object(parameter)

        for specification_data in data["Specification"]:
            specification = db_models.Specification()

            action_address = specification_data["action_address"]
            action_id = self.get_objects_of_class(db_models.Action, {"address": action_address})[0].action_id

            parameter_name = specification_data["parameter_name"]
            parameter_id = self.get_objects_of_class(db_models.Parameter, {"name": parameter_name})[0].parameter_id

            specification.action_id = action_id
            specification.parameter_id = parameter_id
            self.map_object(specification)

    def __clear_all_tables(self):

        db_models.Base.metadata.drop_all(bind=self.__engine)
        db_models.Base.metadata.create_all(bind=self.__engine)

    def __get_filtered_results(self, results, class_to_get, attribs):

        for attr, value in attribs.items():
            if isinstance(value, list):
                results = results.filter(getattr(class_to_get, attr).in_(value))
            else:
                results = results.filter(getattr(class_to_get, attr) == value)
        results = results.all()
        return results