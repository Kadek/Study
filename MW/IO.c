#include "IO.h"

void przemiel_dane(char* bufer, dane_ptr *dane, int i){
	strcpy(dane[i]->nazwa,strtok(bufer, " "));
	strtok(NULL, " ");
	dane[i]->wartosc = atoi(strtok(NULL, " "));
}

void przemiel_reguly(char* bufer, reguly_ptr *reguly, int i){
	char *tmp = NULL;	

	strcpy(reguly[i]->lewa , strtok(bufer, " "));
	strtok(NULL, " ");
	
	tmp = strtok(NULL, " ");
	if(tmp[0] == '!'){
		tmp++;
		reguly[i]->neg_prawa = 1;
	}
	strcpy(reguly[i]->prawa , tmp);

	reguly[i]->prawa[strcspn(reguly[i]->prawa, "\r\n")] = 0;
}

void przemiel_cele(char *bufer, dane_ptr *cele, int i){	
	strcpy(cele[i]->nazwa , strtok(bufer, " "));
	cele[i]->nazwa[strcspn(cele[i]->nazwa, "\r\n")] = 0;
}

int wczytaj(char *input, dane_ptr **dane, reguly_ptr **reguly, dane_ptr **cele, counter_ptr ilosc){

	char bufer[BUF_SIZE];
	int i,n;

	FILE *in = fopen(input, "r");
	if(in == NULL)
		return 0;

	n = atoi(fgets(bufer, BUF_SIZE, in));
	ilosc->dane_count = n;
	ilosc->dane_size = n;
	create_dane_mem(n, dane);

	for(i = 0; i < n; i++){
		fgets(bufer, BUF_SIZE, in);
		przemiel_dane(bufer, *dane, i);
	}

	ilosc->reguly_count = atoi(fgets(bufer, BUF_SIZE, in));
	create_reguly_mem(ilosc->reguly_count, reguly);

	for(i = 0; i < ilosc->reguly_count; i++){
		fgets(bufer, BUF_SIZE, in);	
		przemiel_reguly(bufer, *reguly, i);
	}

	ilosc->cele_count = atoi(fgets(bufer, BUF_SIZE, in));
	create_dane_mem(ilosc->cele_count, cele);

	for(i = 0; i < ilosc->cele_count; i++){
		fgets(bufer, BUF_SIZE, in);	
		przemiel_cele(bufer, *cele, i);
	}
	
	return 1;
}

void wyswietl_wszystko(dane_ptr *dane, reguly_ptr *reguly, dane_ptr *cele, counter_ptr counter){
	int i;

	printf("-----------------------------------------------\n");
	printf("Ilosc danych: %d ilosc regul: %d\n\n", counter->dane_count, counter->reguly_count);
	for(i = 0; i < counter->dane_count; i++)
		printf("Nazwa danej: %s wartosc: %d\n", dane[i]->nazwa, dane[i]->wartosc);
	
	printf("\n");
	for(i = 0; i < counter->reguly_count; i++)
		printf("Nazwa lewa: %s nazwa prawej: %s wartosc: %d\n", reguly[i]->lewa, reguly[i]->prawa, reguly[i]->wartosc);

	printf(" \nIlosc celow: %d \n\n", counter->cele_count);
	for(i = 0; i < counter->cele_count; i++)
		printf("Nazwa celu: %s wartosc: %d\n", cele[i]->nazwa, cele[i]->wartosc);

	printf("-----------------------------------------------\n");
}
