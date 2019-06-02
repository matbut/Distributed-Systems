import sys, Ice
Ice.loadSlice('../../../slice/bankSystem.ice')
import sr.middleware.slice as Slice

class BankClient:

    def __init__(self, communicator):
        self.account = None
        self.communicator = communicator
        self.account_factory = Slice.AccountFactoryPrx.checkedCast(communicator.propertyToProxy('AccountFactory.Proxy').ice_twoway().ice_secure(False))
        if not self.account_factory:
            print("invalid proxy")
            sys.exit(1)

    def createAccount(self, firstName, lastName, pesel, monthlyIncome):
        user_data = Slice.UserData(firstName, lastName, pesel, monthlyIncome)
        return self.account_factory.createAccount(user_data)
    
    def accessPremiumAccount(self, pesel):
        self.account = self.account_factory.accessPremiumAccount(pesel)
        return self.account

    def accessStandardAccount(self, pesel):
        self.account = self.account_factory.accessStandardAccount(pesel)
        return self.account

    def getAccountBalance(self, pesel, password):
        ctx = {'password': password}
        return self.account.getAccountBalance(pesel, ctx)

    def getLoanInfo(self, pesel, password, currency, loanValue, period):
        loanInquiry = Slice.LoanInquiry(currency,loanValue,period)
        ctx = {'password': password}
        return self.account.getLoanInfo(pesel, loanInquiry, ctx)

def consoleApp(bank_client):
    options = {
        'ca':{
            'name': 'create account', 
            'args': {'firstname':str, 'lastname':str, 'pesel':str, 'monthly income':int},
            'fun': bank_client.createAccount,
            'print': True},
        'ap':{
            'name': 'access premium account', 
            'args': {'pesel':str},
            'fun': bank_client.accessPremiumAccount,
            'print': True},
        'as':{
            'name': 'access standard account', 
            'args': {'pesel':str},
            'fun': bank_client.accessStandardAccount,
            'print': True},
        'ab':{
            'name': 'account balance', 
            'args': {'pesel':str, 'password':str},
            'fun': bank_client.getAccountBalance,
            'print': True},            
        'li':{
            'name': 'loan info', 
            'args': {'pesel':str, 'password':str, 'currency':Slice.Currency, 'loan value':float, 'period':int},
            'fun': bank_client.getLoanInfo,
            'print': True},
        }

    def help():
        for command,info in options.items():
            print('%s - %s'%(command,info['name']))

    c = None
    while True:  
        try:
            sys.stdout.write("==> ")
            sys.stdout.flush()
            selected = sys.stdin.readline().strip()

            if selected in ['ex','exit']:
                sys.exit(0)

            if selected in ['hp','help']:
                help()
                continue

            if selected in options.keys():
                option = options[selected]
                args = readArgs(option['args'])
                fun = option['fun']
                if option['print']:
                    print(fun(*args))
            else:
                print('%s is not a valid command'%(selected))
        except Slice.AuthenticationFailedException:
            print('Wrong password or pesel number')
        except Slice.NotSupportedCurrencyException:
            print('Not supported currency')
        except Ice.ObjectNotExistException:
            print('Account does not exist')
        except Ice.AlreadyRegisteredException:
            print('Account arleady exists')
        except AttributeError:
            print('Not supported operation')
        except ValueError:
            print('Illegal value')

def readArgs(argDict):
    currencies = {
        'PLN':Slice.Currency.PLN,
        'EUR':Slice.Currency.EUR,
        'USD':Slice.Currency.USD,
        'GBP':Slice.Currency.GBP,
        'CHR':Slice.Currency.CHR,
    }
    args = []
    for name,typo in argDict.items():
        print('Specify %s : '%(name), end='', flush=True)
        read = sys.stdin.readline().strip()
        if typo==int:
            read = int(read)
        if typo==float:
            read = float(read)
        if typo==Slice.Currency:
            try:
                read = currencies[read.upper()]
            except KeyError:
                raise Slice.NotSupportedCurrencyException
            
        args.append(read)
    return args

with Ice.initialize(sys.argv, "config.client") as communicator:

    if len(sys.argv) > 1:
        print(sys.argv[0] + ": too many arguments")
        sys.exit(1)

    consoleApp(BankClient(communicator))

    