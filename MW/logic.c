#include "logic.h"

#define BUF_SIZE 256

void create_dane_mem(int n, dane_ptr **dane){
	
	int i;
	*dane = malloc(n * sizeof(dane_ptr));

	for(i = 0; i < n; i++){
		(*dane)[i] = malloc(sizeof(dane));
		(*dane)[i]->nazwa = malloc(BUF_SIZE * sizeof(char));
		(*dane)[i]->wartosc = -1;
	}

}

void create_reguly_mem(int n, reguly_ptr **reguly){
	
	int i;
	*reguly = malloc(n * sizeof(reguly_ptr));

	for(i = 0; i < n; i++){
		(*reguly)[i] = malloc(sizeof(reguly));
		(*reguly)[i]->lewa = malloc(BUF_SIZE * sizeof(char));
		(*reguly)[i]->prawa = malloc(BUF_SIZE * sizeof(char));
		(*reguly)[i]->wartosc = -1;
		(*reguly)[i]->neg_prawa = 0;
	}

}

void free_dane_mem(int n, dane_ptr *dane){
	
	int i;
	for(i = 0; i < n; i++){
		free(dane[i]->nazwa);
		free(dane[i]);
	}

	free(dane);
}

void free_reguly_mem(int n, reguly_ptr *reguly){
	
	int i;
	for(i = 0; i < n; i++){
		free(reguly[i]->lewa);
		free(reguly[i]->prawa);
		free(reguly[i]);
	}

	free(reguly);
}

dane_ptr * extend_dane_mem(int siz, int cnt, dane_ptr *dane){

	int i;

	dane_ptr *tmp = malloc(siz * sizeof(dane_ptr));	
	for(i = 0; i < siz; i++){
		tmp[i] = malloc(sizeof(dane));
		tmp[i]->nazwa = malloc(BUF_SIZE * sizeof(char));	
		tmp[i]->wartosc = -1;
	}	

	for(i = 0; i < cnt; i++){
		strcpy(tmp[i]->nazwa, dane[i]->nazwa);
		tmp[i]->wartosc = dane[i]->wartosc;
		free(dane[i]->nazwa);
		free(dane[i]);
	}
	
	free(dane);
	
	return tmp;	
}
