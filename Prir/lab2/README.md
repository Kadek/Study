Adam Kasperowicz - 279046
Piotr Zadrożny - 269080
# Laboratorium programowania rozproszonego i równoległego
# Sprawozdanie z ćwiczenia 2
Naszym zadaniem była implementacja programu, który wykonuje mnożenie dwóch macierzy oraz wylicza normę Frobeniusa, tzn. pierwiastek z sumy kwadratów elementów macierzy. Zadanie zostało wykonane w dwóch wersjach: w języcku C oraz Python.
Obie macierze są w plikach, które należy podawać jako argumenty wywołania programu. Pliki, w których podane są macierze wyglądają następująco:
 - liczba wierszy
 - liczba kolumn
 - macierz, z zachowaniem jej 'oryginalnego' wyglądu

Przykład:
 5
 3
0.19 0.16 0.62
0.84 0.20 0.94
0.76 0.9383 1.3
0.933 0.28384 2.384
0.383 0.33 0.9 

# Implementacja w języku C
Kod w C znajduje się w pliku mul.c. Program jest kompilowany komendą "gcc mul.c -o mul -lpthread -lm -pedantic -Wall -ansi -Wextra". Uruchomienie programu następuje poprzez wywołanie komendy "./mul [plik z macierzą] [plik z macierzą] [liczba wątków wykorzystanych do mnożenia macierzy] [liczba wątków wykorzystanych do wyliczenia normy Froebiusa]".

# Implementacja w języku Python
Kod o tym samym działaniu znajduje się w pliku mul.py. Ten program zostaje uruchamiany poprzez komendę "python mul.py [plik z macierzą] [plik z macierzą] [liczba wątków wykorzystanych do mnożenia macierzy] [liczba wątków wykorzystanych do wyliczenia normy Froebiusa]".

# Wnioski
Po zaimplementowaniu odpowiednich programów przeprowadzono eksperyment, którego celem było sprawdzenie, jak wpływa liczba wątków, którą utworzono na potrzeby działania programu. Na jego podstawie można zauważyć trend potwierdzający oczekiwania, że większa liczba wątków przekłada się na szybsze działanie programu, jednak trend ten załamuje się dla dużej liczby wątków i następuje spadek szybkości działania programu.