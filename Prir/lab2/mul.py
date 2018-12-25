# -*- coding: utf-8 -*-

from __future__ import print_function
import math
import threading
import sys

C = [[]]
frobius = 0
mutex = threading.Lock()

class MulThread(threading.Thread):
	def __init__(self, data_pack):

		threading.Thread.__init__(self)
		self.data_pack = data_pack
	
	def run(self):

		n_thread = self.data_pack["n_thread"]
		bottom = self.data_pack["indices"][2*n_thread]	
		top = self.data_pack["indices"][2*n_thread + 1]

		factor = self.data_pack["sizeA"]["n"]*self.data_pack["sizeB"]["n"]
		
		mul_sum = 0
		
		old_rowA = -1
		old_columnB = -1

		rowA = 0
		columnA = 0
		columnB = 0

		for i in range(bottom, top + 1):
			rowA = i/factor
			columnB = (i % factor)/self.data_pack["sizeA"]["n"]
			columnA = (i % factor) % self.data_pack["sizeA"]["n"]

			if (old_rowA != -1 and old_columnB != -1) and (old_rowA != rowA or old_columnB != columnB):
				mutex.acquire()
				C[old_rowA][old_columnB] += mul_sum
				mul_sum = 0
				mutex.release()

			mul_sum += self.data_pack["A"][rowA][columnA]*self.data_pack["B"][columnA][columnB]
			old_rowA = rowA
			old_columnB = columnB

		mutex.acquire()
		C[old_rowA][old_columnB] += mul_sum
		mutex.release()

		print("Wątek " + str(n_thread) + " kończy działanie")

class FrobThread(threading.Thread):
	def __init__(self, data_pack):

		threading.Thread.__init__(self)
		self.data_pack = data_pack
	
	def run(self):

		n_thread = self.data_pack["n_thread"]
		bottom = self.data_pack["indices"][2*n_thread]	
		top = self.data_pack["indices"][2*n_thread + 1]

		global frobius

		for i in range(bottom, top + 1):

			rowC = i/len(C)
			columnC = i % len(C)

			mutex.acquire()
			frobius += C[rowC][columnC]*C[rowC][columnC]
			mutex.release()


		print("Wątek " + str(n_thread) + " kończy działanie")

def check_args():

	if len(sys.argv) < 5:
		print("""Za mała liczba argumentów. 
	Poprawna forma to:
	<nazwa pliku z macierzą> 
	<nazwa pliku z macierzą> 
	<liczba wątków do mnożenia macierzy>
	<liczba wątków do wyliczenia normy Froebiusa>""")
		sys.exit()

def open_file(filename):

	f = open(filename, "r")
	if f is None:
		print("Nie udało się otworzyć pliku.")

	return f

def get_matrix_size(file_loaded):

	matrix_size = {}

	matrix_size["m"] = int(file_loaded.readline())
	matrix_size["n"] = int(file_loaded.readline())

	return matrix_size

def check_sizes(A_size, B_size):

	if A_size["n"] != B_size["m"]:
		print("Złe rozmiary macierzy")
		exit(-1)	

def read_matrix(file_loaded, size):

	i = 0
	matrix = [[0 for x in range(size["n"])] for y in range(size["m"])]
	for line in file_loaded:
		matrix[i/size["n"]][i%size["n"]] = float(line)
		i += 1

	return matrix

def prepare_indices_mul(n_threads, sizeA, sizeB):

	all_indices = sizeA["n"]*sizeA["m"]*sizeB["n"]
	if all_indices < n_threads:
		print("Liczba wątków większa od możliwej liczby zadań w mnożeniu macierzy.\n")
		exit()
	

	span = all_indices/n_threads
	
	indices = [0 for x in range(n_threads*2)]
	i = 0
	for i in range(n_threads-1):
		indices[2*i] = i*span
		indices[2*i + 1] = (i+1)*span -1

	if(n_threads > 1):	
		i += 1

	indices[2*i] = i*span
	indices[2*i + 1] = all_indices-1

	return indices

def multiply(A, B, sizeA, sizeB, indices, n_threads):

	threads = []

	for i in range(n_threads):

		data_pack = {
			"A": A,
			"B": B,
			"sizeA": sizeA,
			"sizeB": sizeB,
			"indices": indices,
			"n_thread": i
		}
		thread = MulThread(data_pack)
		thread.start()
		threads.append(thread)

	for i in range(n_threads):

		threads[i].join()

def prepare_indices_frob(n_threads):

	all_indices = len(C)*len(C[0])

	if all_indices < n_threads:
		print("Liczba wątków większa od możliwej liczby zadań w wyliczaniu normy Froebiusa.\n")
		exit()
	

	span = all_indices/n_threads
	
	indices = [0 for x in range(n_threads*2)]
	i = 0
	for i in range(n_threads-1):
		indices[2*i] = i*span
		indices[2*i + 1] = (i+1)*span -1

	if(n_threads > 1):	
		i += 1

	indices[2*i] = i*span
	indices[2*i + 1] = all_indices-1

	return indices

def calculate_frobius(indices, n_threads):

	threads = []

	for i in range(n_threads):

		data_pack = {
			"indices": indices,
			"n_thread": i
		}
		thread = FrobThread(data_pack)
		thread.start()
		threads.append(thread)

	for i in range(n_threads):

		threads[i].join()

	global frobius
	frobius = math.sqrt(frobius)

def print_matrix(matrix):

	print("[")
	for i in range(len(matrix)):
		for j in range(len(matrix[0])):
			print(matrix[i][j], end=" ")
		print("")
	print("]")	

	
if __name__ == "__main__":
	
	check_args()

	fileA = open_file(sys.argv[1])	
	fileB = open_file(sys.argv[2])
	
	n_threads_mul = int(sys.argv[3])
	n_threads_frob = int(sys.argv[4])

	A_size = get_matrix_size(fileA)
	B_size = get_matrix_size(fileB)
	check_sizes(A_size, B_size)

	C = [[0 for x in range(B_size["n"])] for y in range(A_size["m"])]

	A = read_matrix(fileA, A_size)
	print_matrix(A)
	B = read_matrix(fileB, B_size)
	print_matrix(B)

	fileA.close()
	fileB.close()

	indices_mul = prepare_indices_mul(n_threads_mul, A_size, B_size)
	multiply(A, B, A_size, B_size, indices_mul, n_threads_mul)
	print_matrix(C)

	indices_frob = prepare_indices_frob(n_threads_frob)
	calculate_frobius(indices_frob, n_threads_frob)
	print("Norma Froebiusa wynosi " + str(frobius))
