package info.kgeorgiy.ja.kadyrov.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Account> bankAccounts = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String subID, final String passportID) throws RemoteException {
        final Person person = getPerson(passportID, true);
        if (person == null) {
            System.out.println("Can't create account because no person which such passportID has been created.");
            return null;
        }
        final String id = passportID + ":" + subID;
        System.out.println("Creating account: " + id);
        final Account account = new RemoteAccount(id);
        if (bankAccounts.putIfAbsent(id, account) == null) {
            person.setAccount(subID, account);
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccountByID(id);
        }
    }

    @Override
    public Account getAccountByID(final String id) {
        return bankAccounts.get(id);
    }

    @Override
    public Person createPerson(final String firstName, final String lastName, final String passportID) throws RemoteException {
        System.out.println("Creating person: " + firstName + " " + lastName);
        final RemotePerson person = new RemotePerson(firstName, lastName, passportID);
        if (persons.putIfAbsent(passportID, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getPerson(passportID, true);
        }
    }

    @Override
    public Person getPerson(final String passportID, final boolean isRemote) throws RemoteException {
        final Person person = persons.get(passportID);
        if (person == null) {
            System.out.println("No person with such passportID has been created.");
            return null;
        }
        if (isRemote) {
            return person;
        } else {
            final ConcurrentMap<String, Account> localPersonAccounts = copyRemoteAccounts(person);
            return new LocalPerson(person.getFirstName(), person.getLastName(), person.getPassportID(), localPersonAccounts);
        }
    }

    private static ConcurrentMap<String, Account> copyRemoteAccounts(final Person person) throws RemoteException {
        ConcurrentMap<String, Account> localPersonAccounts = new ConcurrentHashMap<>();
        for (Account account : person.getPersonAccounts().values()) {
            String[] parts = account.getId().split(":", 2);
            localPersonAccounts.putIfAbsent(parts[1], new LocalAccount(account.getId(), account.getAmount()));
        }
        return localPersonAccounts;
    }
}
