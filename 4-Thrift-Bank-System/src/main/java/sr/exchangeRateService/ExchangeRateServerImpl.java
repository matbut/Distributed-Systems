package sr.exchangeRateService;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.middleware.proto.*;

public class ExchangeRateServerImpl extends ExchangeRateServiceGrpc.ExchangeRateServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateServer.class);

    private static long REFRESH_TIME_MS = 5000;

    private final ExchangeRateRepository exchangeRateRepository = new ExchangeRateRepository();

    @Override
    public void getExchangeRateStream(CurrencyCollection request, StreamObserver<ExchangeRateCollection> responseObserver) {

        log.info("Start streaming exchange rates to new bank.");

        while(true) {

            ExchangeRateCollection exchangeRateCollection = request
                    .getCurrencyList()
                    .stream()
                    .map(exchangeRateRepository::getExchangeRate)
                    .collect(ExchangeRateCollection::newBuilder, ExchangeRateCollection.Builder::addExchangeRate, (builder1, builder2) -> builder1.mergeFrom(builder2.build()))
                    .build();

            responseObserver.onNext(exchangeRateCollection);

            exchangeRateRepository.updateAll();

            try {
                Thread.sleep(REFRESH_TIME_MS);
            }catch (InterruptedException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        //log.info("End of streaming.");
        //responseObserver.onCompleted();
    }
}
