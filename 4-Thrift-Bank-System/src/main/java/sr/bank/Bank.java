package sr.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.middleware.proto.Currency;
import sr.middleware.proto.CurrencyCollection;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class Bank {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    private final String host;

    private final int port;

    private final CurrencyCollection currencyCollection;

    public static void main(String[] args){
        Bank bank = parseArgs(args);
        log.info("Bank has started" + bank.toString());
    }

    private static Bank parseArgs(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        String host = args[0];
        int port = Integer.valueOf(args[1]);

        CurrencyCollection currencyCollection = Arrays.stream(Arrays.copyOfRange(args, 2, args.length))
                .map(s -> Currency.valueOf(s.toUpperCase(Locale.ENGLISH)))
                .collect(CurrencyCollection::newBuilder, CurrencyCollection.Builder::addCurrency, (builder1, builder2) -> builder1.mergeFrom(builder2.build()))
                .build();

        return new Bank(host,port,currencyCollection);
    }

    public Bank(String host, int port, CurrencyCollection currencyCollection) {
        this.host = host;
        this.port = port;
        this.currencyCollection = currencyCollection;

        ExchangeRateService exchangeRateService = new ExchangeRateService(currencyCollection);
        new Thread(exchangeRateService).start();

        AccountService accountService = new AccountService();
        new Thread(accountService).start();
    }

    @Override
    public String toString() {
        return "\n  Address: " + host + ':' + port + "\n  " + currencyCollection.getCurrencyList().stream().map(Currency::toString).collect(Collectors.joining(", ", "Currencies: ",""));
    }
}
