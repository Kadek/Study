Adam Kasperowicz 279046  
Piotr Zadrożny 269080

# Programowanie równoległe i rozproszone
Sprawozdanie z ćwiczenia 4  

# Wstęp  
Naszym zadaniem było napisanie programu, który wykonuje całkowanie numeryczne wybranym przez nas sposobem spośród metod prostokątów, trapezów i Simpsona. Obliczenia powinny być wykonywane równolegle przy użyciu standardu MPI. 

# Opis implementacji  
Zdecydowaliśmy się na zastosowanie metody prostokątów. Proces główny rozdziela przedział całkowania na podprzedziały oraz liczbę punktów, w których będą wykonywane obliczenia pomiędzy siebie i pozostałe procesy. Po wykonaniu rachunków wynik zostaje wypisany na ekran. Dodatkowo, użytkownik dowiaduje się, ile czasu zajął każdy proces, oraz na jakim komputerze został wykonany. Zgodnie z zaleceniami prowadzącego, program pozwala na ustawienie początku i końca przedziału całkowania oraz całkowitej liczby punktów. 

Kod znajduje się w pliku "integrate.c". Do skompilowania kodu wykorzystana zostaje komenda "mpicc integrate.c -Wall -lm -o integrate". Aby uruchomić program, należy wykorzystać polecenie:  
mpirun -np <liczba procesów> -host <adresy węzłów obliczeniowych oddzielone przecinkiem> ./integrate <lewa strona przedziału całkowania> <prawa strona przedziału całkowania> <liczba punktów>.

# Eksperyment
Testowaliśmy funkcję y=sin(x) na przedziale (0,3.14) z liczbą punktów równą 999999999. Obliczenia były wykonywane jednocześnie na komputerach prir i prir2. Poniższa tabela przedstawia zależność wykorzystanej liczby procesów do średniego czasu obliczeń dla jednego procesu.  

| liczba procesów | czas wykonania w sekundach|
| ------ | ------ |
| 1 | 50.03 |
| 2 | 26.98 |
| 3 | 23,39 |
| 4 | 13,26 |
| 8 | 6,50 |
| 16 | 3,24 |

Dla wszystkich pomiarów, wynik był równy 1.999999, a więc dokładność obliczeń stała na zadowalającym poziomie  

# Wnioski  
Z powyższej tabeli wynika, że wraz z większą ilością procesów zmniejszał się czas ich wykonywania. W większości przypadków nie było to jednak spowodowane efektami zrównoleglania, lecz proporcjonalnie mniejszą ilością punktów wyliczanych przez każdy proces. Rzeczywiste skrócenie czasu działania całego programu zauważyć można było przy przejściu z jednego procesu do dwóch. Wykorzystanie dwóch maszyn rzeczywiście przyśpieszyło działanie programu prawie dwukrotnie. Dalsze zwiększanie ilości procesów skutkowało przyśpieszaniem działania programu, lecz zmiany te były nieznaczne.
