import numpy as np
import os
import random
import re
import shutil
import time
import tensorflow as tf

#import matplotlib.pyplot as plt

from tensorflow.contrib.tensorboard.plugins import projector


class LstmRNN(object):
    def __init__(self, sess,
                 lstm_size=128,
                 num_layers=1,
                 num_steps=30,
                 input_size=14,
                 embed_size=None,
                 logs_dir="logs",
                 plots_dir="images"):
        """
        Construct a RNN model using LSTM cell.

        Args:
            sess:
            lstm_size (int)
            num_layers (int): num. of LSTM cell layers.
            num_steps (int)
            input_size (int)
            keep_prob (int): (1.0 - dropout rate.) for a LSTM cell.
            checkpoint_dir (str)
        """
        self.sess = sess

        self.lstm_size = lstm_size
        self.num_layers = num_layers
        self.num_steps = num_steps
        self.input_size = input_size

        self.logs_dir = logs_dir
        self.plots_dir = plots_dir

        self.build_graph()

    def build_graph(self):
        """
        The model asks for four things to be trained:
        - learning_rate
        - keep_prob: 1 - dropout rate
        - input: training data X
        - targets: training label y
        """
        # inputs.shape = (number of examples, number of input, dimension of each input).
        self.learning_rate = tf.placeholder(tf.float32, None, name="learning_rate")
        self.keep_prob = tf.placeholder(tf.float32, None, name="keep_prob")

        self.inputs = tf.placeholder(tf.float32, [None, self.num_steps, self.input_size], name="inputs")
        self.targets = tf.placeholder(tf.float32, [None, 1], name="targets")

        def _create_one_cell():
            lstm_cell = tf.contrib.rnn.LSTMCell(self.lstm_size, state_is_tuple=True)
            lstm_cell = tf.contrib.rnn.DropoutWrapper(lstm_cell, output_keep_prob=self.keep_prob)
            return lstm_cell

        cell = tf.contrib.rnn.MultiRNNCell(
            [_create_one_cell() for _ in range(self.num_layers)],
            state_is_tuple=True
        ) if self.num_layers > 1 else _create_one_cell()

        print "inputs.shape:", self.inputs.shape

        # Run dynamic RNN
        val, state_ = tf.nn.dynamic_rnn(cell, self.inputs, dtype=tf.float32, scope="dynamic_rnn")
        # Before transpose, val.get_shape() = (batch_size, num_steps, lstm_size)
        # After transpose, val.get_shape() = (num_steps, batch_size, lstm_size)
        val = tf.transpose(val, [1, 0, 2])

        last = tf.gather(val, int(val.get_shape()[0]) - 1, name="lstm_state")
        ws = tf.Variable(tf.truncated_normal([self.lstm_size, 1]), name="w")
        bias = tf.Variable(tf.constant(0.1, shape=[1]), name="b")
        self.pred = tf.matmul(last, ws) + bias

        self.last_sum = tf.summary.histogram("lstm_state", last)
        self.w_sum = tf.summary.histogram("w", ws)
        self.b_sum = tf.summary.histogram("b", bias)

        # self.loss = -tf.reduce_sum(targets * tf.log(tf.clip_by_value(prediction, 1e-10, 1.0)))
        self.loss = tf.reduce_mean(tf.square(self.pred - self.targets), name="loss_mse_train")
        self.optim = tf.train.RMSPropOptimizer(self.learning_rate).minimize(self.loss, name="rmsprop_optim")

        # Separated from train loss.
        self.loss_test = tf.reduce_mean(tf.square(self.pred - self.targets), name="loss_mse_test")

        self.loss_sum = tf.summary.scalar("loss_mse_train", self.loss)
        self.loss_test_sum = tf.summary.scalar("loss_mse_test", self.loss_test)
        self.learning_rate_sum = tf.summary.scalar("learning_rate", self.learning_rate)

        self.t_vars = tf.trainable_variables()
        self.saver = tf.train.Saver()

    def train(self, dataset, config):
        """
        Args:
            dataset (<StockDataSet>)
            config (tf.app.flags.FLAGS)
        """

        # Set up the logs folder
        self.writer = tf.summary.FileWriter(os.path.join("./logs", self.model_name))
        self.writer.add_graph(self.sess.graph)

        tf.global_variables_initializer().run()

        global_step = 0

        num_batches = dataset.get_train_num_batches()
        random.seed(time.time())

        print "Start training for stocks:"
	for epoch in xrange(config.max_epoch):
		epoch_step = 0
		learning_rate = config.init_learning_rate * (
			config.learning_rate_decay ** max(float(epoch + 1 - config.init_epoch), 0.0))
		for batch_X, batch_y in dataset.generate_train_epoch(config.batch_size): 
			global_step += 1
			epoch_step += 1
			train_data_feed = {
				self.learning_rate: learning_rate,
				self.keep_prob: config.keep_prob,
				self.inputs: batch_X,
				self.targets: batch_y,
			}
			train_loss, _ = self.sess.run([self.loss, self.optim], train_data_feed)

			if epoch_step % 10 == 0:
				
				print "Step:%d [Epoch:%d] [Learning rate: %.6f] train_loss:%.6f" % (
				    global_step, epoch, learning_rate, train_loss)

			self.save(global_step)

	final_loss_avg = 0
	final_losses = []
	for batch_X, batch_y in dataset.generate_train_epoch(config.batch_size):

		test_data_feed = {
		    self.learning_rate: 0.0,
		    self.keep_prob: 1.0,
		    self.inputs: batch_X,
		    self.targets: batch_y,
		}

		final_pred, final_loss = self.sess.run([self.pred, self.loss], test_data_feed)
		final_losses.append(final_loss)

	final_loss_avg = reduce(lambda x, y: x+y, final_losses)/len(final_losses)
	print "Step:%d [Epoch:%d] [Learning rate: %.6f] train_loss:%.6f test_loss:%.6f" % (
	    global_step, epoch, learning_rate, train_loss, final_loss_avg)

        # Save the final model
        self.save(global_step)
        return final_pred, batch_y

    @property
    def model_name(self):
        name = "stock_rnn_lstm%d_step%d_input%d" % (
            self.lstm_size, self.num_steps, self.input_size)

        return name

    @property
    def model_logs_dir(self):
        model_logs_dir = os.path.join(self.logs_dir, self.model_name)
        if not os.path.exists(model_logs_dir):
            os.makedirs(model_logs_dir)
        return model_logs_dir

    @property
    def model_plots_dir(self):
        model_plots_dir = os.path.join(self.plots_dir, self.model_name)
        if not os.path.exists(model_plots_dir):
            os.makedirs(model_plots_dir)
        return model_plots_dir

    def save(self, step):
        model_name = self.model_name + ".model"
        self.saver.save(
            self.sess,
            os.path.join(self.model_logs_dir, model_name),
            global_step=step
        )

    def load(self):
        print(" [*] Reading checkpoints...")
        ckpt = tf.train.get_checkpoint_state(self.model_logs_dir)
        if ckpt and ckpt.model_checkpoint_path:
            ckpt_name = os.path.basename(ckpt.model_checkpoint_path)
            self.saver.restore(self.sess, os.path.join(self.model_logs_dir, ckpt_name))
            counter = int(next(re.finditer("(\d+)(?!.*\d)", ckpt_name)).group(0))
            print(" [*] Success to read {}".format(ckpt_name))
            return True, counter

        else:
            print(" [*] Failed to find a checkpoint")
            return False, 0
