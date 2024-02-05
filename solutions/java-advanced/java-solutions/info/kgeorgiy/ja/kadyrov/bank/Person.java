package info.kgeorgiy.ja.kadyrov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public interface Person extends Remote {

    /**
     * Returns first name of a person.
     */
    String getFirstName() throws RemoteException;

    /**
     * Returns last name of a person.
     */
    String getLastName() throws RemoteException;

    /**
     * Returns person's passport ID.
     */
    String getPassportID() throws RemoteException;

    /**
     * Puts account in person's map of accounts.
     *
     * @param subID   subID for one of the person accounts.
     * @param account account that was put in the map.
     */
    void setAccount(final String subID, Account account) throws RemoteException;

    /**
     * Gets account by subID.
     *
     * @param subID subID for one of the person accounts.
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccountBySubID(final String subID) throws RemoteException;

    /**
     * Gets map of person's accounts.
     *
     * @return map with person's accounts.
     */
    ConcurrentMap<String, Account> getPersonAccounts() throws RemoteException;
}
