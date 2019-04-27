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
            string peselNumber;
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
            AccountType accountType;
            string password;
        };

        exception WrongPassword {};
        exception AccountDoesNotExist {};
        exception NotSupportedCurrency {};

        interface StandardAccount {
            AccountType getAccountType(string peselNumber) throws WrongPassword;
        };

        interface PremiumAccount extends StandardAccount {
            LoanOffer getLoanInfo(string peselNumber, LoanInquiry loanInquiry) throws WrongPassword, NotSupportedCurrency;
        };

        interface AccountFactory {
            AccountInfo createAccount(UserData userData);
            StandardAccount* accessAccount(string peselNumber, AccountType accountType) throws AccountDoesNotExist;
        };
    };
  };
};

#endif