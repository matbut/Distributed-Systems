package sr.bank;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.middleware.proto.*;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;


public class ExchangeRateStorage implements Runnable {

    private static String SERVER_HOST = "localhost";

    private static int SERVER_PORT = 50001;

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateStorage.class);

    private Map<Currency, ExchangeRate> exchangeRates = new EnumMap<>(Currency.class);

    private Iterator<ExchangeRateCollection> exchangeRateIterator;

    public ExchangeRateStorage(CurrencyCollection currencyCollection) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext(true)
                .build();

        ExchangeRateServiceGrpc.ExchangeRateServiceBlockingStub serviceStub = ExchangeRateServiceGrpc.newBlockingStub(channel);
        exchangeRateIterator = serviceStub.getExchangeRateStream(currencyCollection);
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
        } catch (StatusRuntimeException ex) {
            log.warn("RPC failed:" + ex.getStatus());
        }
    }

    private String getPrintableExchangeRates(){
        return exchangeRates.values().stream()
                .map(er -> er.getCurrency() + ": buy: " + String.format("%8.5f", er.getBuy())  + ": sell: " + String.format("%8.5f", er.getSell()))
                .collect(Collectors.joining("\n  ", "Exchange rates: \n  ",""));
    }
}
