#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <getopt.h>

#include <mpi.h>

//#define debug printf // odkomentuj, aby zobaczyc komunikaty diagnostyczne
#define debug // odkomentuj, aby ukryc komunikaty diagnostyczne
#define die(msg, err) do { perror(msg); return err; } while(0)
#define master 0

#define IP 0
#define DATE 1
#define STATUS 2

#define MAP 0
#define REDUCE 1

int world_rank; /* identyfikator w komunikatorze globalnym */
int world_size; /* liczba procesow */

/* przetwarza argumenty wejście i zwraca tryb w jakim działa program */
void get_read_mode(int argc, char** argv, int* mode, char** filepath){

	int choice;
	*mode = -1;
	*filepath = NULL;

	while(1){
		static struct option long_options[] = {
			{"addr", no_argument, 0, 'a'},
			{"time", no_argument, 0, 'b'},
			{"stat", no_argument, 0, 'c'},
			{"file", required_argument, 0, 'd'},
			{0,0,0,0}
		};

		int option_index=0;
		choice = getopt_long(argc, argv, "abcd:", long_options, &option_index);

		if(choice == -1)
			break;

		switch(choice){
			case 'a':
				if(*mode != -1)
					break;
				printf("Tryb liczenia adresów\n");
				*mode = IP;
				break;
			case 'b':
				if(*mode != -1)
					break;
				printf("Tryb liczenia czasów\n");
				*mode = DATE;
				break;
			case 'c':
				if(*mode != -1)
					break;
				printf("Tryb liczenia statusów\n");
				*mode = STATUS;
				break;
			case 'd':
				if(*filepath != NULL)
					break;
				*filepath = optarg;		
				printf("Povieram dane z pliku %s\n", optarg);
				break;
			default:
				printf("Zły argument. Poprawna forma to --(addr|time|stat) --file <nazwa_pliku>. \n");
				MPI_Abort(MPI_COMM_WORLD, -1);
		}
	}

}

/* Liczy roznice w bajtach pomiedzy wartosciami w indexes */
void bytes(int size, int* indexes, int* count, int* skip, int* out_count_bytes, int* out_skip_bytes) {
	int i;
	/*			0123456789	*/
	/* text	= "oto tekst!" */
	/* indexes = [3, 9]		 */
	/* indexes zawiera indeksy koncow podciagow w text */
	for (i=0; i<world_size; i++) {
		if (i>0) 
			out_skip_bytes[i] = indexes[skip[i]-1];
		out_count_bytes[i] = indexes[skip[i]+count[i]-1];
	}

	for (i= --size; i>0; i--)
		out_count_bytes[i] -= out_count_bytes[i-1];
}

/* generowanie tablic count i skip dla MPI_Scatter/Gather na podstawie ilosci */
/* slow nwords i liczby procesow size */
void divide(int size, int nwords, char* words, int* out_count, int* out_skip) {
	int i, j;
	int part = nwords / size; /* calkowita czesc podzialu */
	int reminder = nwords % size; /* reszta podzialu */

	debug("part %d, reminder %d\n", part, reminder);
	for (i=0; i<size; i++) {
		out_count[i] = part;

		if (reminder-- > 0) /* jesli zostala reszta to dodaj ja do tego procesu */
			out_count[i]++;

		out_skip[i] = 0;
		/* dla count = [0, 2, 4, 5] => skip = [0, 0+2, 0+2+4, 0+2+4+5] */
		for (j=0; j<i; j++)
			out_skip[i] += out_count[j];
	}
}

void calculate_occurences(int nwords, char* words, int* indexes, int* occurs, int mode){

	int i,j;

	for(i = 0; i < nwords; i++){
		char* a = words+(i == 0 ? 0 : indexes[i-1]);
		if(*a == '\0')
			continue;

		if(mode == MAP)
			occurs[i] = 1;
	
		int len_a;
		if(i>0){
			len_a = indexes[i] - indexes[i-1];
		}else{
			len_a = indexes[i];
		}

		if(i == nwords -1)
			break;

		for(j = i+1; j < nwords; j++){
			char* b = words+indexes[j-1];
			if(*b == '\0')
				continue;

			int len_b;
			if(j>0){
				len_b = indexes[j] - indexes[j-1];
			}else{
				len_b = indexes[j];
			}

			if(len_a == len_b && (strncmp(a, b, len_a) == 0)){
				if(mode == REDUCE){
					occurs[i] += occurs[j];
				}else if(mode == MAP){
					occurs[i]++; 
				}
				*b = '\0';		
			}
		}
	}	
}

/* redukuje {kN: [v1, v2, v3 ...]} do {kN: sum([v1, v2, v3 ...])} */
void reduce(int nwords, char* words, int* indexes, int* occurs) {

	calculate_occurences(nwords, words, indexes, occurs, REDUCE);

}

/* mapuje slowa [k1, k2, k1, k3...] z words na [{k1: v1}, {k2: v2}, {k1: v3}, {k3: v4} ...] */
void map(int nwords, char* words, int* indexes, int* out_occurs) {
	int all_words = 0;
	int* count = NULL;
	int* count_bytes = NULL;
	int* skip = NULL;
	int* skip_bytes = NULL;
	int i, j;
	int nbytes = 0;
	int* adjusted_indexes = NULL;

	if (world_rank == master) {
		all_words = nwords;

		count = calloc(world_size, sizeof(int));
		count_bytes = calloc(world_size, sizeof(int));
		skip = calloc(world_size, sizeof(int));
		skip_bytes = calloc(world_size, sizeof(int));

		divide(world_size, nwords, words, count, skip);
		bytes(world_size, indexes, count, skip, count_bytes, skip_bytes);

		for (i=0; i<world_size; i++)
			debug("M count[%d] = %d, count_bytes[%d] = %d, skip[%d] = %d, skip_bytes[%d] = %d\n", i, count[i], i, count_bytes[i], i, skip[i], i, skip_bytes[i]);
		for (i=0; i<nwords; i++)
			debug("M indexes[%d] = %d\n", i, indexes[i]);

		adjusted_indexes = calloc(all_words, sizeof(int));
		memcpy(adjusted_indexes, indexes, all_words*sizeof(int));
		int indexes_sum = 0;
		int last_element = 0;
		for(i = 0; i < world_size; i++){
			for(j = 0; j < count[i]; j++){
				adjusted_indexes[j+indexes_sum] -= last_element;
			}
			indexes_sum += count[i];
			last_element += adjusted_indexes[count[i] - 1];
		}
	}

	/* nwords */
	MPI_Scatter(count, 1, MPI_INT, &nwords, 1, MPI_INT, master, MPI_COMM_WORLD);
	debug("[%d] Got %d words to map\n", world_rank, nwords);

	/* indexes */
	MPI_Scatter(count_bytes, 1, MPI_INT, &nbytes, 1, MPI_INT, master, MPI_COMM_WORLD);
	if (world_rank != master)
		words = calloc(nbytes, sizeof(char));
	debug("[%d] Got %d bytes to map\n", world_rank, nbytes);

	/* words */
	MPI_Scatterv(words, count_bytes, skip_bytes, MPI_CHAR, words, nbytes, MPI_CHAR, master, MPI_COMM_WORLD);
	debug("[%d] Got %.*s to map\n", world_rank, nbytes, words);

	/* indexes */
	int* partial_indexes = calloc(nwords, sizeof(int));
	MPI_Scatterv(adjusted_indexes, count, skip, MPI_INT, partial_indexes, nwords, MPI_INT, master, MPI_COMM_WORLD);

	/* calcuate occurences */
	int* occurs = calloc(nwords, sizeof(int));
	calculate_occurences(nwords, words, partial_indexes, occurs, MAP);

	/* words */
	MPI_Gatherv(words, nbytes, MPI_CHAR, words, count_bytes, skip_bytes, MPI_CHAR, master, MPI_COMM_WORLD);

	/* occurs */
	MPI_Gatherv(occurs, nwords, MPI_INT, out_occurs, count, skip, MPI_INT, master, MPI_COMM_WORLD);

	free(adjusted_indexes);
	free(occurs);
	free(partial_indexes);

	free(count);
	free(count_bytes);
	free(skip);
	free(skip_bytes);
}

int main(int argc, char** argv) {
	int i;
	int nwords = 0;
	int out_nwords = 0;
	char* words = NULL;
	int* indexes = NULL;
	int* out_occurs = NULL;
	int buffer_size = 1024;
	MPI_Init(NULL, NULL);
	MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);
	
	if (world_rank == master) {
		int bytes = 0;
		int len = 0;

		int read_mode;
		char* filepath;
		get_read_mode(argc, argv, &read_mode, &filepath);

		FILE* f = fopen(filepath,"r");
		words = calloc(buffer_size, sizeof(char)); /* "The last time I was"... */
		indexes = calloc(buffer_size, sizeof(int)); /* "3 8 13 15... */

		if (!f) die("Nie udalo sie otworzyc pliku", 1);


		/* pozbieraj slowa */
		ssize_t read;
		char* line = NULL;
		char* tmp_string = NULL;
		size_t line_len = 0;
		int found_string;
		while((read = getline(&line, &line_len, f)) != -1) {

			tmp_string = strtok(line, " ");
			i = 0;
			found_string = 0;
			while(tmp_string != NULL){
				switch(read_mode){
					case DATE:
						if(i == 3)
							found_string = 1;
							/* usuwa [ z początku */
							tmp_string++;

							/* usuwa sekundy */
							tmp_string[strlen(tmp_string) - 3] = '\0';
						break;
					case IP:
						if(i == 0)
							found_string = 1;
						break;
					case STATUS:
						if(strlen(tmp_string) != 3){
							break;
						}else if(i == 6 && *tmp_string != '/'){
							found_string = 1;
						}else if(i == 8)
							found_string = 1;
						break;
				}

				if(found_string)
					break;	
				
				tmp_string = strtok(NULL, " ");
				i++;
			}

			if(found_string){
				
				int tmp_len = strlen(tmp_string);

				if (bytes + tmp_len > buffer_size) {
					buffer_size *= 2;
					printf("Powiekszam bufor do %d bajtow.\n", buffer_size);
					words = realloc(words, buffer_size * sizeof(char));
					indexes = realloc(indexes, buffer_size * sizeof(int));
				}
				strcpy(words + bytes, tmp_string);
				bytes += tmp_len;
			
				indexes[nwords++] = bytes;
				continue;
			}
		}

		/* pokaż slowa */
		len = strlen(words);
		i = 0;
		int offset = 0;
		while(i < nwords) {
			debug("%.*s on position %d\n", indexes[i] - offset, words + offset, indexes[i]);
			offset = indexes[i];
			i++;
		}
		debug("Wczytano %d bajtow (%d slow)\n", bytes, nwords);
		out_occurs = calloc(nwords, sizeof(int));
	}

	if (world_rank == master)
		printf("Faza mapowania %d slow na %d procesorach\n", nwords, world_size);
	map(nwords, words, indexes, out_occurs);

	out_nwords = 0;
	if(world_rank == master){
		/* wynik mapowania */	
		for(i = 0; i < nwords; i++){
			int len_word = i == 0 ? indexes[i] : indexes[i] - indexes[i-1];
			int offset = i == 0 ? 0 : indexes[i-1];
			if(*(words+offset) == '\0')
				continue;
			printf("%.*s %d\n", len_word, words+offset, out_occurs[i]);
			out_nwords++;
		}

		printf("Faza redukcji %d kluczy na %d procesorach\n", out_nwords, world_size);

		reduce(nwords, words, indexes, out_occurs);
		
		/* wynik redukcji */	
		for(i = 0; i < nwords; i++){
			int len_word = i == 0 ? indexes[i] : indexes[i] - indexes[i-1];
			int offset = i == 0 ? 0 : indexes[i-1];
			if(*(words+offset) == '\0')
				continue;
			printf("%.*s %d\n", len_word, words+offset, out_occurs[i]);
			out_nwords++;
		}
	
	}

	free(indexes);
	free(out_occurs);
	free(words);
	MPI_Finalize();
	return 0;
}
