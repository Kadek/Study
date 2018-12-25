#ifndef LOGIC_H
#define LOGIC_H

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

typedef struct {
	char *nazwa;
	int wartosc;
} dane, *dane_ptr;

typedef struct {
	char *lewa;
	char *prawa;
	int wartosc;
	int neg_prawa;
} reguly, *reguly_ptr;

typedef struct {
	int dane_count;
	int dane_size;
	int reguly_count;
	int cele_count;
} counter, *counter_ptr;

void create_dane_mem(int , dane_ptr **);
void free_dane_mem(int , dane_ptr *);
void create_reguly_mem(int , reguly_ptr **);
void free_reguly_mem(int , reguly_ptr *);
void create_cele_mem(int, dane_ptr **);
void free_cele_mem(int, dane_ptr *);
dane_ptr * extend_dane_mem(int, int, dane_ptr *);
#endif
