package sr.bank.accounts;

import com.zeroc.Ice.Current;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.bank.services.AccountService;
import sr.middleware.slice.AuthenticationFailedException;
import sr.middleware.slice.UserData;

public class StandardAccount implements sr.middleware.slice.StandardAccount {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final UserData userData;

    private final String password;

    private double ballance = 0;

    public StandardAccount(UserData userData, String password) {
        this.userData = userData;
        this.password = password;
    }

    @Override
    public double getAccountBalance(String pesel, Current current) throws AuthenticationFailedException {
        authenticate(pesel,current);
        log.info("get account balance");
        return ballance;
    }

    protected void authenticate(String checkedPesel, Current current) throws AuthenticationFailedException{
        String checkedPassword = current.ctx.get("password");
        if (!userData.pesel.equals(checkedPesel) || !password.equals(checkedPassword))
            throw new AuthenticationFailedException();
    }
}
