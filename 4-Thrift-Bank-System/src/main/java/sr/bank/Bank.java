package sr.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.middleware.proto.Currency;
import sr.middleware.proto.CurrencyCollection;
import sr.middleware.proto.ExchangeRateCollection;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static sr.middleware.proto.Currency.*;

public class Bank {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    private final String host;

    private final int port;

    private final CurrencyCollection currencyCollection;

    public static void main(String[] args){
        Bank bank = parseArgs(args);
        log.info("Bank started");
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

        ExchangeRateStorage exchangeRateStorage = new ExchangeRateStorage(currencyCollection);
        new Thread(exchangeRateStorage).start();
    }
}
