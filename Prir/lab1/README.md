Adam Kasperowicz - 279046
Piotr Zadrożny - 269080
# Programowanie równoległe i rozproszone - sprawozdanie z laboratorium 3
# Opis zadania i działania programu
Naszym zadaniem było napisanie programu, który sumuje elementy wektora zapisanego w pliku. Sumowanie, dzięki wykorzystaniu procesów potomnych (których liczba określana jest jako pierwszy argument w momencie wywołania programu), przebiega współbieżnie. Plik z danymi jest podawany jako drugi argument programu w formacie:
 - liczba danych w wektorze
 - liczba1
 - liczba2
 - ...
 
Cały kod źródłowy znajduje się w jednym pliku o nazwie "wektor.c". Po podaniu listy argumentów, na którą składają się  liczba, określająca liczbę potomków, a także plik z danymi, zaczyna się wykonanie programu. Po otwarciu pliku z danymi i podzieleniu wektora następuje sumowanie podwektorów, a następnie zliczenie ostatecznej sumy.

# Sprawdzenie wpływu zastosowania większej liczby procesów na szybkość wykonania programu
W ramach laboratorium przeprowadzony został również eksperymment, którego celem było sprawdzenie, jak liczba procesów wpływa na szybkość działania algorytmu. W celu przeprowadzenia badania utworzone zostały dwa dodatkowe pliki: "speed.c" oraz "gen.c". Zadaniem pliku "gen.c" jest generowanie pliku z tysiącem losowych liczb z zakresu <-100, 100>, zaś program "speed.c", po pobraniu pliku z danymi jako argument, wykonuje pomiary działania programu. Różne konfiguracje programu wywoływane są po sto razy, a czas ich wykonania jest uśredniany.
Poniżej przedstawione są wyniki eksperymentu.

n - ilość procesów
t - średnia ilość jednostek czasowych dla 100 iteracji

| n  | t   |
|----|-----|
| 1  | 117 |
| 2  | 126 |
| 4  | 132 |
| 6  | 145 |
| 8  | 160 |
| 16 | 175 |

# Wnioski
Z danych zawartych w tabeli można wywnioskować, że większa liczba procesów nie przyśpiesza, a nawet spowalnia szybkość działania tego algorytmu dla wektorów o rozmiarze równym tysiąc elementów. Prawdopodobnie, jest to spowodowane dużym kosztem działań na wspólnej pamięci, które muszą być wykonywane przez wszystkie podprocesy.
Należy jednak zwrócić uwagę na niewielki rozmiar wektora - dla większych zbiorów danych walory zrównoleglania z pewnością byłyby znacznie bardziej widoczne.

