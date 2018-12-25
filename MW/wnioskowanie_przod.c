#include "wnioskowanie_interface.h"

int sprawdz_dane(dane_ptr *dane, reguly_ptr regula, int n){
	char bufer[BUF_SIZE];
	char *delim = "()|&~";
	char *schowek;
	int i, flag;

	strcpy(bufer, regula->lewa);
	schowek = strtok(bufer, delim);

	while(schowek != NULL){
		flag = 0;

		for(i = 0; i < n ;i++){
			if(strcmp(dane[i]->nazwa, schowek) == 0){
				flag = 1;
			}
		}
		if(!flag){
			printf("Dla reguly %s => %s , %s<-nie znane\n", regula->lewa, regula->prawa, schowek);
			return 0;
		}

		schowek = strtok(NULL, delim);
	}
	if(regula->neg_prawa == 1)
		printf("Dla reguly %s => !%s dane znane", regula->lewa, regula->prawa);
	else
		printf("Dla reguly %s => %s dane znane", regula->lewa, regula->prawa);
		
	printf("\n");	

	return 1;
}

reguly_ptr znajdz_regule(dane_ptr *dane, reguly_ptr *reguly, counter_ptr ilosc){
	int i;

	for(i = 0; i<ilosc->reguly_count; i++){
		if(reguly[i]->wartosc == -1){
			if(sprawdz_dane(dane, reguly[i], ilosc->dane_count))
				return reguly[i];
		}
	}

	return NULL;
}

int rowna_celowi(reguly_ptr regula, dane_ptr cel){

	if(strcmp(regula->prawa, cel->nazwa) == 0){
		cel->wartosc = regula->wartosc;
		return 1;
	}

	return 0;
}

dane_ptr * wnioskowanie_przod(dane_ptr *dane, reguly_ptr *reguly, dane_ptr cel, counter_ptr ilosc){
	
	printf("----------------------------------------------\n");
	printf("Rozpoczynam wnioskowanie w przod.\n");	
	printf("----------------------------------------------\n");

	printf("cel -> %s\n", cel->nazwa);
	while(1){
		
		if(cel_dane(cel, dane, ilosc)){
			printf("----------------------------------------------\n");
			printf("Wnioskowanie w przod zakończone sukcesem.\n");
			printf("----------------------------------------------\n");
			return dane;
		}
		/* znajdz_regule znajduje pierwszą nieprzemieloną regułę 
		   dla której znamy wszystkie dane*/
		reguly_ptr mielona_regula = znajdz_regule(dane, reguly, ilosc);

		if(mielona_regula == NULL)
			break;

		podstaw_binarne(mielona_regula, dane, ilosc->dane_count);
		mielona_regula->wartosc = wylicz_wartosc(mielona_regula->lewa);
		dodaj_dane(mielona_regula, &dane, ilosc);
		
	}

	printf("----------------------------------------------\n");
	printf("Wnioskowanie w przod zakonczone.\n");	
	printf("----------------------------------------------\n");

	return dane;
}
