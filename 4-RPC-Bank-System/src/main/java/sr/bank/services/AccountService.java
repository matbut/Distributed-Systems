package sr.bank.services;

import com.zeroc.Ice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.bank.accounts.StandardAccount;
import sr.middleware.slice.*;

import java.lang.Exception;
import java.util.Random;
import java.util.stream.IntStream;

public class AccountService implements AccountFactory, Runnable {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private static final long MIN_PREMIUM_INCOME = 4500;

    private static final int PASSWORD_LEN = 10;

    private final String host;

    private final int port;

    private final LoanService loanService;

    public AccountService(String host, int port, LoanService loanService) {
        this.host = host;
        this.port = port;
        this.loanService = loanService;
    }

    @Override
    public void run() {
        //com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server", extraArgs)
        try(Communicator communicator = Util.initialize()){
            String s1 = "default -h " + host + " -p " + port; //"tcp -h "++" -p "+ SERVER_PORT+":udp -h "+SERVER_HOST+" -p "+SERVER_PORT;
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("accountAdapter", s1);

            adapter.add(this, new Identity("accountFactory","accountService")); //new Identity("accountFactory", "factory"));
            adapter.activate();

            log.info("Account Service is listening on "+host+":"+port);

            communicator.waitForShutdown();
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public AccountInfo createAccount(UserData userData, Current __current) {
        AccountType accountType = (userData.monthlyIncome > MIN_PREMIUM_INCOME) ? AccountType.Premium : AccountType.Standard;
        String password = generatePassword();
        sr.bank.accounts.StandardAccount account = (accountType == AccountType.Premium) ? new sr.bank.accounts.PremiumAccount(userData, password, loanService) : new StandardAccount(userData, password);

        __current.adapter.add(account, new Identity(userData.pesel, accountType.toString()));

        log.info("Created new " + accountType.toString() + " account for " + userData.pesel);
        return new AccountInfo(password, accountType);
    }

    @Override
    public StandardAccountPrx accessStandardAccount(String pesel, Current __current) {
        return StandardAccountPrx.checkedCast(accessAccount(pesel, AccountType.Standard, __current));
    }

    @Override
    public PremiumAccountPrx accessPremiumAccount(String pesel, Current __current) {
        return PremiumAccountPrx.checkedCast(accessAccount(pesel, AccountType.Premium, __current));
    }

    private ObjectPrx accessAccount(String pesel, AccountType accountType, Current __current) {
        log.info("Access " + accountType.toString() + " account for " + pesel);
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
