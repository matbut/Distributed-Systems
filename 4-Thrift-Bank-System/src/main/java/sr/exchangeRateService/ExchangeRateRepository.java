package sr.exchangeRateService;

import sr.middleware.proto.*;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class ExchangeRateRepository {

    private static final double MIN_EXCHANGE_RATE = 0.1;

    private static final double MAX_EXCHANGE_RATE = 10.0;

    private static final double MIN_UPDATE_RATIO  = 0.8;

    private static final double MAX_UPDATE_RATIO = 1.2;

    private static final double EXCHANGE_DIFF = 1.1;

    private final Map<Currency, ExchangeRate> repository = new EnumMap<Currency, ExchangeRate>(Currency.class) {
        {
            Arrays.stream(Currency.values())
                    .filter(currency -> currency != Currency.UNRECOGNIZED)
                    .forEach(currency -> put(currency, randomExchangeRate(currency)));
        }
    };

    public ExchangeRate getExchangeRate(Currency currency) {
        return repository.get(currency);
    }

    public void updateAll() {
        repository.forEach(((currency, exchangeRate) -> {
            repository.replace(currency, randomUpdate(exchangeRate));
        }));
    }

    private ExchangeRate randomExchangeRate(Currency currency) {
        double buyRate = MIN_EXCHANGE_RATE + Math.random() * (MAX_EXCHANGE_RATE - MIN_EXCHANGE_RATE);
        return ExchangeRate
                .newBuilder()
                .setCurrency(currency)
                .setBuy(buyRate)
                .setSell(buyRate*EXCHANGE_DIFF)
                .build();
    }

    private ExchangeRate randomUpdate(ExchangeRate exchangeRate) {
        double buyRatio = MIN_UPDATE_RATIO + Math.random() * (MAX_UPDATE_RATIO - MIN_UPDATE_RATIO);
        double buyRate = exchangeRate.getBuy()*buyRatio;

        return ExchangeRate
                .newBuilder(exchangeRate)
                .setBuy(buyRate)
                .setSell(buyRate*EXCHANGE_DIFF)
                .build();
    }
}
