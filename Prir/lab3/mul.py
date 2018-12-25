# -*- coding: utf-8 -*-

from __future__ import print_function
import sys
import time

import pp

secret = "secret"

def multiply_job(data_pack):

	n_thread = data_pack["n_thread"]
	bottom = data_pack["indices"][2*n_thread]	
	top = data_pack["indices"][2*n_thread + 1]

	factor = data_pack["sizeA"]["n"]*data_pack["sizeB"]["n"]

	return_pack = []	
	mul_sum = 0
	
	old_rowA = -1
	old_columnB = -1

	rowA = 0
	columnA = 0
	columnB = 0

	for i in range(bottom, top + 1):
		rowA = i/factor
		columnB = (i % factor)/data_pack["sizeA"]["n"]
		columnA = (i % factor) % data_pack["sizeA"]["n"]

		if (old_rowA != -1 and old_columnB != -1) and (old_rowA != rowA or old_columnB != columnB):
			return_unit = {
				"row": old_rowA,
				"column": old_columnB,
				"sum": mul_sum
			}
			return_pack.append(return_unit)
			mul_sum = 0

		mul_sum += data_pack["A"][rowA][columnA]*data_pack["B"][columnA][columnB]
		old_rowA = rowA
		old_columnB = columnB
			
	return_unit = {
		"row": rowA,
		"column": columnB,
		"sum": mul_sum
	}
	return_pack.append(return_unit)

	return return_pack

def check_args():

	if len(sys.argv) < 4:
		print("""Za mała liczba argumentów. 
	Poprawna forma to:
	<nazwa pliku z macierzą> 
	<nazwa pliku z macierzą> 
	<liczba wątków do mnożenia macierzy>""")
		sys.exit()

	try:
		int(sys.argv[3])
	except ValueError:
		print("Zły format liczby wątków. Należy podać liczbę całkowitą wiekszą od zera.")
		sys.exit()

	if int(sys.argv[3]) <= 0:
		print("Zły format liczby wątków. Należy podać liczbę całkowitą wiekszą od zera.")
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

def prepare_indices(n_threads, sizeA, sizeB):

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

def measure_time(func):
	def wrapper(*args):
		time_start = time.time()
		return_value = func(*args)
		print("Dokonano obliczeń w " + str(time.time() - time_start) + " jednostkach czasu")
		return return_value
	return wrapper

@measure_time
def multiply(A, B, C, sizeA, sizeB, indices, n_threads):

	global secret
	ppservers = ("*",)

	job_server = pp.Server(ncpus=4,ppservers=ppservers, secret=secret)

	#Czekamy na znalezienie węzłów klastra
	time.sleep(1)

	print(job_server.get_active_nodes())

	jobs = []

	for i in range(n_threads):

		data_pack = {
			"A": A,
			"B": B,
			"sizeA": sizeA,
			"sizeB": sizeB,
			"indices": indices,
			"n_thread": i
		}
		job = job_server.submit(multiply_job,(data_pack,))
		jobs.append(job)

	for i in range(n_threads):

		job_results = jobs[i]()
		for result in job_results:
			C[result["row"]][result["column"]] += result["sum"]

	job_server.print_stats()

	return C

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
	
	n_threads = int(sys.argv[3])

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

	indices = prepare_indices(n_threads, A_size, B_size)
	C = multiply(A, B, C, A_size, B_size, indices, n_threads)
	print_matrix(C)

