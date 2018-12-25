 Adam Kasperowicz 279046  
 Piotr Zadrożny 269080
 # Laboratorium programowania równoległego i rozproszonego
  # Sprawozdanie z ćwiczenia 3
  
Naszym zadaniem było napisanie w języku Python programu, który umożliwia obliczenie iloczynu macierzy i wektora przy wykorzystaniu mechanizmów zrównoleglania udostępnionych przez ParallelPython. Podobnie jak w poprzednich ćwiczeniach, wykorzystany został zapis macierzy w pliku w postaci:
 - liczba wierszy
 - liczba kolumn
 - kolejny element macierzy
 
 Przykład:  
2  
3  
0.19  
0.84  
0.76  
0.933  
0.383  
0.16  

# Implementacja
Jako że zadanie, które przed nami zostało postawione jest podobne do poprzedniego, zdecydowano się dokonać jedynie modyfikacji uprzedniej implementacji. 
Kod znajduje się w pliku mul.py. Program uruchamia się poprzez komendę:
"python mul.py [plik z macierzą] [plik z macierzą] [liczba wątków wykorzystanych do mnożenia macierzy]

Aby użyć innych komputerów do obliczeń, należy wykonać następujące czynności:
1. Włączyć program ppserver.py za pomocą komendy './ppserver.py -s "secret"' na komputerze podłączonym do tej samej sieci, co komputer z którego wywoływołujemy program.
2. W programie mul.py należy zmienić liczbę procesorów wykorzystywanych przez ParallelPython na danym komputerze. Następuje to poprzez zmianę parametru 'ncpus' w konstruktorze klasy pp.Server. Ustawienie tej wartości na 1 gwarantuje użycie innych komputerów do dokonania obliczeń.
 
 # Wnioski
 Podczas testowania programu zauważyć można następujące właściwości tej metody uwspółbieżniania obliczeń:
- Dystrybucja zadań do innych komputerów pozwala na teoretycznie nieograniczone zwiększanie liczby jednostek obliczających, co oznacza wielki potencjał takiego skalowania programów. Dzięki temu mogą one działać na znacznie większych zbiorach danych w tym samym czasie.
- Czynnikiem znacznie spowalniającym działanie programu może być komunikacja między rozproszonymi procesami. Jeśli następuje ona często oraz każde połączenie zajmuje dużo czasu, system rozproszony będzie działać wolniej od jednego komputera, dlatego komunkację należy ograniczać do minimum.
- Ponadto, narzut częsci niesynchronizowalnej także może powodować wymierne spowolnienie działania. Na przykładzie załączonego programu widać, że działania mnożenia macierzy mogą być urównoleglane do pojedynczych elementów macierzy, jednak czynność sumowania wartości macierzy wynikowej musi być nadal wykonywana przez jeden proces.
- Podczas wykonywania eksperymentu znajduje potwierdzenie prawo Amdahla. Dla małych rozmiarów macierzy czas wykonania operacji jest praktycznie taki sam, niezależnie od liczby zastosowanych rdzeni i węzłów. Przyśpieszenie widoczne jest dopiero przy zwiększaniu ilości przetwarzanych danych.
