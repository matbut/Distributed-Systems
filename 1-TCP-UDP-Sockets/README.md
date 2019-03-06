# Gniazda TCP/UDP
Celem zadania jest napisanie aplikacji w języku C lub C++, która pozwoli użytkownikom na przesyłanie (nadawanie i wyświetlanie) informacji bez wykorzystania serwera centralnego poprzez logiczną symulację architektury token ring. 

### Parametry:
Każdy klient podczas uruchomienia otrzymuje cztery argumenty:
- tekstowy identyfikator użytkownika,
- adres IP i port sąsiada, do którego przekazywane będą wiadomości,
- informacja o tym, czy dany użytkownik po uruchomieniu posiada token,
- wybrany protokół: tcp lub udp.

### Opis działania:
Wiadomości przekazywane są tylko w jedną stronę. W sieci znajduje się tylko jeden token i żadna aplikacja nie może nadawać dopóki nie otrzyma wolnego tokenu, pierwotnie token jest wolny. Wysłanie wiadomości polega na zajęciu tokenu i wpisaniu jej zawartości. Token traktujemy jako kopertę, nośnik wiadomości. Odbiorca po przeczytaniu wiadomości zwalnia token (flaga, wyczyszczenie zawartości...) i przekazuje go dalej. Dla celów symulacyjnych przyjmujemy, że token jest przetrzymywany przez każdego klienta przez około 1 sekundę (po otrzymaniu tokenu wywołujemy np. sleep(1000), po tym czasie przesyłamy go dalej po ewentualnym dodaniu wiadomości). Dla uproszczenia zakładamy, że żaden klient nie będzie "złośliwy" i nie doprowadzi do sytuacji, w której w sieci znajdą się dwa tokeny - jednak za implementację mechanizmu, który to wyklucza, zostanie przyznany bonus punktowy. Program ma umożliwiać dodawanie nowych użytkowników w trakcie działania systemu oraz zapewniać dla nich pełną funkcjonalność, a także zabezpieczać przed sytuajcą, w której wiadomość krąży w nieskończoność w sieci (należy odpowiednio przemyśleć protokół komunikacyjny). Dodatkowo, każdy klient ma przesyłać multicastem informację o otrzymaniu tokenu (na dowolny adres grupowy, wspólny dla wszystkich klientów - może być wpisany "na sztywno" w kod). Odbiorcami grupy multicastowej są wyłącznie loggery - proste aplikacje zapisujące ID nadawcy i timestamp otrzymania tokenu, do pliku. Ilość loggerów może być dowolna (co najmniej 2). Logger należy zaimplementować w języku innym niż klientów. 

### Punktacja:
- Implementacja klientów - wersja TCP: 2pkt
- Implementacja klientów - wersja UDP: 1pkt
- Mechanizm dołączania nowych klientów: 2pkt
- Implementacja loggera i mechanizmu logowania: 1 pkt
- Poprawna obsługa gniazd i protokołu TCP: 0,5pkt
- Poprawna obsługa gniazd i protokołu UDP: 0,5pkt
- Zagwarantowanie braku sytuacji zagłodzenia klientów: 1pkt
- Odpowiedź ustna przy oddawaniu programu: [-4; 4]pkt

Za zadanie można otrzymać minimalnie 0, a maksymalnie 10 punktów. Otrzymanie niezerowej ilości punktów za podpunkty 3-7 wymaga wykonania podpunktu 1 lub 2. Pytania mogą dotyczyć kodu lub wiedzy podanej w wymaganiach do laboratorium (nie trzeba znać dokładnie sygnatur metod i funkcji, z których się korzysta, ale jeżeli korzysta się z jakiejś flagi, to warto wiedzieć co ona z grubsza oznacza). Należy przesłać kod źródłowy wraz z skryptem kompilującym rozwiązanie (mile widziany cmake, ewentualnie make) w formie zgodnej z regulaminem przedmiotu.

### Uwagi:
- Nie wolno korzystać z frameworków do komunikacji sieciowej – tylko gniazda!
- Nie wolno też korzystać z Akka
- Brak rozwiązania na moodle = 0 punktów

### Przy oddawaniu należy:
- zademonstrować działanie aplikacji (min. 3 klientów)
- omówić kod źródłowy i odpowiedzieć na pytania
