#include "wnioskowanie_interface.h"

#define REC_LIMIT 10

char *znajdz_brak(reguly_ptr regula, dane_ptr *dane, counter_ptr ilosc){

	char bufer[BUF_SIZE];
	char *delim = "()|&~";
	char *schowek;
	int i, flag;

	strcpy(bufer, regula->lewa);
	schowek = strtok(bufer, delim);

	while(schowek != NULL){
		flag = 0;

		for(i = 0; i < ilosc->dane_count ;i++){
			if(strcmp(dane[i]->nazwa, schowek) == 0){
				flag = 1;
				break;
			}
		}
		if(!flag){
			printf("Dla reguly %s => %s , %s<-nie znane\n", regula->lewa, regula->prawa, schowek);
			return schowek;
		}

		schowek = strtok(NULL, delim);
	}

	return NULL;
}

int uzupelnij(dane_ptr **dane, reguly_ptr regula, counter_ptr ilosc){

	podstaw_binarne(regula, *dane, ilosc->dane_count);
	regula->wartosc = wylicz_wartosc(regula->lewa);
	dodaj_dane(regula, dane, ilosc);

	return 0;
}

int wnioskowanie_tyl(dane_ptr **dane, reguly_ptr *reguly, reguly_ptr regula, counter_ptr ilosc, int rec_level, int max_rec_level){

	int i;
	char *brak = NULL;
	int flag;	

	printf("----------------------------------------------\n");
	printf("Wnioskowanie głębiej.\n");	
	printf("----------------------------------------------\n");

	printf("obrabiana regula: %s -> %s\n", regula->lewa, regula->prawa);

	brak = znajdz_brak(regula, *dane, ilosc);	
	while(brak != NULL){
		flag = 0;
		for(i = 0; i < ilosc->reguly_count; i++){
			if(!(strcmp(reguly[i]->prawa, brak) == 0))
				continue;
			if(((reguly[i]->wartosc < 0) && (rec_level < max_rec_level)) 
				&& wnioskowanie_tyl(dane, reguly, reguly[i],  ilosc, rec_level + 1, max_rec_level)){
				flag = 1;
				break;
			}
		}
		if(flag)
			brak = znajdz_brak(regula, *dane, ilosc);
		else
			brak = NULL;
	}

	if(znajdz_brak(regula, *dane, ilosc) == NULL){
		uzupelnij(dane, regula, ilosc);
		return 1;
	}

	printf("----------------------------------------------\n");
	printf("Powrót wniosku.\n");	
	printf("----------------------------------------------\n");

	return 0;
}

dane_ptr * wnioskowanie_tyl_wrapper(dane_ptr * dane, reguly_ptr * reguly, dane_ptr cel , counter_ptr ilosc){
	int i;
	int j = 0;

	printf("cel -> %s\n", cel->nazwa);	
	if(cel_dane(cel, dane, ilosc))
		return dane;
	while(1){
		for(i = 0; i< ilosc->reguly_count; i++){
			if(!(strcmp(reguly[i]->prawa, cel->nazwa) == 0)){
				continue;
			}
			printf("----------------------------------------------\n");
			printf("Rozpoczynam wnioskowanie w tył.\n");	
			printf("----------------------------------------------\n");
			wnioskowanie_tyl(&dane, reguly, reguly[i], ilosc, 0, j);
			printf("----------------------------------------------\n");
			printf("Wnioskowanie w tył zakończone.\n");	
			printf("----------------------------------------------\n");
		
			if(cel_dane(cel, dane, ilosc))
				return dane;
		}
		if(j++ > REC_LIMIT)
			return dane;
	}

	return dane;
}
