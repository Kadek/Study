#include "wnioskowanie_interface.h"

void szukaj_dane_binarne(int *k,int *j,int n, dane_ptr *dane, char *bufer, reguly_ptr regula){

        int z;

        bufer[++(*k)] = '\0';
        for(z = 0; z < n; z++){
                if(strcmp(dane[z]->nazwa, bufer) == 0){
                        regula->lewa[(*j)++] = dane[z]->wartosc + '0';
                        *k = -1;
                        break;
                }
        }
}

void podstaw_binarne(reguly_ptr regula, dane_ptr *dane, int n){

        char bufer[BUF_SIZE];
        int i,j,k;

        i = 0;
        j = 0;
        k = -1;
        while(regula->lewa[i] != '\0'){
                switch(regula->lewa[i]){
                        case '(':
                                regula->lewa[j++] = '(';
                                break;
                        case ')':
                                if(k>=0){
                                        szukaj_dane_binarne(&k, &j, n, dane, bufer, regula);
                                }
                                regula->lewa[j++] = ')';
                                break;
                        case '~':
                                regula->lewa[j++] = '~';
                                break;
                        case '&':
                                if(k>=0){
                                        szukaj_dane_binarne(&k, &j, n, dane, bufer, regula);
                                }
                                regula->lewa[j++] = '&';
                                break;
                        case '|':
                                if(k>=0){
                                        szukaj_dane_binarne(&k, &j, n, dane, bufer, regula);
                                }
                                regula->lewa[j++] = '|';
                                break;
                        default:
                                bufer[++k] = regula->lewa[i];
                }
                i++;
        }

        regula->lewa[j] = '\0';
}

int znajdz_glebokosc(char *regula){
        int i, level, max;

        i = -1;
        max = 0;
        level = 0;
        while(regula[++i] != '\0'){
                if(regula[i] == '(')
                        level++;
                if(regula[i] == ')')
                        level--;
                if(max < level)
                        max = level;
        }

        return max;
}

char wartosc_nawiasu(char *regula, int j){
        int a, b;

        j++;
        /* dla znaku typu ~A zwróc zaprzeczenie wartości A jako char */
        if(regula[j] == '~')
                return !(regula[j+1] - '0') + '0';

        a = regula[j] - '0';

        while(regula[j+1] != ')'){
                b = regula[j+2] - '0';
                printf("%s %d\n", regula, b);
                if(regula[j+1] == '&')
                        a = (a && b) ;
                if(regula[j+1] == '|')
                        a = (a || b) ;
                j += 2;
        }

        return a + '0';
}

int wylicz_wartosc(char *regula){

        int i, j, d_count, k;
        int d = znajdz_glebokosc(regula);

        for(i = d; i>0; i--){
                k = 0;
                j = -1;
                d_count = 0;
                while(regula[++j] != '\0'){
                        if(regula[j] == '('){
                                d_count++;
                                if(d_count != i)
                                        regula[k++] = '(';
                                else
                                        regula[k++] = wartosc_nawiasu(regula, j);
                        } else if(regula[j] == ')'){
                                if(d_count != i)
                                        regula[k++] = ')';
                                d_count--;
                        } else {
                                if(d_count != i)
                                        regula[k++] = regula[j];
                        }
                }
                regula[k] = '\0';
                printf("%s\n", regula);
        }

        return regula[0] - '0';
}

void dodaj_dane(reguly_ptr mielona_regula, dane_ptr **dane, counter_ptr ilosc){
        int cnt = ilosc->dane_count;
        int siz = ilosc->dane_size;

	/*Jeśli wartość lewej strony wynosi 0 i prawa nie jest negowana, to nie dodawaj danej*/
	if((mielona_regula->wartosc != 1))
		return;
        if(cnt == siz){
                siz *= 2;
                *dane = extend_dane_mem(siz, cnt, *dane);
        }

	if(mielona_regula->neg_prawa)
		mielona_regula->wartosc = !(mielona_regula->wartosc); 
        strcpy((*dane)[cnt]->nazwa, mielona_regula->prawa);
        (*dane)[cnt]->wartosc = mielona_regula->wartosc;
        printf("Dodana dana to: %s jej wartosc to: %d\n", (*dane)[cnt]->nazwa, (*dane)[cnt]->wartosc);  

        ilosc->dane_count++;
        ilosc->dane_size = siz;
}
                     
int cel_dane(dane_ptr cel, dane_ptr *dane, counter_ptr ilosc){
        int i;

        for(i = 0; i< ilosc->dane_count; i++){
                if(strcmp(cel->nazwa, dane[i]->nazwa) == 0){
                        cel->wartosc = dane[i]->wartosc;
                        return 1;
                }
        }

        return 0;
}
 
