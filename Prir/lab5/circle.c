#include<stdio.h>
#include<stdlib.h>
#include<mpi.h>
#include<time.h>

int random_nr(int world_rank){

	int nr;
	srand(time(NULL)+world_rank);
	nr=rand()%500+1;
	return nr;
}

int find_gcd(int a, int b) {

	int c;
	while(b!=0){
		c=a%b;
		a=b;
		b=c;
	}
	return a;
}

int mod(int a, int b) {
	int r = a % b;
	return r < 0 ? r + b : r;
}

int main(int argc, char** argv){

	MPI_Init(NULL, NULL);
	int world_rank, world_size, rand_nr, i, gcd, rand_nr_received;
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);
	rand_nr=random_nr(world_rank);	

	for(i=1; i<=world_size; i *= 2){

		int send_rank = ((world_rank + i) % world_size);	
		if(world_rank == send_rank)
			continue;
		MPI_Send(&rand_nr,1,MPI_INT,send_rank,0,MPI_COMM_WORLD);
	}

	for(i=1; i<=world_size; i *= 2){

		int receive_rank = mod((world_rank - i), world_size);	
		if(world_rank == receive_rank)
			continue;
		MPI_Recv(&rand_nr_received, 1,MPI_INT,receive_rank,0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

		gcd = find_gcd(rand_nr, rand_nr_received);
		printf("%d odbiera od %d, NWD(%d, %d) = %d \n", world_rank, receive_rank, rand_nr, rand_nr_received, gcd);
	}	
	
	MPI_Finalize();
	return 0;
}
