#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>
#include <time.h>

#define VECTOR_SIZE 100
#define PARTITION_SIZE 10

void fill_vector(int *vector){
	srand(time(NULL));
	int i;
	for(i = 0; i < VECTOR_SIZE; i++){
		vector[i] = rand();
	}
}

void share_vector(int *vector){

	int world_rank;
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
	int world_size;
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);

	int dims[1] = {world_size}, periods[1] = {0}, reorder = 0;
	MPI_Comm cart_comm;
	MPI_Cart_create(MPI_COMM_WORLD, 1, dims, periods, reorder, &cart_comm);

	int n_iter = VECTOR_SIZE/PARTITION_SIZE;
	int i;
	int other_rank;
	for(i = 0; i < n_iter; i++){
			
		MPI_Cart_shift(cart_comm, 0, -1, &world_rank, &other_rank);
		MPI_Recv(vector + i*PARTITION_SIZE, PARTITION_SIZE, MPI_INT, other_rank, 0, cart_comm, MPI_STATUS_IGNORE);

		MPI_Cart_shift(cart_comm, 0, 1, &world_rank, &other_rank);
		MPI_Send(vector + i*PARTITION_SIZE, PARTITION_SIZE, MPI_INT, other_rank, 0, cart_comm);
		
	}

}

void print_summary(int rank, int *vector){

	char file_name[6];
	sprintf(file_name, "%d.txt", rank);
	FILE *f = fopen(file_name, "w");
	if(f == NULL){
		printf("Problem z otworzeniem pliku.\n");
		exit(-1);
	}

	int i;
	for(i = 0; i< VECTOR_SIZE; i++){
		fprintf(f, "%d\n", vector[i]);
	}

	fclose(f);	
}

int main(int argc, char** argv){

	MPI_Init(NULL, NULL);

	int vector[VECTOR_SIZE];
	int world_rank;
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
	if(world_rank == 0){
		fill_vector(vector);
	}

	share_vector(vector);

	print_summary(world_rank, vector);
	MPI_Finalize();

	return 0;

}
