package sr.bank;

import com.zeroc.Ice.Current;
import sr.middleware.slice.LoanInquiry;
import sr.middleware.slice.LoanOffer;
import sr.middleware.slice.NotSupportedCurrencyException;
import sr.middleware.slice.WrongPasswordException;


public class PremiumAccount extends StandardAccount implements sr.middleware.slice.PremiumAccount{

    public PremiumAccount(String password) {
        super(password);
    }

    @Override
    public LoanOffer getLoanInfo(String pesel, LoanInquiry loanInquiry, Current current) throws NotSupportedCurrencyException, WrongPasswordException {
        authenticate(current);
        return null;
    }
}
