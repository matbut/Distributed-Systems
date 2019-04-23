package sr.bank;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.middleware.proto.*;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExchangeRateStorage implements Runnable {

    private static String SERVER_HOST = "localhost";

    private static int SERVER_PORT = 50001;

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateStorage.class);

    private Map<Currency, ExchangeRate> exchangeRates = new EnumMap<Currency, ExchangeRate>(Currency.class);

    private ExchangeRateServiceGrpc.ExchangeRateServiceBlockingStub currencyServiceBlockingStub;

    private final CurrencyCollection currencyCollection;

    public ExchangeRateStorage(CurrencyCollection currencyCollection) {
        this.currencyCollection = currencyCollection;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext(true)
                .build();
        currencyServiceBlockingStub = ExchangeRateServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void run() {
        Iterator<ExchangeRateCollection> currencyStatusCollectionIterator;
        try {
            currencyStatusCollectionIterator = currencyServiceBlockingStub.getExchangeRateStream(currencyCollection);
            while (currencyStatusCollectionIterator.hasNext()) {
                ExchangeRateCollection currencyStatusCollection = currencyStatusCollectionIterator.next();
                List<ExchangeRate> list = currencyStatusCollection.getExchangeRateList();

                for (ExchangeRate currencyStatus : list) {
                    exchangeRates.put(currencyStatus.getCurrency(),currencyStatus);
                    log.info(currencyStatus.getCurrency() + ", buy: "+ currencyStatus.getBuy() +", sell: "+ currencyStatus.getSell());
                }
            }
        } catch (StatusRuntimeException ex) {
            log.warn("RPC failed:" + ex.getStatus());
        }
    }
}
