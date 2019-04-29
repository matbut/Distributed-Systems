package sr.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.bank.services.AccountService;
import sr.bank.services.ExchangeRateService;
import sr.bank.services.LoanService;
import sr.middleware.proto.Currency;
import sr.middleware.proto.CurrencyCollection;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class Bank {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    private final CurrencyCollection currencyCollection;

    public Bank(String exchangeRateServiceHost, int exchangeRateServicePort, String accountServiceHost, int accountServicePort, CurrencyCollection currencyCollection) {
        this.currencyCollection = currencyCollection;

        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateServiceHost, exchangeRateServicePort, currencyCollection);
        new Thread(exchangeRateService).start();

        LoanService loanService = new LoanService(exchangeRateService);

        AccountService accountService = new AccountService(accountServiceHost, accountServicePort, loanService);
        new Thread(accountService).start();
    }

    public static void main(String[] args){
        Bank bank = parseArgs(args);
        log.info("Bank has started" + bank.toString());
    }

    private static Bank parseArgs(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        String exchangeRateServiceHost = args[0];
        int exchangeRateServicePort = Integer.valueOf(args[1]);

        String accountServiceHost = args[2];
        int accountServicePort = Integer.valueOf(args[3]);

        CurrencyCollection currencyCollection = Arrays.stream(Arrays.copyOfRange(args, 4, args.length))
                .map(s -> Currency.valueOf(s.toUpperCase(Locale.ENGLISH)))
                .collect(CurrencyCollection::newBuilder, CurrencyCollection.Builder::addCurrency, (builder1, builder2) -> builder1.mergeFrom(builder2.build()))
                .build();

        return new Bank(exchangeRateServiceHost,exchangeRateServicePort,accountServiceHost,accountServicePort,currencyCollection);
    }



    @Override
    public String toString() {
        return "\n  " + currencyCollection.getCurrencyList().stream().map(Currency::toString).collect(Collectors.joining(", ", "Currencies: ",""));
    }
}
