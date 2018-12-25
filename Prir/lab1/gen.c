#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#define BUFFOR_SIZE 1000

//Generator wektora

void fill_vector(double* vector){
	
	srand(time(NULL));
	for(int i = 0; i < BUFFOR_SIZE; i++){
		vector[i] = (((double)rand()/RAND_MAX)*2.0-1)*100;
	}

}

void save_file(double* vector, char* filename){

	FILE* f = fopen(filename, "w");
	if(f == NULL){
		fprintf(stderr, "Nie udało się odczytać ani utworzyć pliku");
		exit(-1);
	}

	fprintf(f, "%d\n", BUFFOR_SIZE);

	int i;
	for(i=0; i<BUFFOR_SIZE; i++) {
		fprintf(f, "%f\n", vector[i]);
	}
	fclose(f);

}

int main(int argc, char** argv){
	
	if(argc < 2){
		fprintf(stderr, "Za mała ilość argumentów");
		exit(-1);
	}

	double vector[BUFFOR_SIZE];

	fill_vector(vector);
	save_file(vector, argv[1]);

	fprintf(stdout, "Zrobione!\n");	
}
