package info.kgeorgiy.ja.kadyrov.bank;

import java.rmi.RemoteException;

public class AbstractAccount implements Account {
    protected final String id;
    protected long amount;

    public AbstractAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }

    @Override
    public synchronized long getAmount() throws RemoteException {
        System.out.println("Getting amount of money for remote account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final long amount) throws RemoteException {
        System.out.println("Setting amount of money for remote account " + id);
        this.amount = amount;
    }

    @Override
    public synchronized void addAmount(final long amount) throws RemoteException {
        System.out.println("Setting amount of money for remote account " + id);
        this.amount += amount;
    }
}
