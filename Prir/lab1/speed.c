#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#define BUFFOR_SIZE 1000

//Test działania

int main(int argc, char** argv){
	
	if(argc < 2){
		fprintf(stderr, "Za mała ilość argumentów\n");
		exit(-1);
	}

	int n_processes_vector[6] = {1, 2, 4, 6, 8, 16};
	int n_processes = sizeof(n_processes_vector)/sizeof(int);

	int i;
	for(i = 0; i < n_processes; i++){

		int j;
		clock_t sum = 0;
		
		char command[64];
		strcpy(command, "./wektor ");

		char number[2];
		sprintf(number, "%d ", n_processes_vector[i]);
		strcat(command, number);

		strcat(command, argv[1]);
		printf("%s\n", command);
		for(j = 0; j < 100; j++){

			clock_t begin = clock();
			if(system(command) == -1){
				printf("Nieudane wykonanie programu.");
				exit(-1);
			}
			clock_t end = clock();

			sum += (end-begin);
		}
		clock_t avg_time = sum/100;
		printf("Dla %d procesów, program wykonuje się średnio w %ld\n jednostkach czasowych", n_processes_vector[i], avg_time);
	}

}
