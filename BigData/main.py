import os
import pandas as pd
import pprint

import tensorflow as tf
import tensorflow.contrib.slim as slim

from data_model import StockDataSet
from model_rnn import LstmRNN

flags = tf.app.flags
flags.DEFINE_integer("input_size", 14, "Input size [14]")
flags.DEFINE_integer("num_steps", 4, "Num of steps [4]")
flags.DEFINE_integer("num_layers", 1, "Num of layer [1]")
flags.DEFINE_integer("lstm_size", 128, "Size of one LSTM cell [128]")
flags.DEFINE_integer("batch_size", 10, "The size of batch images [10]")
flags.DEFINE_float("keep_prob", 0.8, "Keep probability of dropout layer. [0.8]")
flags.DEFINE_float("init_learning_rate", 0.001, "Initial learning rate at early stage. [0.001]")
flags.DEFINE_float("learning_rate_decay", 0.99, "Decay rate of learning rate. [0.99]")
flags.DEFINE_integer("init_epoch", 5, "Num. of epoches considered as early stage. [5]")
flags.DEFINE_integer("max_epoch", 10, "Total training epoches. [10]")
flags.DEFINE_boolean("train", False, "True for training, False for testing [False]")
flags.DEFINE_string("password", None, "Password to mongo db [None]")

FLAGS = flags.FLAGS

pp = pprint.PrettyPrinter()

if not os.path.exists("logs"):
	os.mkdir("logs")


def show_all_variables():
	model_vars = tf.trainable_variables()
	slim.model_analyzer.analyze_vars(model_vars, print_info=True)


def load_stocks(input_size, num_steps, test_ratio=0.1, password=None, batch_size=None):
	if password is not None:
		return StockDataSet(
				input_size=input_size,
				num_steps=num_steps,
				test_ratio=test_ratio,
				password=password,
				batch_size=batch_size)
	else:
		print "Password to mongo is required"

def main(_):
	stock_data_list = load_stocks(
		FLAGS.input_size,
		FLAGS.num_steps,
		password=FLAGS.password,
		batch_size=FLAGS.batch_size,
	)	
	run_config = tf.ConfigProto()
	run_config.gpu_options.allow_growth = True

	with tf.Session(config=run_config) as sess:
		rnn_model = LstmRNN(
			sess,
			lstm_size=FLAGS.lstm_size,
			num_layers=FLAGS.num_layers,
			num_steps=FLAGS.num_steps,
			input_size=FLAGS.input_size,
		)

		show_all_variables()

		if FLAGS.train:
			pred, real = rnn_model.train(stock_data_list, FLAGS)
		else:
			if not rnn_model.load()[0]:
				raise Exception("[!] Train a model first, then run test mode")


if __name__ == '__main__':
	tf.app.run()
