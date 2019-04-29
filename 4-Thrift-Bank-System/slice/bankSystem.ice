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

        exception WrongPasswordException {};
        exception NotSupportedCurrencyException {};

        interface StandardAccount {
            double getAccountBalance(string pesel) throws WrongPasswordException;
        };

        interface PremiumAccount extends StandardAccount {
            LoanOffer getLoanInfo(string pesel, LoanInquiry loanInquiry) throws WrongPasswordException, NotSupportedCurrencyException;
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