#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "omp.h"

#define N 10000000
#define S (int)sqrt(N)
#define M N/10

long int podzielniki(long int* erastotes, long int* pierwsze){

	long i,k;
	long int llpier = 0; /*l. liczb pierwszych w tablicy pierwsze*/

	/*wyznaczanie podzielnikow z przedzialow 2..S*/
	for(i=2; i<=S; i++)
		erastotes[i] = 1; /*inicjowanie*/
	for(i=2; i<=S; i++)
		if(erastotes[i] == 1){
			pierwsze[llpier++] = i; /*zapamietanie podzielnika*/
			/*wykreslanie liczb zlozonych bedacych wielokrotnosciami i*/
		for(k = i+i; k<=S; k+=i) 
			erastotes[k] = 0;
	}

	return llpier;
}

long int pierwsze_gen(long int lpodz, long int* pierwsze){

	long k;
	long liczba, reszta;
	long int llpier = lpodz;

	/*wyznaczanie liczb pierwszych*/
	double time = omp_get_wtime();
	#pragma omp parallel for private(liczba, k, reszta)
	for(liczba=S+1; liczba <= N; liczba++){
		for(k=0; k<lpodz; k++){
			reszta = (liczba % pierwsze[k]);
			if(reszta == 0) 
				break; /*liczba zlozona*/
		}
		if(reszta != 0){
			#pragma omp critical
			pierwsze[llpier++] = liczba; /*zapamietanie liczby pierwszej*/
		}
	}
	printf("Czas działania części równoległej = %f sekund \n", omp_get_wtime() - time);

	return llpier;
}

int main(int argc, char**argv){

	double start = omp_get_wtime();

	long int erastotes[S + 1]; /*tablica pomocnicza*/
	long int pierwsze[M]; /*liczby pierwsze w przedziale 2..N*/
	long i;
	long int lpodz, llpier; /* l. podzielnikow*/
	FILE *fp;

	lpodz = podzielniki(erastotes, pierwsze); /*zapamietanie liczby podzielnikow*/

	llpier = pierwsze_gen(lpodz, pierwsze);

	if((fp = fopen("primes.txt", "w")) == NULL){
		printf("Nie moge otworzyc pliku do zapisu\n");
		exit(1);
	}
	printf("%d\n", llpier);
	for(i=0; i< llpier; i++)
		fprintf(fp,"%ld ", pierwsze[i]);
	fclose(fp);

	printf("Czas działania całego programu = %f sekund\n", omp_get_wtime() - start);

	return 0;
}
