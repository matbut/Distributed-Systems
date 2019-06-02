#ifndef CALC_ICE
#define CALC_ICE

module sr {
  module middleware {
    module slice {
        enum Currency {
            PLN,
            EUR,
            USD,
            GBP,
            CHR
        };

        enum AccountType {
            Standard,
            Premium
        };

        struct UserData {
            string firstName;
            string lastName;
            string pesel;
            long monthlyIncome;
        };

        struct LoanInquiry {
            Currency currency;
            double loanValue;
            int period;
        };

        struct LoanOffer {
            double loanCostBaseCurrency;
            double loanCostOtherCurrency;
        };

        struct AccountInfo {
            string password;
            AccountType accountType;
        };

        exception AuthenticationFailedException {};
        exception NotSupportedCurrencyException {};

        interface StandardAccount {
            double getAccountBalance(string pesel) throws AuthenticationFailedException;
        };

        interface PremiumAccount extends StandardAccount {
            LoanOffer getLoanInfo(string pesel, LoanInquiry loanInquiry) throws AuthenticationFailedException, NotSupportedCurrencyException;
        };

        interface AccountFactory {
            AccountInfo createAccount(UserData userData);
            StandardAccount* accessStandardAccount(string pesel);
            PremiumAccount* accessPremiumAccount(string pesel);
        };
    };
  };
};

#endif