package info.kgeorgiy.ja.kadyrov.bank.tests;

import info.kgeorgiy.ja.kadyrov.bank.*;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BankTests {
    private static Bank bank;
    private final static int DEFAULT_PORT = 8888;
    private final static int PERSONS = 10;
    private final static int ACCOUNTS = 10;
    private final static int REQUESTS = 10;
    private final static int AMOUNT = 100;
    private final static String FIRST_NAME = "firstName";
    private final static String LAST_NAME = "lastName";
    private final static String PASSPORT_ID = "1234567890";
    private final static int THREADS = 5;
    private static ExecutorService service;
    private final static String URL = "//localhost:" + DEFAULT_PORT + "/bank";

    @BeforeClass
    public static void beforeClass() throws RemoteException, MalformedURLException {
        bank = new RemoteBank(DEFAULT_PORT);
        LocateRegistry.createRegistry(DEFAULT_PORT);
        UnicastRemoteObject.exportObject(bank, DEFAULT_PORT);
        Naming.rebind(URL, bank);
    }

    @Test
    public void test1() throws RemoteException {
        System.err.println("test_1::create_new_remote&local_person.");
        String prefix = "test_numbers";
        checkPerson(prefix, 0);
        System.err.println("test_1::complete!");
    }


    @Test
    public void test2() {
        System.err.println("test_2::create_new_remote&local_person_with_threads.");
        String prefix = "test_strings";
        service = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < PERSONS; i++) {
            int finalI = i;
            service.submit(() -> {
                try {
                    checkPerson(prefix, finalI);
                } catch (RemoteException e) {
                    Assert.fail(e.getMessage());
                }
            });
        }
        service.close();
        System.err.println("test_2::complete!");
    }

    private static String curTestField(String field, String prefix, int finalI) {
        return field + "_" + prefix + "_" + finalI;
    }

    private static void checkPerson(String prefix, int finalI) throws RemoteException {
        String passportId = curTestField(PASSPORT_ID, prefix, finalI);
        Assert.assertNull(bank.getPerson(passportId, true));
        Assert.assertNull(bank.getPerson(passportId, false));
        Person remotePerson = createUniquePerson(prefix, finalI);
        checkPersonFields(remotePerson, bank.getPerson(passportId, true));
        checkPersonFields(remotePerson, bank.getPerson(passportId, false));
    }

    private static Person createUniquePerson(String prefix, int id) throws RemoteException {
        String firstName = curTestField(FIRST_NAME, prefix, id);
        String lastName = curTestField(LAST_NAME, prefix, id);
        String passportId = curTestField(PASSPORT_ID, prefix, id);
        return bank.createPerson(firstName, lastName, passportId);
    }

    private static void checkPersonFields(Person first, Person second) throws RemoteException {
        Assert.assertEquals(first.getFirstName(), second.getFirstName());
        Assert.assertEquals(first.getLastName(), second.getLastName());
        Assert.assertEquals(first.getPassportID(), second.getPassportID());
    }

    @Test
    public void test3() throws RemoteException {
        System.err.println("test_3::create_new_remote_account.");
        String prefix = "test3";
        Person person = createUniquePerson(prefix, 0);
        checkCreatingRemoteAccount(person, 0);
        System.err.println("test_3::complete!");
    }

    @Test
    public void test4() throws RemoteException {
        System.err.println("test_4::create_new_remote_account_threads.");
        String prefix = "test4";
        Person person = createUniquePerson(prefix, 0);
        service = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < ACCOUNTS; i++) {
            int finalI = i;
            service.submit(() -> {
                try {
                    checkCreatingRemoteAccount(person, finalI);
                } catch (RemoteException e) {
                    Assert.fail(e.getMessage());
                }
            });
        }
        service.close();
        System.err.println("test_4::complete!");
    }

    private static void checkCreatingRemoteAccount(Person remotePerson, int finalI) throws RemoteException {
        Assert.assertEquals(remotePerson.getPersonAccounts(), new ConcurrentHashMap<>());
        String subID = Integer.toString(finalI);
        String id = getAccountID(remotePerson.getPassportID(), subID);
        LocalPerson localPersonBefore = (LocalPerson) bank.getPerson(remotePerson.getPassportID(), false);
        Assert.assertEquals(localPersonBefore.getPersonAccounts(), new ConcurrentHashMap<>());
        Assert.assertNull(localPersonBefore.getAccountBySubID(subID));
        Account account = bank.createAccount(subID, remotePerson.getPassportID());
        Assert.assertEquals(account.getAmount(), 0);
        checkAccountFields(bank.getAccountByID(id), account);
        checkAccountFields(remotePerson.getAccountBySubID(subID), account);
        Assert.assertNull(localPersonBefore.getAccountBySubID(subID));
        LocalPerson localPersonAfter = (LocalPerson) bank.getPerson(remotePerson.getPassportID(), false);
        checkAccountFields(localPersonAfter.getAccountBySubID(subID), account);
    }

    private static void checkAccountFields(Account first, Account second) throws RemoteException {
        Assert.assertEquals(first.getId(), second.getId());
        Assert.assertEquals(first.getAmount(), second.getAmount());
    }

    private static String getAccountID(String passportID, String subID) {
        return passportID + ":" + subID;
    }

    @Test
    public void test5() throws RemoteException {
        System.err.println("test_5::create_new_local_account_for_local_person.");
        String prefix = "test5";
        Person person = createUniquePerson(prefix, 0);
        LocalPerson localPerson = (LocalPerson) bank.getPerson(person.getPassportID(), false);
        checkPersonFields(person, localPerson);
        int subID = 0;
        checkCreatingLocalAccount(localPerson, subID);
        System.err.println("test_5::complete!");
    }

    @Test
    public void test6() throws RemoteException {
        System.err.println("test_6::create_new_local_account_for_local_person_threads.");
        String prefix = "test6";
        Person person = createUniquePerson(prefix, 0);
        LocalPerson localPerson = (LocalPerson) bank.getPerson(person.getPassportID(), false);
        checkPersonFields(person, localPerson);
        service = Executors.newFixedThreadPool(THREADS);
        for (int subID = 0; subID < ACCOUNTS; subID++) {
            int finalSubID = subID;
            service.submit(() -> {
                try {
                    checkCreatingLocalAccount(localPerson, finalSubID);
                } catch (RemoteException e) {
                    Assert.fail(e.getMessage());
                }
            });
        }
        service.close();
        System.err.println("test_6::complete!");
    }

    private static void checkCreatingLocalAccount(LocalPerson localPerson, int subID) throws RemoteException {
        long amount = 666;
        LocalAccount account = new LocalAccount(getAccountID(localPerson.getPassportID(), Integer.toString(subID)), amount);
        Assert.assertNotNull(account);
        localPerson.setAccount(Integer.toString(subID), account);
        Assert.assertNull(bank.getAccountByID(account.getId()));
        Assert.assertNull(bank.getPerson(localPerson.getPassportID(), true)
                .getAccountBySubID(Integer.toString(subID)));
    }

    @Test
    public void test7() throws RemoteException {
        System.err.println("test_7::get_remote_person_accounts_threads.");
        String prefix = "test7";
        Person remotePerson = createUniquePerson(prefix, 0);
        LocalPerson localPerson = (LocalPerson) bank.getPerson(remotePerson.getPassportID(), false);
        Assert.assertEquals(localPerson.getPersonAccounts(), new ConcurrentHashMap<>());
        service = Executors.newFixedThreadPool(THREADS);
        for (int subID = 0; subID < ACCOUNTS; subID++) {
            int finalSubID = subID;
            service.submit(() -> {
                try {
                    bank.createAccount(Integer.toString(finalSubID), remotePerson.getPassportID());
                } catch (RemoteException e) {
                    Assert.fail(e.getMessage());
                }
            });
        }
        service.close();
        Assert.assertNotEquals(localPerson.getPersonAccounts(), remotePerson.getPersonAccounts());
        Assert.assertEquals(localPerson.getPersonAccounts(), new ConcurrentHashMap<>());
        System.err.println("test_7::complete!");
    }


    @Test
    public void test8() throws RemoteException {
        System.err.println("test_8::add_amount_remote_person_threads.");
        String prefix = "test8";
        Person remotePerson = createUniquePerson(prefix, 0);
        String subID = "0";
        Account account = bank.createAccount(subID, remotePerson.getPassportID());
        long startAmount = 666;
        account.setAmount(startAmount);
        LocalPerson localPerson = (LocalPerson) bank.getPerson(remotePerson.getPassportID(), false);
        Assert.assertEquals(localPerson.getAccountBySubID(subID).getAmount(), account.getAmount());
        addAmount(account);
        Assert.assertEquals(startAmount + AMOUNT * REQUESTS, account.getAmount());
        Assert.assertNotEquals(localPerson.getAccountBySubID(subID).getAmount(), account.getAmount());
        System.err.println("test_8::complete!");
    }

    @Test
    public void test9() throws RemoteException {
        System.err.println("test_9::add_amount_local_person_threads.");
        String prefix = "test9";
        String subID = "0";
        Person remotePerson = createUniquePerson(prefix, 0);
        Account remoteAccount = bank.createAccount(subID, remotePerson.getPassportID());
        long startAmount = 666;
        remoteAccount.setAmount(startAmount);
        LocalPerson localPerson = (LocalPerson) bank.getPerson(remotePerson.getPassportID(), false);
        Account localAccount = localPerson.getAccountBySubID(subID);
        addAmount(localAccount);
        Assert.assertEquals(startAmount, remoteAccount.getAmount());
        Assert.assertEquals(startAmount + AMOUNT * REQUESTS, localAccount.getAmount());
        long hundred = 111;
        remoteAccount.addAmount(hundred);
        Assert.assertEquals(startAmount + hundred, remoteAccount.getAmount());
        Assert.assertEquals(startAmount + AMOUNT * REQUESTS, localAccount.getAmount());
        System.err.println("test_9::complete!");
    }

    private static void addAmount(Account account) {
        service = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < REQUESTS; i++) {
            service.submit(() -> {
                try {
                    account.addAmount(AMOUNT);
                } catch (RemoteException e) {
                    Assert.fail(e.getMessage());
                }
            });
        }
        service.close();
    }

    @AfterClass
    public static void afterClass() {
    }

}
