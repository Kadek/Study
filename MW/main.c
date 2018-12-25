#include<getopt.h>

#include "IO.h"
#include "wnioskowanie_interface.h"

int main(int argc, char **argv){

	system("clear");

	dane_ptr *dane = NULL;
	reguly_ptr *reguly = NULL;
	dane_ptr *cele = NULL;

	char *input = NULL;
	int tryb_flag = 0;
	int show_flag = 0;

	int c,i;
	counter_ptr ilosc = malloc(sizeof(counter));
	ilosc->dane_count = 0;
	ilosc->reguly_count = 0;
	ilosc->dane_size = 0;
	ilosc->cele_count = 0;

	while((c = getopt(argc, argv, "i:t:s")) != -1){
		switch(c){
			case 'i':
				input = optarg;
				break;
			case 't':
				switch(optarg[0]){
					case 'p':
						tryb_flag = 1;
						break;
					case 't':
						tryb_flag = 2;
						break;
				}
				break;
			case 's':
				show_flag = 1;
		}
	}

	if((input == NULL) || (tryb_flag == 0)){
		printf("Brak danych wejściowych\n");
		return EXIT_FAILURE;
	}
		
	if(!wczytaj(input, &dane, &reguly, &cele, ilosc)){
		printf("Nie udało się wczytać pliku");
		return EXIT_FAILURE;
	}

	if(show_flag){
		printf("dane przed wnioskowaniem:\n");
		wyswietl_wszystko(dane, reguly, cele, ilosc);
	}
	
	for(i = 0; i<ilosc->cele_count; i++){	
		if(tryb_flag == 1){
			dane = wnioskowanie_przod(dane, reguly, cele[i], ilosc);
		}else if(tryb_flag == 2){	
			dane = wnioskowanie_tyl_wrapper(dane, reguly, cele[i], ilosc);
		}else
			printf("Coś nie tak z trybami\n");

		if(cele[i]->wartosc == -1)
			printf("Niemożliwe do wywnioskowania.\n");		
	}	


	if(show_flag){
		printf("dane po wnioskowaniu:\n");
		wyswietl_wszystko(dane, reguly, cele, ilosc);
	}
	free_dane_mem(ilosc->dane_size, dane);
	free_reguly_mem(ilosc->reguly_count, reguly);
	free_dane_mem(ilosc->cele_count, cele);
	free(ilosc);

	return 0;
}
