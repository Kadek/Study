from autobahn.twisted.websocket import WebSocketClientProtocol, WebSocketClientFactory, connectWS

from twisted.internet import reactor
from twisted.internet.defer import Deferred
from twisted.internet.protocol import Protocol
from twisted.internet.ssl import ClientContextFactory
from twisted.web.client import Agent


class ModifiedWebSocketProtocol(WebSocketClientProtocol):

    def __init__(self, protocol_control):
        self.__protocol_control = protocol_control

    def onConnect(self, response):
        print("Server connected: {0}".format(response.peer))

    def onOpen(self):
        print("WebSocket connection open.")

    def onMessage(self, payload, is_binary):
        self.__protocol_control.notify_action(payload)

    def onClose(self, wasClean, code, reason):
        print("WebSocket connection closed: {0}".format(reason))


class ModifiedWebSocketFactory(WebSocketClientFactory):

    def buildProtocol(self, *args, **kwargs):

        protocol = ModifiedWebSocketProtocol(self.protocol_control)
        protocol.factory = self
        return protocol


class WebSocketProtocol:

    def __init__(self, url, bot):

        self.__url = url
        self.__bot = bot

    def do(self, command, parameters):

        curr_url = self.__url + command + "/" + parameters["currency_pair"]
        factory = ModifiedWebSocketFactory(curr_url)
        factory.protocol_control = self

        connectWS(factory)

    def stop(self):

        self.__session.leave()

    def notify_action(self, *args):

        self.__call_action(*args)

    def __call_action(self, *args):

        if not self.__bot.done:
            self.__bot.action(args)

    def __str__(self):

        return "WebSocket"


class RESTProtocol:

    def __init__(self, url, bot):

        self.__url = url
        self.__bot = bot

        class WebClientContextFactory(ClientContextFactory):
            def getContext(self, hostname, port):
                return ClientContextFactory.getContext(self)
        context_factory = WebClientContextFactory()
        self.__agent = Agent(reactor, context_factory)

    def do(self, action, parameters):

        self.__define_action(action)

    def __define_action(self, command):

        url = self.__url+command
        url = url.encode()
        d = self.__agent.request(b'GET', url)
        d.addCallbacks(self.__collect_resource, self.__error)

    def __collect_resource(self, response):

        class ResourceCollector(Protocol):
            def __init__(self, finished_inner):
                self.finished = finished_inner
                self.response = b""

            def dataReceived(self, data):
                self.response += data

            def connectionLost(self, reason):
                self.finished.callback(self.response)

        finished = Deferred()
        finished.addCallbacks(self.__success, self.__error)

        response.deliverBody(ResourceCollector(finished))

    def __error(self, failure):

        print(failure)

    def __success(self, data):

        self.__bot.action(data)

    def __str__(self):

        return "REST"


class ProtocolFactory:

    __protocols = {
        "WebSocket": WebSocketProtocol,
        "REST": RESTProtocol
    }

    def get_protocols(self):

        return self.__protocols.keys()

    def create(self, protocol_name):

        return self.__protocols[protocol_name]