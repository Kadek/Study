#ifndef IO_H
#define IO_H

#include <stdio.h>
#include <stdlib.h>
#include "logic.h"
#include <string.h>

#define BUF_SIZE 256
int wczytaj(char *, dane_ptr **, reguly_ptr **,dane_ptr **, counter_ptr);
void wyswietl_wszystko(dane_ptr *, reguly_ptr*, dane_ptr *, counter_ptr);
#endif
