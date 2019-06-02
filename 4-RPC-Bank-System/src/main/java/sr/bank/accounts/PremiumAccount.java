package sr.bank.accounts;

import com.zeroc.Ice.Current;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sr.bank.services.LoanService;
import sr.middleware.slice.*;


public class PremiumAccount extends StandardAccount implements sr.middleware.slice.PremiumAccount{

    private static final Logger log = LoggerFactory.getLogger(PremiumAccount.class);

    private final LoanService loanService;

    public PremiumAccount(UserData userData, String password, LoanService loanService) {
        super(userData, password);
        this.loanService = loanService;
    }

    @Override
    public LoanOffer getLoanInfo(String pesel, LoanInquiry loanInquiry, Current current) throws NotSupportedCurrencyException, AuthenticationFailedException {
        authenticate(pesel, current);
        log.info("get loan info");
        return loanService.getLoanOffer(loanInquiry);
    }
}
