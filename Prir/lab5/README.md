Adam Kasperowicz 279046  
Piotr Zadrożny 269080
 # Programowanie równoległe i rozproszone - laboratorium
 # Zadanie 1
  #### Wstęp
 Naszym zadaniem było napisanie programu, który, korzystając ze schematu komunikacyjnego  "upowszechnianie", oblicza największy wspólny dzielnik liczb całkowitych przechowywanych lokalnie w różnych procesach. NWD jest wyliczane dla procesów przesuniętych o kolejne potęgi dwójki. 
  #### Opis implementacji
 Kod programu został w całości umieszczony w pliku o nazwie "circle.c". Kompilacja zostaje dokonana za pomocą komendy "mpicc circle.c -Wall -o circle". Aby uruchomić program, należy użyć polecenia "mpirun -np <liczba procesów> circle".
Wtedy w terminalu zostają wypisane informacje dotyczące komunikacji między procesami, dzięki którym można dowiedzieć się, który proces odbiera dane od którego i jaki jest największy wspólny dzielnik przechowywanych przez nie liczb.
 
 # Zadanie 2
  #### Wstęp
Schemat komunikacyjny "potokowe przesyłanie danych", sprawdza się przy wysyłaniu dużych danych, gdzie wysyłana wiadomość jest rozdzielana na n fragmentów. Naszym zadaniem było napisanie programu, który, korzysta z ww. techniki. Zastosowano jednowymiarową topologię komunikacyjną, gdzie każdy proces wysyła dane tylko do swojego prawego sąsiada. Gdy zostanie otrzymany fragment danych od lewego sąsiada, jest on przesyłany dalej do prawego sąsiada procesu-odbiorcy, wtedy natychmiast zaczyna się odbiór następnej porcji danych.

  #### Opis implementacji
 Kod programu został w całości umieszczony w pliku o nazwie "cart.c". Kompilacja zostaje dokonana komendą "mpicc cart.c -Wall -o cart".Aby uruchomić program, należy użyć polecenia "mpirun -np <liczba procesów> cart".
 Wektor służący do rozprzestrzenienia jest generowany losowo. Jego rozmiar wynosi 1000000, zaś pojedyncza przesyłana partycja zawiera 1000 elementów.
 Efektem działania każdego procesu jest plik, w którym zapisany jest rozprzestrzenieniony wektor. Plik nazywa się "numer procesu.txt ". W celu sprawdzenia poprawności działania programu użyta została komenda "diff --from-file *.txt" w folderze z plikami wyjściowymi programu. Pu użyciu ww. polecenia nie otrzymano wiadomości zwrotnej, co oznacza, że pliki wyjściowe są identyczne.

# Wnioski
 MPI pozwala na sprawne zaimplementowanie zaawansowanych wzorców komunikacyjnych. Dzięki jednoczesnej prostocie podstawowych funkcji, która umożliwa szybkie zrozumienie podstawowych zasad działań mechanizmów komunikacji wraz z bogactwem funkcji zaawansowanych pozwala na wykorzystanie bardziej abstrakcyjnych i skomplikowanych technik, takich jak topologie wirtualne.
