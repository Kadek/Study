import logging
import os


def create_logger(module_name):

    logger = logging.getLogger(module_name)
    path = os.path.dirname(os.path.abspath(__file__))

    assure_log_dir_exists(path)
    hdlr = logging.FileHandler("{}/../logs/{}.log".format(path, module_name))

    formatter = logging.Formatter("%(asctime)s %(levelname)s %(message)s")
    hdlr.setFormatter(formatter)

    logger.addHandler(hdlr)
    logger.setLevel(logging.INFO)

    return logger


def assure_log_dir_exists(path):

    if not os.path.exists(path + "/../logs/"):
        os.makedirs(path + "/../logs/")