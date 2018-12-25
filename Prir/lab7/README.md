Adam Kasperowicz 279046  
Piotr Zadrożny 269080

 # Programowanie równoległe i rozproszone - laboratorium
 # Sprawozdanie z ćwiczenia 7
  ### Wstęp
Naszym zadaniem było, na podstawie przykładowego kodu znajdującego się w ISOD, napisać program, który wyszukuje liczby pierwsze. Jednocześnie miał on przeprowadzać obliczenia równolegle, używając OpenMP. Zwiększenie szybkości działania algorytmu uzyskano poprzez wykorzystanie faktu, że do wyznaczania liczb pierwszych z przedziału B = [floor[sqrt(n)]+1..n] wystarczy znajomość liczb pierwszych z przedziału A = [2...floor[sqrt(n)]]. Każda liczba złożona należąca do przedziału B dzieli się przez jedną lub więcej liczb pierwszych z przedziału A. Aby sprawdzić czy dowolna liczba z przedziału B jest złożona, wystarczy sprawdzić, czy dzieli się bez reszty przez liczbę z przedziału A.
 ### Opis implementacji
Podobnie jak program przykładowy, na początku wyliczone zostają liczby  pierwsze z przedziału A = 2..floor[sqrt(n)] przy pomocy sita Eratostenesa. Wtedy następuje poszukiwanie liczb pierwszych w przedziale B = [floor[sqrt(n)]+1..n], ten etap jest wykonywany równolegle. Dowolna liczba j należąca do B jest liczbą pierwszą, jeśli nie dzieli się bez reszty przez żadną liczbę z przedziału A.

Kod znajduje się w pliku par.c. Kompilacja zostaje dokonana komendą "gcc -fopenmp -o par par.c". Program uruchamiamy komendą "./par". W celu wyznaczenia liczby wykorzystanych wątków, należy przed uruchomieniem programu ustawić odpowiednią zmienną środowiskową "export OMP_NUM_THREADS=<liczba wątków>".

Na wyjściu otrzymujemy czas działania całego programu, czas działania częsci urównoleglonej oraz ilość wyliczonych liczb pierwszych. Liczby pierwsze są zapisywane do pliku "primes.txt"

### Eksperyment

Jako że prir pozwalał wykonywać program tylko na jednym wątku, doświadczenie zostało wykonane na komputerze studenta. Mała ilość pamięci niestety nie pozwala przetestować działania programu dla N=10^7. Poniżej przedstawiono statystyki czasu działania programu dla różnej liczby wątków.

Dla N = 10^6 
| n wątków | T całkowity | T części równoległej | T częsci sekwencyjnej | Speedup | Efficency|
|---|-------|-------|-------|-------------|-------------| 
| 1 | 0,358 | 0,32  | 0,038 | 1           | 1           |
| 2 | 0,231 | 0,2   | 0,031 | 1,54978355  | 0,774891775 |
| 3 | 0,211 | 0,173 | 0,038 | 1,696682464 | 0,565560821 |
| 4 | 0,386 | 0,352 | 0,034 | 0,92746114  | 0,231865285 |
| 5 | 0,38  | 0,344 | 0,036 | 0,942105263 | 0,188421053 |
| 6 | 0,365 | 0,326 | 0,039 | 0,980821918 | 0,16347032  |
| 7 | 0,378 | 0,343 | 0,035 | 0,947089947 | 0,135298564 |
| 8 | 0,385 | 0,348 | 0,037 | 0,92987013  | 0,116233766 |



Dla N = 10^5 
| n wątków | T całkowity | T części równoległej | T częsci sekwencyjnej | Speedup | Efficency| 
|---|-------|-------|-------------|-------------|------|
| 1 | 0,027 | 0,02  | 0,007 | 1           | 1           |
| 2 | 0,032 | 0,027 | 0,005 | 0,84375     | 0,421875    |
| 3 | 0,043 | 0,038 | 0,005 | 0,627906977 | 0,209302326 |
| 4 | 0,05  | 0,045 | 0,005 | 0,54        | 0,135       |
| 5 | 0,047 | 0,042 | 0,005 | 0,574468085 | 0,114893617 |
| 6 | 0,051 | 0,046 | 0,005 | 0,529411765 | 0,088235294 |
| 7 | 0,056 | 0,049 | 0,007 | 0,482142857 | 0,068877551 |
| 8 | 0,072 | 0,067 | 0,005 | 0,375       | 0,046875    |



 ### Wnioski
 
Z wyżej widocznej tabeli wynika, że większa ilość danych pozwala na większe przyśpieszenie. Efektywność jednak zmniejsza się. Z jednej strony jest to spowodowane dostępem do tylko dwóch fizycznych rdzeni, a z drugiej świadczy to niewielkiej podatności problemu na urównoleglenie.
