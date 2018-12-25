#ifndef WNIOSKOWANIE_INTERFACE_H
#define WNIOSKOWANIE_INTERFACE_H

#include "logic.h"
#include "IO.h"

dane_ptr * wnioskowanie_przod(dane_ptr *, reguly_ptr *, dane_ptr, counter_ptr);
dane_ptr * wnioskowanie_tyl_wrapper(dane_ptr *, reguly_ptr *, dane_ptr, counter_ptr);
void podstaw_binarne(reguly_ptr, dane_ptr *, int);
int wylicz_wartosc(char *);
void dodaj_dane(reguly_ptr, dane_ptr **, counter_ptr);
int cel_dane(dane_ptr, dane_ptr *, counter_ptr);
#endif
