package sr.bank;

import com.zeroc.Ice.Current;
import sr.middleware.slice.WrongPasswordException;

public class StandardAccount implements sr.middleware.slice.StandardAccount {

    private final String password;

    private double ballance = 0;

    public StandardAccount(String password) {
        this.password = password;
    }

    @Override
    public double getAccountBalance(String pesel, Current current) throws WrongPasswordException {
        authenticate(current);
        return ballance;
    }

    protected void authenticate(Current current) throws WrongPasswordException{
        String checkedPassword = current.ctx.get("password");
        if (!password.equals(checkedPassword))
            throw new WrongPasswordException();
    }
}
