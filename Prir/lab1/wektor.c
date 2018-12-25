#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <wait.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/types.h>
#define BUFFOR_SIZE 1000

int active_processes_count = 0;
pid_t* active_processes;

int ranges_key = 6784;
int partial_sums_key = 6904;
int vector_key = 9084;

int ranges_id;
int partial_sums_id;
int vector_id;

bool sent = false;
bool calculated = false;

struct vector_info{
	double* vector;
	int vector_size;
};

void handle_signal_parent(int sig, siginfo_t *info, void *ptr){
	
	if(sig == SIGUSR1){
		if(info->si_pid == getpid())
			return;
		active_processes[active_processes_count] = info->si_pid;
		active_processes_count++;
		kill(info->si_pid, SIGUSR1);
	}else if(sig == SIGUSR2){
		active_processes_count--;
		kill(info->si_pid, SIGUSR2);
	}
}
void handle_signal_child(int sig){
	
	if(sig == SIGUSR1){
		sent = true;
	}else if(sig == SIGUSR2){
		calculated = true;	
	}

}

void prepare_for_signals_parent(){
	struct sigaction setup_action;

	setup_action.sa_flags = SA_SIGINFO;
	setup_action.sa_sigaction = handle_signal_parent;
	sigaction(SIGUSR1, &setup_action, 0);
	sigaction(SIGUSR2, &setup_action, 0);
}
void prepare_for_signals_child(){
	struct sigaction setup_action;
	sigset_t block_mask;

	sigemptyset (&block_mask);
	setup_action.sa_mask = block_mask;
	setup_action.sa_flags = 0;
	setup_action.sa_handler = handle_signal_child;
	sigaction(SIGUSR1, &setup_action, 0);
	sigaction(SIGUSR2, &setup_action, 0);
}

void inform_created(pid_t parent_pid){
	do{
		kill(parent_pid, SIGUSR1);
	}while(!sent);
}

void compute_sum(int count){
	
	if( ((ranges_id = shmget(ranges_key, 0, 0666)) < 0)){
		perror("range shmget error");
		exit(-1);
	}

	int* ranges_addr;
	if( (ranges_addr = (int*)shmat(ranges_id, 0, 0)) < 0){
		perror("ranges shmat error\n");
		exit(-1);
	}

	int bottom = ranges_addr[count*2];
	int top = ranges_addr[count*2+1];
	
	shmdt(ranges_addr);

	count *= 2;
	int i;
	double sum = 0;
	double* vector_addr;
	
	if((vector_id = shmget(vector_key, 0, 0)) < 0){
		perror("shmget vector error");
		exit(-1);
	}

	if((vector_addr = (double*)shmat(vector_id, 0, 0)) < 0){
		perror("vector shmat error\n");
		exit(-1);
	}

	for(i = bottom; i <= top; i++){
		sum += vector_addr[i];
	}	
	shmdt(vector_addr);

	if((partial_sums_id = shmget(partial_sums_key, 0, 0)) < 0){
		perror("shmget partial sums error");
		exit(-1);
	}

	double* partial_sums_addr;
	if((partial_sums_addr = (double*)shmat(partial_sums_id, 0, 0)) < 0){
		perror("partial sums shmat error\n");
		exit(-1);
	}
	partial_sums_addr[count/2] = sum;

	shmdt(partial_sums_addr);
	
}

void wait_for_creation(int n){
	
	while(active_processes_count != n){
		sleep(1);	
		errno = 0;
	}
}

int prepare_children(int n, int count){
	pid_t parent_pid = getpid();
	pid_t child_pid;

	switch(child_pid = fork()){
		case -1:
			fprintf(stderr, "Nie udało się utworzyć dziecka.");
			exit(-1);
		case 0:
			prepare_for_signals_child();
			inform_created(parent_pid);
			pause();
			errno = 0;
			compute_sum(count);
			
			do{
				kill(parent_pid, SIGUSR2);
			}while(!calculated);
			exit(0);
		default:
			count++;
			if(count == n){
				wait_for_creation(n);
			}else{
				prepare_children(n, count);
			}
	
	}	
}

struct vector_info read_file(struct vector_info vector_instance, char* filename){
	FILE* f = fopen(filename, "r");
	char buffor[BUFFOR_SIZE+1];
	int n;
	int i;

	if( f == NULL ){
		fprintf(stderr, "Nie udało sie otworzyć pliku.\n");
		exit(-1);
	}

	fgets(buffor, BUFFOR_SIZE, f);
 	n = atoi(buffor);
	vector_instance.vector_size = n;
	vector_instance.vector = (double*)malloc(sizeof(double) * n);
	for(i=0; i<n; i++) {
		fgets(buffor, BUFFOR_SIZE, f);
		vector_instance.vector[i] = atof(buffor);
	}
	fclose(f);


	return vector_instance;
}

void create_ranges_memory(int n_processes){	
	if( (ranges_id = shmget(ranges_key, n_processes*2*sizeof(int), 0666 | IPC_CREAT)) < 0){
		perror("shmget ranges error");
		exit(-1);
	}
}

void fill_ranges_memory(int n_processes, int vector_size){
	int* ranges_addr;
	if( (ranges_addr = (int*)shmat(ranges_id, 0, 0)) < 0)
		perror("shmat error");

	int* addr = ranges_addr;
	int step = vector_size/n_processes;
	int i;
	for(i = 0; i < n_processes; i += 1){
		*addr++ = i*step;
		*addr++ = (i+1)*step -1;
	}
	*(--addr) = vector_size -1;

	addr = ranges_addr;
	shmdt(ranges_addr);
}

void create_partial_sums_memory(int n_processes){
	if( (partial_sums_id = shmget(partial_sums_key, sizeof(int)*n_processes, 0666 | IPC_CREAT)) < 0){
		perror("shmget error");
		exit(-1);
	}
}

int prepare_shared_memory(int n_processes, int vector_size){
	if(n_processes > vector_size){
		fprintf(stderr, "Więcej procesorów od możliwych zadań.\n");
		exit(-1);
	}

	create_ranges_memory(n_processes);
	fill_ranges_memory(n_processes, vector_size);
	create_partial_sums_memory(n_processes);
}

void share_vector(double* vector, int vector_size){
	
	int size = vector_size*sizeof(double);
	if( (vector_id = shmget(vector_key, size, 0666 | IPC_CREAT)) < 0){
		perror("shmget error");
		exit(-1);
	}
 
	double* vector_addr;
	if( (vector_addr = (double*)shmat(vector_id, 0, 0)) < 0)
		perror("shmat error");
	
	memcpy(vector_addr, vector, sizeof(double)*vector_size);
	int i;

	shmdt(vector_addr);
}

void compute_subsums(){
	int i;
	/* nie jest tu wkorzystywana metoda kill(-getpid(), SIGUSR1), gdyż gdy program wektor
	   wywoływany jest przez inny program(speed.c) to -getpid nie odpowiada grupie utworzonych tu procesów
	   i kill nie odnajduje procesów którym ma wysłać sygnał */
	for(i = 0; i<active_processes_count; i++){
		kill(active_processes[i], SIGUSR1);
	}

	while(active_processes_count > 0){
		sleep(1);
		errno = 0;
	}
}

double get_final_sum(int n_processes){

	double* partial_sums_addr;	
	if( (partial_sums_addr = (double*)shmat(partial_sums_id, 0, 0)) < 0)
		perror("ranges shmat error\n");

	int i;
	double sum = 0;
	for(i = 0; i < n_processes; i++){
		sum += partial_sums_addr[i];
	}
	//Wypisywanie zablokowane w celu niezakłócania eksperymentu
	//printf("Suma całego wektora = %f\n", sum);
	return sum;

}

void free_shared_memory(){
	shmctl(ranges_id, IPC_RMID, 0);
	shmctl(partial_sums_id, IPC_RMID, 0);
	shmctl(vector_id, IPC_RMID, 0);
}

int main(int argc, char** argv){
	
	if(argc < 3){
		fprintf(stderr, "Za mała ilość argumentów\n");
		exit(-1);
	}

	int n_processes = atoi(argv[1]);
	active_processes = (pid_t*)malloc(n_processes*sizeof(pid_t));

	prepare_for_signals_parent();
	if(prepare_children(n_processes, 0) <= 0)
		exit(0);

	struct vector_info vector_instance;
	vector_instance = read_file(vector_instance, argv[2]);
	
	double* vector = vector_instance.vector;
	int vector_size = vector_instance.vector_size;

	prepare_shared_memory(n_processes, vector_size);
	share_vector(vector, vector_size);
	compute_subsums();

	double sum = get_final_sum(n_processes);

	free_shared_memory();
	free(vector);
	free(active_processes);

	while(active_processes_count > 0){
		wait(0);
	}
	exit(0);
}
