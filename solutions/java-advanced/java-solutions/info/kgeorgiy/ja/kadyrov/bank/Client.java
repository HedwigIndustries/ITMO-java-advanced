package info.kgeorgiy.ja.kadyrov.bank;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    public static void main(final String[] args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound.");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid.");
            return;
        }
        final String firstName = getArgument(args, 0, "Rustam");
        final String lastName = getArgument(args, 1, "Kadyrov");
        final String passportID = getArgument(args, 2, "1234567890");
        final String subID = getArgument(args, 3, "0");
        Person person = bank.getPerson(passportID, true);
        if (person == null) {
            person = bank.createPerson(firstName, lastName, passportID);
        }
        System.out.println("Person: " + person.getFirstName() + " " + person.getLastName());
        Account account = bank.getAccountByID(passportID + ":" + subID);
        if (account == null) {
            account = bank.createAccount(subID, passportID);
        }
        try {
            final int amount = Integer.parseInt(getArgument(args, 3, "100"));
            account.setAmount(amount);
        } catch (NumberFormatException e) {
            System.err.println("Amount should be number.");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money...");
        account.addAmount(100);
        System.out.println("Money: " + account.getAmount());
    }

    private static String getArgument(String[] args, int index, String defaultValue) {
        return index > args.length ? defaultValue : (args[index]);
    }
}
