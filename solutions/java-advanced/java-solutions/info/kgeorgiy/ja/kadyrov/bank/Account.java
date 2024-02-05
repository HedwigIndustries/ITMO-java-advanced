package info.kgeorgiy.ja.kadyrov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Account extends Remote {
    /**
     * Returns account identifier.
     */
    String getId() throws RemoteException;

    /**
     * Returns amount of money in the account.
     */
    long getAmount() throws RemoteException;

    /**
     * Sets amount of money in the account.
     */
    void setAmount(final long amount) throws RemoteException;

    /**
     * Adds amount of money in the account.
     */
    void addAmount(final long amount) throws RemoteException;
}
