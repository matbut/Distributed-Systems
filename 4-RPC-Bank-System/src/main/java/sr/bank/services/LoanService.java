package sr.bank.services;

import sr.middleware.proto.ExchangeRate;
import sr.middleware.slice.LoanInquiry;
import sr.middleware.slice.LoanOffer;
import sr.middleware.slice.NotSupportedCurrencyException;


public class LoanService {

    private final static double INTEREST_RATE = 1.1;

    private final ExchangeRateService exchangeRateService;

    public LoanService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    public LoanOffer getLoanOffer(LoanInquiry loanInquiry) throws NotSupportedCurrencyException {
        ExchangeRate exchangeRate = exchangeRateService.getExchangeRate(mapCurrency(loanInquiry.currency));

        double loanCostBaseCurrency = (loanInquiry.loanValue*INTEREST_RATE)/ loanInquiry.period;
        double loanCostOtherCurrency = loanCostBaseCurrency*exchangeRate.getBuy();

        return new LoanOffer(loanCostBaseCurrency, loanCostOtherCurrency);
    }

    private sr.middleware.slice.Currency mapCurrency(sr.middleware.proto.Currency protoCurrency) {
        return sr.middleware.slice.Currency.valueOf(protoCurrency.getNumber());
    }

    private sr.middleware.proto.Currency mapCurrency(sr.middleware.slice.Currency sliceCurrency) {
        return sr.middleware.proto.Currency.forNumber(sliceCurrency.value());
    }

}
