# bezpieczna_strona

Bardzo fajna, bezpieczna stronka. 
Wszystkie funkcjonalności są spisane w "wymagania".
Generalnie projekt do nauki więc wszystko jest nieuporządkowane i w jednym pliku, zgodnie
z security-by-obscurity.

Trochę taki MVC:

	View - templates i js zajmują się tylko kosmetyką i nigdy nie autoryzują ani nic.
	
	Controller - web/bezpieczna_strona.py kłębek dziwacznych route'ów i wywołań funkcji z bezpiecznych_narzędzi.
	
	Model - bezpieczne_narzedzia.py, murzyn strony, zajmuje się całą robotą

Apache jest standardowo ustawiony na port 1234 a UWSGI na 9090
