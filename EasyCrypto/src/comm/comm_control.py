from twisted.internet import reactor

from src.comm.bot import Bot
from src.comm.parser_factory import ParserFactory
from src.comm.protocol_factory import ProtocolFactory


class CommControl:

    def __init__(self):

        self.__protocol_factory = ProtocolFactory()
        self.__parser_factory = ParserFactory()
        self.__setup_reactor()

    def use_command(self, command):

        exchange_bot = self.__create_exchange_bot(command)
        exchange_bot.run()

    def __setup_reactor(self):

        # ought to be triggered when shutting down
        def stop():
            reactor.stop()

        reactor.runReturn()
        reactor.addSystemEventTrigger('before', 'shutdown', stop)

    def __create_exchange_bot(self, specification):

        protocol_class = self.__get_protocol_class(specification["protocol"])
        parser_class = self.__get_parser_class(self.__get_parser_name(specification))

        exchange_bot = Bot(
            protocol_class,
            parser_class,
            specification["address"],
            specification["action"],
            specification["parameters"]
        )

        return exchange_bot

    def __get_protocol_class(self, protocol_name):

        if protocol_name in self.__protocol_factory.get_protocols():
            return self.__protocol_factory.create(protocol_name)
        else:
            raise AttributeError("No such protocol")

    def __get_parser_class(self, parser_name):

        if parser_name in self.__parser_factory.get_parsers():
            return self.__parser_factory.create(parser_name)
        else:
            raise AttributeError("No such parser")

    def __get_parser_name(self, specification):

        return specification["name"] + " " + specification["protocol"]