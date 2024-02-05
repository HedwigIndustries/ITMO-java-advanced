package info.kgeorgiy.ja.kadyrov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account (if account does not already exist)
     * which attached to person (if person with such passportID exists) with unique passportID.
     *
     * @param subID      account id of person.
     * @param passportId passportID of person.
     * @return created account with specified identifier or {@code null} if such person with passportID does not exist.
     */
    Account createAccount(final String subID, final String passportId) throws RemoteException;

    /**
     * Returns account by identifier, which template is "passportID:subID".
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccountByID(final String id) throws RemoteException;

    /**
     * Creates a new person (if it does not already exist)
     * with specified first name, last name, passport id.
     *
     * @param firstName  person's first name.
     * @param lastName   person's last name.
     * @param passportID person's passportID.
     * @return created person with specified identifier.
     */
    Person createPerson(final String firstName, final String lastName, final String passportID) throws RemoteException;

    /**
     * Returns person (if it already exists) by passportID
     * with a choice of the type of person to be returned.
     *
     * @param passportID person's passportID.
     * @param isRemote   type of person.
     * @return person with specified identifier or {@code null} if such person does not exist.
     */
    Person getPerson(final String passportID, final boolean isRemote) throws RemoteException;

}
