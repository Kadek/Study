import datetime

import matplotlib.pyplot as plt
from matplotlib.finance import candlestick2_ohlc
import matplotlib.ticker as ticker
import pandas as pd

from src.db.db_control import DBControl
from src.db.db_models import Ticker


class VisualControl:

    def __init__(self):

        self.__db_control = DBControl()
        self.__actions = {
            "show ticker": self.__show_ticker,
            "show ticker candlesticks": self.__show_ticker_candlesticks
        }

    def use_command(self, command):

        if command["action"] in self.__actions:
            self.__actions[command["action"]](command)
        else:
            print("nope")

    def __show_ticker(self, command):


        currency_pair = command["parameters"]["currency_pair"]

        ticker_data = self.__db_control.get_objects_of_class(Ticker, command["parameters"])
        ticker_last_price = [float(ticker.last_price) for ticker in ticker_data]
        ticker_timestamp = [ticker.timestamp for ticker in ticker_data]

        data = {currency_pair: ticker_last_price, "timestamp": ticker_timestamp}
        ticker_data = pd.DataFrame(data=data)

        fig = plt.figure()
        ax = plt.subplot2grid((1, 1), (0, 0))

        fig.autofmt_xdate()
        fig.tight_layout()

        plt.plot("timestamp", currency_pair, data=ticker_data, linestyle='-', marker='o', markersize=0.1)
        plt.xlabel("time")
        plt.ylabel(currency_pair)
        plt.show()

    def __show_ticker_candlesticks(self, command):

        currency_pair = command["parameters"]["currency_pair"]

        ticker_data = self.__db_control.get_objects_of_class(Ticker, command["parameters"])
        ticker_last_price = [float(data.last_price) for data in ticker_data]
        ticker_open_price = [float(data.open_price) for data in ticker_data]
        ticker_highest_bid = [float(data.highest_bid) for data in ticker_data]
        ticker_lowest_ask = [float(data.lowest_ask) for data in ticker_data]

        fig = plt.figure()
        ax = plt.subplot2grid((1, 1), (0, 0))
        candlestick2_ohlc(
            ax,
            ticker_last_price,
            ticker_open_price,
            ticker_highest_bid,
            ticker_lowest_ask,
            width=0.6
        )

        ticker_timestamp = [data.timestamp for data in ticker_data]
        ax.xaxis.set_major_locator(ticker.MaxNLocator(6))

        def mydate(x, pos):
            try:
                return ticker_timestamp[int(x)]
            except IndexError:
                return ''

        ax.xaxis.set_major_formatter(ticker.FuncFormatter(mydate))

        fig.autofmt_xdate()
        fig.tight_layout()

        ax.set_xlabel("time")
        ax.set_ylabel(currency_pair)
        plt.show()

