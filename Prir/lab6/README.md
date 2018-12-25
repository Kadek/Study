Adam Kasperowicz 279046  
Piotr Zadrożny 269080

 # Programowanie równoległe i rozproszone - laboratorium
  ### Wstęp
Naszym zadaniem było napisanie programu, który przetwarza pliki z logami, gdzie zostają ślady działalności każdej usługi działającej w systemie. Dzięki analizie tych danych administratorzy mogą  rozpoznawać przyczyny błędów czy też przewidywać zagrożenia. Pliki te z upływem czasu stają się bardzo długie i wtedy analiza wymaga przetworzenia olbrzymiej ilości danych, co dzięki zastosowaniu obliczeń równoległych możemy osiągnąć w satysfakcjonującym czasie.

 ### Opis implementacji
Cały kod programu znajduje się w pliku o nazwie "main.c". Aby umożliwić analizę nawet bardzo długiego pliku z logami, skorzystano z idei MapReduce przy pomocy MPI, co oznacza, że w czasie wykonania program realizuje dwie fazy: map oraz reduce. Aby skompliować program, należy użyć polecenia "mpicc main.c -o main" (-Wall jest pomijane, gdyż niekatywna funkcja debug powoduje wysyp warning'ów). W celu uruchomienia programu, należy wpisać komendę "mpirun -np <ilośc procesów> main --(addr|time|stat) -- file <nazwa_pliku>". Komenda --(addr|time|stat) umożliwia zliczanie odpowiednio według adresów IP, czasów oraz statusów.

 ### Dodawanie dodatkowych opcji
Aby zbadać inny parametru logu, należy dokonać następujących czynności:
1. Zdefiniować makro opisujące dany parametr(np. #define PARAM 4).
2. Dodać obsługę nowego parametru w funkcji get_read_mode().
3. Zaimplementować mechanizm wykrywający dany parametr w wejściowym łańcuchu znaków. Mechanizm należy umieścić w switchu pod komentarzem /* pozbieraj słowa */ w funkcji main.

 ### Różnice od kodu przykładowego z ISOD'a
Dokonana została znaczna ilość modyfikacji w stosunku do kodu przykładowego. 
1. Dodano funkcję get_read_mode(). Wykorzystuje ona biblitekę getopt.h służącą do wczytania parametrów wejściowych.
2. Wczytywany plik jest przetwarzany linia po linii. Dane słowa są wychwytywane funkcją strtok. Dodano switch, który pozwala w łatwy sposób dodawać nowe parametry do analizy.
3. W funkcji map zmieniona została metoda zliczania wystąpień danych słów. Funkcja get_occurences() zostaje wykorzystana przez każdy proces do wyliczenia wystąpień danych ciągów znaków w przydzielonym im przedziale.
4. Poszczególne przedziały ciągów znaków zostają złączone w całość. Funkcja reduce wywołuje funkcję get_occurences() dla całego przetworzonego przez podprocesy ciągu znaków.
5. Wyniki są wypisywane i zwalniana jest pamięć.

 ### Wnioski
Wyżej opisany program umożliwia podstawową, ale zautomatyzowaną analizę uproszczonego pliku z logami, co znacznie ułatwia pracę administratorowi i przekłada się na wymierne korzyści dla bezpieczeństwa całego systemu. Gdyby program nie został wykonany z wykorzystaniem idei programowania równoległego, nie działałby on w satysfakcjonujacym czasie, co udowadnia przydatność programowania współbieżnego w codziennej pracy informatyka.
