package sr.bank.services;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.middleware.proto.*;
import sr.middleware.slice.NotSupportedCurrencyException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;


public class ExchangeRateService implements Runnable {

    private static final int RESPONSE_DELAY = 5000;

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private static final ExchangeRate PLN_EXCHANGE_RATE = ExchangeRate
            .newBuilder()
            .setCurrency(Currency.PLN)
            .setBuy(1.0)
            .setSell(1.0)
            .build();

    private Map<Currency, ExchangeRate> exchangeRates = Collections.synchronizedMap(new EnumMap<>(Currency.class));

    private Iterator<ExchangeRateCollection> exchangeRateIterator;

    public ExchangeRateService(String host, int port, CurrencyCollection currencyCollection) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();

        ExchangeRateServiceGrpc.ExchangeRateServiceBlockingStub serviceStub = ExchangeRateServiceGrpc.newBlockingStub(channel);
        exchangeRateIterator = serviceStub.getExchangeRateStream(currencyCollection);

        log.info("Account Service is listening on "+host+":"+port);
    }

    public ExchangeRate getExchangeRate(Currency currency) throws NotSupportedCurrencyException {
        if (currency.equals(Currency.PLN))
            return PLN_EXCHANGE_RATE;

        if (!exchangeRates.containsKey(currency))
            throw new NotSupportedCurrencyException();

        try {
            Thread.sleep(RESPONSE_DELAY);
        }catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return exchangeRates.get(currency);
    }

    @Override
    public void run() {
        try {
            exchangeRateIterator
                    .forEachRemaining((rateCollection) -> {
                        rateCollection.getExchangeRateList()
                                .forEach((rate) -> exchangeRates.put(rate.getCurrency(), rate));
                        log.info(getPrintableExchangeRates());
                    });
        } catch (StatusRuntimeException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getPrintableExchangeRates(){
        return exchangeRates.values().stream()
                .map(er -> er.getCurrency() + ": buy: " + String.format("%8.5f", er.getBuy())  + ": sell: " + String.format("%8.5f", er.getSell()))
                .collect(Collectors.joining("\n  ", "Exchange rates: \n  ",""));
    }
}
