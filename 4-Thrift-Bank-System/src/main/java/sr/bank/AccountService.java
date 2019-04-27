package sr.bank;

import com.zeroc.Ice.Current;
import sr.middleware.slice.*;

import java.util.HashMap;

public class AccountService implements AccountFactory {

    private final HashMap<>

    @Override
    public AccountInfo createAccount(UserData userData, Current current) {
        return null;
    }

    @Override
    public StandardAccountPrx accessAccount(String peselNumber, AccountType accountType, Current current) throws AccountDoesNotExist {
        return null;
    }
}
