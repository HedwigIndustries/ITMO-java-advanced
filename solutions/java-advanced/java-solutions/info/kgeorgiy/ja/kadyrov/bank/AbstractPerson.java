package info.kgeorgiy.ja.kadyrov.bank;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract public class AbstractPerson implements Person {
    protected final String firstName;
    protected final String lastName;
    protected final String passportID;
    protected ConcurrentMap<String, Account> personAccounts = new ConcurrentHashMap<>();

    public AbstractPerson(final String firstName, final String lastName, final String passportID) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportID = passportID;
    }

    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    @Override
    public String getLastName() throws RemoteException {
        return lastName;
    }

    @Override
    public String getPassportID() throws RemoteException {
        return passportID;
    }

    @Override
    public void setAccount(final String subID, Account account) throws RemoteException {
        personAccounts.put(subID, account);
    }

    @Override
    public Account getAccountBySubID(final String subID) throws RemoteException {
        return personAccounts.get(subID);
    }

    @Override
    public ConcurrentMap<String, Account> getPersonAccounts() throws RemoteException {
        return personAccounts;
    }
}
