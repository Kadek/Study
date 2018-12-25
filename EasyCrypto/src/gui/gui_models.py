from PyQt5.QtCore import QAbstractListModel, Qt


class ModelFactory:

    def __init__(self):

        self.__models = {}
        self.__role_count = Qt.UserRole + 1

    def create_model(self, model_name, data, data_count_repr):

        model = DataModel(data, data_count_repr)

        self.__set_roles(model, data)

        self.__models[model_name] = model
        return model

    def __set_roles(self, model, data):

        for role_name in data:

            formatted_role_name = "{}_role".format(role_name)
            model.set_role_name(formatted_role_name, self.__role_count)
            self.__role_count += 1

            model.insert_to_roles(formatted_role_name, role_name)


class DataModel(QAbstractListModel):
    """ Model used by Qt to display list of websites

    """

    def __init__(self, data, data_count_repr):
        super(DataModel, self).__init__()

        self.__roles = {}
        self.__roles_data = data
        self.__roles_count_repr = data_count_repr

    def set_role_name(self, formatted_role_name, role_index):
        self.__setattr__(formatted_role_name, role_index)

    def insert_to_roles(self, formatted_role_name, name):
        self.__roles[self.__getattribute__(formatted_role_name)] = "{}".format(name).encode(encoding='UTF-8')

    def rowCount(self, parent=None, *args, **kwargs):
        row_count = (len(self.__roles_data[self.__roles_count_repr]))
        return row_count

    def data(self, QModelIndex, role_int=None):
        row = QModelIndex.row()

        role_string = self.__roles[role_int].decode(encoding='UTF-8')
        if role_string in self.__roles_data:
            return self.__roles_data[role_string][row]

    def roleNames(self):
        return self.__roles