#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>
#include <time.h>
#include <math.h>

void check_arg_count(int argc, char **argv){
		
	if(argc < 4){
		fprintf(stderr, "Za mało argumentów\n Poprawna forma to:\n <dolna wartość przedziału całkowania>\n <górna wartość przedziału całkowania>\n <liczba punktów>\n");
		exit(-1);
	}

	double test = 0;
	int test_point = 0;
	if(((sscanf(argv[1], "%lf", &test) == 0) || (sscanf(argv[2], "%lf", &test) == 0)) || (sscanf(argv[3], "%d", &test_point) == 0)) {
		fprintf(stderr, "Argumenty powinny byc liczbami\n");
		exit(-1);
	}

	if(atoi(argv[1]) >= atoi(argv[2])){
		fprintf(stderr, "Zły przedział całkowania.\n");
		exit(-1);
	}

	if(atoi(argv[3]) <= 1){
		fprintf(stderr, "Liczba punktów musi być większa od jeden.\n");
		exit(-1);
	}
	
}


void prepare_indices(double *indices, double lower_bound, double upper_bound){

	int world_size;
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);
	int world_rank;
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

	double step = (upper_bound-lower_bound)/world_size;
	indices[0] = lower_bound + world_rank*step;
	if(world_size != world_rank + 1){
		indices[1] = lower_bound + (world_rank+1)*step;
	}else{
		indices[1] = upper_bound;		
	}
		
}

int prepare_points(int n_points){
	
	int world_size;
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);
	int world_rank;
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

	if(world_size >= n_points){
		printf("Więcej procesów od możliwych zadań\n");
		exit(-1);
	}

	int points;
	int step = n_points/world_size;
	
	
	if(world_size != world_rank + 1){
		points = step;
	}else{
		points = n_points - (world_size-1)*step;		
	}

	return points;
}

double linear_func(double x){
	return x;
}

double sin_func(double x){
	return sin(x);
}

double integrate(double (*func)(double), double lower_bound, double upper_bound, int points){

	double step = (upper_bound - lower_bound)/points;
	double sum = 0;
	
	int i;
	for(i = 0; i < points; i++){
		sum += func(lower_bound + i*step + step/2) * step;
	}	
	return sum;	
}

double calc_integral(double *indices, int points){


	double (*func)(double);
	func = &sin_func;

	int world_rank;
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
	int world_size;
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);

	double final_sum = 0;
	double partial_sum;	

	partial_sum = integrate(func, indices[0], indices[1], points);

	if(world_rank == 0){
		final_sum = partial_sum;
		int i;
		for(i = 1; i < world_size; i++){
			MPI_Recv(&partial_sum, 1, MPI_DOUBLE, i, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			final_sum += partial_sum;
		}
		printf("Calka oznaczona wynosi %f\n", final_sum);
	}else{
		MPI_Send(&partial_sum, 1, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD);
	}


	return final_sum;
}

void print_summary(clock_t start, clock_t end){

	
	char name[MPI_MAX_PROCESSOR_NAME];
	int name_len;
	MPI_Get_processor_name(name, &name_len);

	int world_rank;
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

	long double time = ((double)(end-start))/CLOCKS_PER_SEC;
	printf("Proces %d na komputerze %s zajął %Lf sekund\n", world_rank, name, time);
}

int main(int argc, char** argv){

	clock_t start = clock();
	MPI_Init(NULL, NULL);

	check_arg_count(argc, argv);
	double lower_bound = atof(argv[1]);
	double upper_bound = atof(argv[2]);
	int n_points = atoi(argv[3]);

	double indices[2];
	prepare_indices(indices, lower_bound, upper_bound);
	int points = prepare_points(n_points);

	calc_integral(indices, points);

	clock_t end = clock();
	print_summary(start, end);
	MPI_Finalize();

	return 0;

}
