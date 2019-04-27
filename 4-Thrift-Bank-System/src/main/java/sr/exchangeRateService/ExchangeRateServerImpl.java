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

        log.info("Start streaming exchange rates.");

        while(true) {

            // Use a builder to construct a new Protobuf object. Here we provide a custom business logic to increase amount by 1
            ExchangeRateCollection exchangeRateCollection = request
                    .getCurrencyList()
                    .stream()
                    .map(exchangeRateRepository::getExchangeRate)
                    .collect(ExchangeRateCollection::newBuilder, ExchangeRateCollection.Builder::addExchangeRate, (builder1, builder2) -> builder1.mergeFrom(builder2.build()))
                    .build();

            // Use responseObserver to send a single response back
            responseObserver.onNext(exchangeRateCollection);

            exchangeRateRepository.updateAll();

            try {
                Thread.sleep(REFRESH_TIME_MS);
            } catch (InterruptedException e) {
                log.error("Streaming sleep error: "  + e.getStackTrace());
            }

        }
        //log.info("End of streaming.");
        //responseObserver.onCompleted();
    }
}
