# Komunikacja grupowa
### Cel zadania:
Celem zadania jest implementacja rozproszonej tablicy haszującej. Aplikacje z niej korzystające powinny mieć możliwość dodawania elementów do tablicy i jednocześnie pobierania elementów wcześniej dodanych, być może również przez inne aplikacje.

W wyniku realizacji zadania powinna powstać implementacja klasy DistributedMap, implementująca interfejs:
```shell
public interface SimpleStringMap {
    boolean containsKey(String key);

    Integer get(String key);

    void put(String key, Integer value);

    Integer remove(String key);
}
```

Powinna też zostać opracowana przykładowa aplikacja korzystająca z rozproszonej tablicy haszującej. Funkcjonalność aplikacji powinna umożliwiać interaktywną interakcję i eksponować metody zawarte w interfejsie implementowanej tablicy.

### Własności rozproszonej tablicy:
Uwzględniając teorię [CAP](https://en.wikipedia.org/wiki/CAP_theorem), implementacja rozwiązania powinna cechować się dostępnością i tolerowaniem partycjonowania.

W związku z tym każda instancja klasy DistributedMap powinna mieć własną kopię tablicy rozproszonej, a uspójnianie stanu powinno być zrealizowana podczas operacji dodawania elementów do tablicy. Do rozproszonej komunikacji pomiędzy instancjami należy wykorzystać bibliotekę JGroups:
- w przypadku tworzenia nowej instancji klasy DistributedMap, powinna ona pozyskać początkowy stan od członków grupy, do której dołącza,
- w przypadku podziału grupy węzłów na dwie partycje, powinny one utrzymywać swój stan niezależnie; w przypadku ponownego scalania dwóch partycji, członkowie jednej z partycji powinni stracić swój stan i pozyskać go na nowo od członków drugiej z partycji.

### Dodatkowe informacje:
- [Tutorial opisujący mechanizm przesyłania stanu za pomocą biblioteki JGroups](http://www.jgroups.org/manual/index.html#StateTransfer)
- [Tutorial opisujący mechanizm łączenia partycji za pomocą biblioteki JGroups](http://www.jgroups.org/manual/index.html#HandlingNetworkPartitions)
- Ze względu na swój rozproszony charakter, aplikacja będzie testowana w laboratorium, i w sposób naturalny będzie wchodzić w interakcję z aplikacjami innych studentów, dlatego warto rozważyć modyfikację domyślnego adresu grupowego wykorzystywanego przez protokół UDP:
```shell
new UDP().setValue("mcast_group_addr",InetAddress.getByName("230.100.200.x"))
```
