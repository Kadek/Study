import numpy as np
import os
import pandas as pd
import pymongo
import random
import time
import urllib
import math

random.seed(time.time())

class StockDataSet(object):
	def __init__(self,
			input_size=1,
			num_steps=30,
			test_ratio=0.1,
			password=None,
			batch_size=64):
		self.input_size = input_size
		self.batch_size = batch_size
		self.num_steps = num_steps
		self.test_ratio = test_ratio
		self.username = "bigdata"
		self.password = password

		mongo_cursor_indicators = self._read_mongo("technicalIndicators")
		mongo_cursor_names = self._read_mongo("companies")

		self._create_data_sectors(mongo_cursor_indicators, mongo_cursor_names, test_ratio)

	def _read_mongo(self, collection_name):
		username = urllib.quote_plus(self.username)
		password = urllib.quote_plus(self.password)

		client = pymongo.MongoClient("mongodb://%s:%s@127.0.0.1" % (username, password))
		db = client.projekt
		collection = db[collection_name]
		return collection

	def _read_company_names(self, mongo_cursor):
		
		mongo_cursor = mongo_cursor.find()
		return [company["name"] for company in mongo_cursor]

	def _create_data_sectors(self, mongo_cursor_indicators, mongo_cursor_names, test_ratio):

		self.train_data_sector = []
		self.test_data_sector = []

		self.train_data_size = 0
		self.test_data_size = 0

		company_names = self._read_company_names(mongo_cursor_names)
		for company_name in company_names:
			self._create_data_sector(mongo_cursor_indicators, test_ratio, company_name)

		print "Train data size " + str(self.train_data_size)
		print "Test data size " + str(self.test_data_size)

	def _create_data_sector(self, mongo_cursor, test_ratio, stock_sym):

		mongo_cursor = mongo_cursor.find({"firma": stock_sym}).sort("date", 1)
		data_size = mongo_cursor.count()
	
		train_data_size = math.floor(data_size * (1-test_ratio))
		train_data_size -= train_data_size % self.batch_size
		self.train_data_size += train_data_size
		
		test_data_size = data_size - train_data_size
		test_data_size -= test_data_size % self.batch_size
		self.test_data_size += test_data_size

		for i in range(int(train_data_size + test_data_size)):
			day = mongo_cursor.next()
			if(i < train_data_size):
				if(i % self.batch_size == 0):
					self.train_data_sector.append(day)
			else:
				if(i % self.batch_size == 0):
					self.test_data_sector.append(day)


	def info(self):
		return "StockDataSet [%s] train: %d test: %d" % (
			self.stock_sym, len(self.train_data_size), len(self.test_data_size))

	def _get_data_sector(self, mongo_cursor, index, data_type):
		
		if(data_type == "TRAIN"):
			begin = self.train_data_sector[index]
			end = self.train_data_sector[index+1]
		else:
			begin = self.test_data_sector[index]
			end = self.test_data_sector[index+1]

		mongo_cursor = mongo_cursor.find({"$and": [{"firma": begin["firma"]}, {"date":{"$gt":begin["date"]}}]}).sort("date", 1)
		data = []
		for i in range(self.batch_size):
			day = mongo_cursor.next() 
			data.append(day)
		return data

	def _extract_X(self, data):
		X_data = [data[i:(self.num_steps+i)] for i in range(len(data) - self.num_steps)]

		clean_X_data = []
		for input_unit in X_data:
			clean_input_unit = []
			for day in input_unit:
				clean_day = day.copy()
				try:
					del clean_day["firma"]
					del clean_day["_id"]
					del clean_day["date"]
				except KeyError:
					pass
				clean_input_unit.append(clean_day.values())
			clean_X_data.append(clean_input_unit)
		return clean_X_data
	
	def _extract_Y(self, data):
		data = map(lambda x: x["price"], data)
		Y_data = [[data[self.num_steps+i]] for i in range(len(data) - self.num_steps)]
		return Y_data

	def generate_train_epoch(self, batch_size):
		return self._generate_epoch(self.train_data_size, "TRAIN") 
	
	def generate_test_epoch(self, batch_size):
		return self._generate_epoch(self.test_data_size, "TEST")
	
	def _generate_epoch(self, data_size, data_type):
		mongo_cursor = self._read_mongo("technicalIndicators")

		if data_type == "TRAIN":
			num_batches = len(self.train_data_sector)
		else:
			num_batches = len(self.test_data_sector)

		batch_indices = range(num_batches - 1)
		random.shuffle(batch_indices)
		for j in batch_indices:
			if data_type == "TRAIN":
				data_sector = self._get_data_sector(mongo_cursor, j, data_type)
			else:
				data_sector = self._get_data_sector(mongo_cursor, j, data_type)
			batch_X = self._extract_X(data_sector)
			batch_y = self._extract_Y(data_sector)
			yield batch_X, batch_y

	def get_train_num_batches(self):
		return len(self.train_data_sector)
