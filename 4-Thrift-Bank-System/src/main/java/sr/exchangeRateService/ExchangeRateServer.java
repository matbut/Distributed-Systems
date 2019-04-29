package sr.exchangeRateService;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExchangeRateServer {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateServer.class);

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = Integer.valueOf(args[0]);

        Server server = ServerBuilder
                .forPort(port)
                .addService(new ExchangeRateServerImpl())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        log.info("Exchange Rate Server is listening on port " + port);

        server.awaitTermination();
    }
}
