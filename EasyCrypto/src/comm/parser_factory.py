import json
from datetime import datetime

from src.db.db_control import DBControl
from src.db.db_models import load_args, Website, Ticker


class GeminiWebSocketParser:

    def __init__(self, bot):

        self.db_control = DBControl()
        self.bot = bot

    def process(self, data, action, parameters):

        if action == "marketdata":
            parsed = self.__ticker(data, parameters)

            if parsed is not None:
                self.__save_ticker(parsed)

    def __ticker(self, data, parameters):

        data = data[0].decode("utf-8").replace("'", "\"")
        data = json.loads(data)

        attribs = {"name": get_parser_name(self.__str__())}
        website_data = self.db_control.get_objects_of_class(Website, attribs)
        website_id = [website.website_id for website in website_data][0]

        # ignoring unknown data
        try:
            if float(data["events"][0]["price"]) < 1:
                return None
        except Exception:
            return None

        ticker_data = {
            "website_id": website_id,
            "currency_pair": parameters["currency_pair"],
            "last_price": data["events"][0]["price"],
            "open_price": data["events"][0]["price"],
            "lowest_ask": data["events"][0]["price"],
            "highest_bid": data["events"][0]["price"],
            "timestamp": datetime.now()
        }

        ticker = Ticker()
        load_args(ticker, ticker_data)

        return ticker

    def __save_ticker(self, DTO):

        self.db_control.map_object(DTO)
        print("record added")

    def __str__(self):

        return "Gemini WebSocket"


class PoloniexRESTParser:

    def __init__(self, bot):

        self.db_control = DBControl()
        self.bot = bot
        self.__last_change = 0
        self.__previous_tick = {}

    def process(self, data, action, parameters):

        if action == "returnTicker":
            parsed = self.__returnTicker(data, parameters)

            if parsed is not None:
                self.__save_ticker(parsed)

    def __returnTicker(self, data, parameters):

        data = data.decode("utf-8").replace("'", "\"")
        data = json.loads(data)

        if parameters["currency_pair"] in data:

            data = data[parameters["currency_pair"]]
            attribs = {"name": get_parser_name(self.__str__())}
            website_data = self.db_control.get_objects_of_class(Website, attribs)
            website_id = [website.website_id for website in website_data][0]


            if self.__last_change != 0:
                actual_change = float(data["percentChange"]) - self.__last_change
            else:
                actual_change = 0

            self.__last_change = float(data["percentChange"])
            open_price = float(data["last"])*100/(actual_change+100)
            ticker_data = {
                "website_id": website_id,
                "currency_pair": parameters["currency_pair"],
                "last_price": data["last"],
                "open_price": str(open_price),
                "lowest_ask": data["lowestAsk"],
                "highest_bid": data["highestBid"],
                "timestamp": datetime.now()
            }

            ticker = Ticker()
            load_args(ticker, ticker_data)

            prices = ["last_price", "open_price", "highest_bid", "lowest_ask"]
            current_tick = {}
            for price in prices:
                current_tick[price] = ticker_data[price]

            if self.__previous_tick == current_tick:
                return None

            self.__previous_tick = current_tick
            return ticker

        return None

    def __save_ticker(self, DTO):

        self.db_control.map_object(DTO)

    def __str__(self):

        return "Poloniex REST"


class BittrexRESTParser:

    def __init__(self, bot):

        self.db_control = DBControl()
        self.bot = bot

    def process(self, data, action, parameters):

        if action == "public/getcurrencies":

            print(data.decode("utf-8"))

    def __str__(self):

        return "Bittrex REST"


class ParserFactory:

    __parsers = {
        "Poloniex REST":  PoloniexRESTParser,
        "Gemini WebSocket":  GeminiWebSocketParser,
        "Bittrex REST":   BittrexRESTParser
    }

    def get_parsers(self):

        return self.__parsers.keys()

    def create(self, parser_name):

        return self.__parsers[parser_name]


def get_parser_name(full_name):

    return full_name.split(" ")[0]