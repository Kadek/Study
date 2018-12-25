main.o: main.c
	cc -g -Wall -pedantic -ansi -c main.c

IO.o: IO.c
	cc -g -Wall -pedantic -ansi -c IO.c

logic.o: logic.c
	cc -g -Wall -pedantic -ansi -c logic.c

wnioskowanie_przod.o: wnioskowanie_przod.c
	cc -g -Wall -pedantic -ansi -c wnioskowanie_przod.c

wnioskowanie_tyl.o: wnioskowanie_tyl.c
	cc -g -Wall -pedantic -ansi -c wnioskowanie_tyl.c

wnioskowanie_interface.o: wnioskowanie_interface.c
	cc -g -Wall -pedantic -ansi -c wnioskowanie_interface.c

create: main.o IO.o logic.o wnioskowanie_przod.o wnioskowanie_tyl.o wnioskowanie_interface.o
	cc *.o -o exek

test1:
	./exek -i dane1.txt -t p

test1.1:
	./exek -i dane1.txt -t t

test2:
	./exek -i dane2.txt -t p

test2.1:
	./exek -i dane2.txt -t t

test3:
	./exek -i dane3.txt -t p

test3.1:
	./exek -i dane3.txt -t t

clean:
	rm *.o exek 
