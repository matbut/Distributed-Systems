package sr.bank;

import com.zeroc.Ice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.middleware.slice.*;

import java.lang.Exception;
import java.util.Random;
import java.util.stream.IntStream;

public class AccountService implements AccountFactory, Runnable {

    private static final long MIN_PREMIUM_INCOME = 4500;

    private static final int PASSWORD_LEN = 10;

    private static String SERVER_HOST = "localhost";

    private static int SERVER_PORT = 50002;

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Override
    public void run() {
        //com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server", extraArgs)
        try(Communicator communicator = Util.initialize()){
            String s1 = "default -h localhost -p 10000";//"tcp -h "+SERVER_HOST+" -p "+SERVER_PORT+":udp -h "+SERVER_HOST+" -p "+SERVER_PORT;
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("accountAdapter", s1);

            adapter.add(this, new Identity("accountFactory","accountService")); //new Identity("accountFactory", "factory"));
            adapter.activate();

            log.info("Server is listening on "+SERVER_HOST+":"+SERVER_PORT);

            communicator.waitForShutdown();
        }catch (Exception e){
            log.error(e.getMessage());
        }

    }

    @Override
    public AccountInfo createAccount(UserData userData, Current __current) {
        log.info("Creating new account for: " + userData.pesel);

        AccountType accountType = (userData.monthlyIncome>-MIN_PREMIUM_INCOME) ? AccountType.Premium : AccountType.Standard;
        String password = generatePassword();
        StandardAccount account = (accountType == AccountType.Premium) ? new PremiumAccount(password) : new StandardAccount(password);

        __current.adapter.add(account, new Identity(userData.pesel, accountType.toString()));

        log.info("Created new " + accountType.toString() + " account");
        return new AccountInfo(password, accountType);
    }

    @Override
    public StandardAccountPrx accessStandardAccount(String pesel, Current __current) {
        return StandardAccountPrx.uncheckedCast(accessAccount(pesel, AccountType.Standard, __current));
    }

    @Override
    public PremiumAccountPrx accessPremiumAccount(String pesel, Current __current) {
        return PremiumAccountPrx.uncheckedCast(accessAccount(pesel, AccountType.Premium, __current));
    }

    private ObjectPrx accessAccount(String pesel, AccountType accountType, Current __current) {
        log.info("Access account: " + accountType.toString() + ", pesel: " + pesel);
        return __current.adapter.createProxy(new Identity(pesel, accountType.toString()));
    }

    private String generatePassword(){
        Random random = new Random();
        String alphaString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz";
        return IntStream.range(0, PASSWORD_LEN)
                .map(i -> (int)(alphaString.length()*random.nextFloat()))
                .mapToObj(alphaString::charAt)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
