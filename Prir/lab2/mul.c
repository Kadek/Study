#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <math.h>

typedef struct {
	int nA;
	int mA;
	int nB;
	int mB;
} matrixes_size_struct;

typedef struct {
	double **A;
	double **B;
	double **C;
	int *indices;
	matrixes_size_struct *matrixes_size;
} thread_args_struct;

typedef struct {
	int id;
} thread_id_struct;

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

thread_args_struct thread_args;

double frobius = 0;

void* thread_mul(void *args){
	double **A = thread_args.A;
	double **B = thread_args.B;
	double **C = thread_args.C;
	int *indices = thread_args.indices;
	matrixes_size_struct *matrixes_size = thread_args.matrixes_size;
	thread_id_struct* thread_id = (thread_id_struct*) args;
	int n_thread = thread_id->id;

	int bottom = indices[2*n_thread];
	int top = indices[2*n_thread + 1];

	int i;
	int factor = matrixes_size->nA*matrixes_size->nB;

	double sum = 0;
	int oldRowA = -1;
	int oldColumnB = -1;

	int rowA;
	int columnA;
	int columnB;

	for(i = bottom; i <= top; i++){
		rowA = i/factor;
		columnB = (i % factor)/matrixes_size->nA;
		columnA = (i % factor) % matrixes_size->nA;

		if((oldRowA != -1 && oldColumnB != -1) && ((oldRowA != rowA) || (oldColumnB != columnB))){	
			pthread_mutex_lock(&mutex);
			C[oldRowA][oldColumnB] += sum;
			sum = 0;
			pthread_mutex_unlock(&mutex);
		}
	
		sum += A[rowA][columnA]*B[columnA][columnB];
		oldRowA = rowA;
		oldColumnB = columnB;
	}

	pthread_mutex_lock(&mutex);
	C[rowA][columnB] += sum;
	pthread_mutex_unlock(&mutex);
	printf("Wątek %d kończy działanie\n", n_thread);

	return 0;
}

void* thread_frob(void *args){
	double **C = thread_args.C;
	int *indices = thread_args.indices;
	matrixes_size_struct *matrixes_size = thread_args.matrixes_size;
	thread_id_struct* thread_id = (thread_id_struct*) args;
	int n_thread = thread_id->id;

	int bottom = indices[2*n_thread];
	int top = indices[2*n_thread + 1];

	int i;

	for(i = bottom; i <= top; i++){

		int columnC = i/matrixes_size->mA;
		int rowC = i % matrixes_size->mA;	

		pthread_mutex_lock(&mutex);
		frobius += C[rowC][columnC]*C[rowC][columnC];
		pthread_mutex_unlock(&mutex);
	
	}

	printf("Wątek %d kończy działanie\n", n_thread);

	return 0;
}

void print_matrix(double**A, int m, int n){
	int i, j;
	printf("[\n");
	for(i =0; i< m; i++)
	{
		for(j=0; j<n; j++)
		{
			printf("%f ", A[i][j]);
		}
		printf("\n");
	}
	printf("]\n");
}

void check_arg_count(int argc){
	if(argc < 5){
		fprintf(stderr, "Za mało argumentów\n Poprawna forma to:\n <nazwa pliku z macierzą> <nazwa pliku z macierzą> <liczba wątków do mnożenia macierzy> < liczba wątków do normy Froebiusa");
		exit(-1);
	}
}

FILE *open_file(char* name){	
	FILE *file = fopen(name, "r");
	if( file == NULL )
	{
		perror("Błąd otwarcia pliku");
		exit(-1);
	}
	return file;
}

matrixes_size_struct* get_matrixes_size(FILE *fileA, FILE *fileB){
	
	matrixes_size_struct *matrixes_size = (matrixes_size_struct*)malloc(sizeof(matrixes_size_struct));

	fscanf (fileA, "%d", &(matrixes_size->mA));
	fscanf (fileA, "%d", &(matrixes_size->nA));

	fscanf (fileB, "%d", &(matrixes_size->mB));
	fscanf (fileB, "%d", &(matrixes_size->nB));

	printf("pierwsza macierz ma wymiar %d x %d, a druga %d x %d\n", matrixes_size->mA, matrixes_size->nA, matrixes_size->mB, matrixes_size->nB);

	if(matrixes_size->nA != matrixes_size->mB)
	{
		fprintf(stderr, "Złe wymiary macierzy!\n");
		exit(-1);
	}
	
	return matrixes_size;
}

void read_matrixes(double ***A, double ***B, double ***C, matrixes_size_struct *matrixes_size, FILE *fileA, FILE *fileB){
	int i,j;
	double x;
	/*Alokacja pamięci*/
	*A = (double**)malloc(matrixes_size->mA*sizeof(double*));
	for(i=0; i< matrixes_size->mA; i++)
	{
		(*A)[i] = (double*)malloc(matrixes_size->nA*sizeof(double));
		for(j = 0; j < matrixes_size->nA; j++){
			(*A)[i][j] = 0;
		}
	}

	*B = (double**)malloc(matrixes_size->mB*sizeof(double*));
	for(i=0; i< matrixes_size->mB; i++)
	{
		(*B)[i] = (double*)malloc(matrixes_size->nB*sizeof(double));
		for(j = 0; j < matrixes_size->nB; j++){
			(*B)[i][j] = 0;
		}
	}

	/*Macierz matrixes_size->nA wynik*/
	*C = (double**)malloc(matrixes_size->mA*sizeof(double*));
	for(i=0; i< matrixes_size->mA; i++)
	{
		(*C)[i] = (double*)malloc(matrixes_size->nB*sizeof(double));
		for(j = 0; j < matrixes_size->nB; j++){
			(*C)[i][j] = 0;
		}
	}

	printf("Rozmiar C: %dx%d\n", matrixes_size->mA, matrixes_size->nB);
	for(i =0; i< matrixes_size->mA; i++)
	{
		for(j = 0; j<matrixes_size->nA; j++)
		{
			fscanf( fileA, "%lf", &x );
			(*A)[i][j] = x;
		}
	}

	printf("A:\n");
	print_matrix(*A, matrixes_size->mA, matrixes_size->mB);

	for(i =0; i< matrixes_size->mB; i++)
	{
		for(j = 0; j<matrixes_size->nB; j++)
		{
			fscanf( fileB, "%lf", &x );
			(*B)[i][j] = x;
		}
	}

	printf("B:\n");
	print_matrix(*B, matrixes_size->mB, matrixes_size->nB);

}

void prepare_indices_mul(int n_threads,  matrixes_size_struct *matrixes_size, int **indices){
	int all_indices;	
	int span;
	int i;
	*indices = (int*)malloc(2*n_threads*sizeof(int));

	all_indices = matrixes_size->nA * matrixes_size->nB * matrixes_size->mA;
	if(all_indices < n_threads){
		fprintf(stderr, "Liczba wątków większa od możliwej liczby zadań dla mnożenia macierzy.\n");
		exit(-1);
	}

	span = all_indices/n_threads;

	for(i = 0; i < n_threads-1; i++){
		(*indices)[2*i] = i*span;
		(*indices)[2*i+1] = (i+1)*span - 1;
	}
	
	(*indices)[2*i] = i*span;
	(*indices)[2*i+1] = all_indices-1;
}

void multiply(double **A, double **B, double **C,  matrixes_size_struct *matrixes_size, int* indices, int n_threads){
	int i;
	pthread_t *threads = (pthread_t*)malloc(n_threads*sizeof(pthread_t));
	thread_id_struct **ids = (thread_id_struct**)malloc(n_threads*sizeof(thread_id_struct*));

	thread_args.A = A;
	thread_args.B = B;
	thread_args.C = C;
	thread_args.matrixes_size = matrixes_size;
	thread_args.indices = indices;

	for(i = 0; i < n_threads; i++){
		ids[i] = (thread_id_struct*)malloc(sizeof(thread_id_struct));
		ids[i]->id = i;

		if(pthread_create(&(threads[i]), NULL, thread_mul, ids[i])){
			fprintf(stderr, "Błąd przy tworzeniu wątków");
			exit(-1);
		}
	}
	
	for(i = 0; i < n_threads; i++){
		if(pthread_join(threads[i], NULL)){
			fprintf(stderr, "Błąd przy łączeniu wątków");
			exit(-1);
		}
	}

	free(threads);
	for(i = 0; i < n_threads; i++){
		free(ids[i]);	
	}
	free(ids);
}

void prepare_indices_frob(int n_threads, matrixes_size_struct *matrixes_size, int **indices){
	int all_indices;
	int span;
	int i;	
	*indices = (int*)malloc(2*n_threads*sizeof(int));
	all_indices = matrixes_size->mA * matrixes_size->nB;
	if(all_indices < n_threads){
		fprintf(stderr, "Liczba wątków większa od możliwej liczby zadań dla wyliczenia normy Froebiusa.\n");
		exit(-1);
	}

	span = all_indices/n_threads;

	for(i = 0; i < n_threads-1; i++){
		(*indices)[2*i] = i*span;
		(*indices)[2*i+1] = (i+1)*span - 1;
	}
	
	(*indices)[2*i] = i*span;
	(*indices)[2*i+1] = all_indices-1;
}

void calculate_frobius(double **C, matrixes_size_struct *matrixes_size, int *indices, int n_threads){
	int i;
	pthread_t *threads = (pthread_t*)malloc(n_threads*sizeof(pthread_t));
	thread_id_struct **ids = (thread_id_struct**)malloc(n_threads*sizeof(thread_id_struct*));

	thread_args.A = NULL;
	thread_args.B = NULL;
	thread_args.C = C;
	thread_args.matrixes_size = matrixes_size;
	thread_args.indices = indices;

	
	for(i = 0; i < n_threads; i++){
		ids[i] = (thread_id_struct*)malloc(sizeof(thread_id_struct));
		ids[i]->id = i;

		if(pthread_create(&(threads[i]), NULL, thread_frob, ids[i])){
			fprintf(stderr, "Błąd przy tworzeniu wątków");
			exit(-1);
		}
	}
	
	for(i = 0; i < n_threads; i++){
		if(pthread_join(threads[i], NULL)){
			fprintf(stderr, "Błąd przy łączeniu wątków");
			exit(-1);
		}
	}

	free(threads);
	for(i = 0; i < n_threads; i++){
		free(ids[i]);	
	}
	free(ids);

	frobius = sqrt(frobius);
}

void free_mem(double **A, double **B, double **C, FILE *fileA, FILE *fileB, int *indices_mul, int *indices_frob, matrixes_size_struct *matrixes_size){
	int i;
	for(i=0; i<matrixes_size->mA; i++)
	{
		free(A[i]);
	}
	free(A);

	for(i=0; i<matrixes_size->mB; i++)
	{
		free(B[i]);
	}
	free(B);


	for(i=0; i<matrixes_size->mA; i++)
	{
		free(C[i]);
	}
	free(C);

   
	fclose(fileA);
	fclose(fileB);
	

	free(indices_mul);
	free(indices_frob);
	free(matrixes_size);
}

int main(int argc, char** argv){
	FILE *fileA, *fileB;
	double **A, **B, **C;
	int n_threads_mul, n_threads_frob;
	int *indices_mul, *indices_frob;
	check_arg_count(argc);
	fileA = open_file(argv[1]);
	fileB = open_file(argv[2]);
	A = NULL;
	B = NULL;
	C = NULL;
	n_threads_mul = atoi(argv[3]);
	n_threads_frob = atoi(argv[4]);

	matrixes_size_struct *matrixes_size;
	matrixes_size = get_matrixes_size(fileA, fileB); 
	read_matrixes(&A, &B, &C, matrixes_size, fileA, fileB);

	prepare_indices_mul(n_threads_mul, matrixes_size, &indices_mul);
	multiply(A, B, C, matrixes_size, indices_mul, n_threads_mul);
	print_matrix(C, matrixes_size->mA, matrixes_size->nB);
	
	prepare_indices_frob(n_threads_frob, matrixes_size, &indices_frob);
	calculate_frobius(C, matrixes_size, indices_frob, n_threads_frob);
	printf("Norma Froebiusa wynosi %f\n", frobius); 

	free_mem(A, B, C, fileA, fileB, indices_mul, indices_frob, matrixes_size);
	return 0;
}
