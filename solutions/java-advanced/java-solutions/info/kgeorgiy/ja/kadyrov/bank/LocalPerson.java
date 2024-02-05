package info.kgeorgiy.ja.kadyrov.bank;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

public class LocalPerson extends AbstractPerson implements Serializable {

    public LocalPerson(final String firstName, final String lastName, final String passportID, ConcurrentMap<String, Account> accounts) {
        super(firstName, lastName, passportID);
        this.personAccounts = accounts;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassportID() {
        return passportID;
    }

    @Override
    public void setAccount(final String subID, Account account) {
        personAccounts.put(subID, account);
    }

    @Override
    public Account getAccountBySubID(final String subID) {
        return personAccounts.get(subID);
    }

    @Override
    public ConcurrentMap<String, Account> getPersonAccounts() {
        return personAccounts;
    }
}
