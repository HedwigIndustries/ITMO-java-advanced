package info.kgeorgiy.ja.kadyrov.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount extends AbstractAccount implements Serializable {

    public LocalAccount(final String id, final long amount) throws RemoteException {
        super(id);
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized long getAmount() {
        System.out.println("Getting amount of money for local account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final long amount) {
        System.out.println("Setting amount of money for local account " + id);
        this.amount = amount;
    }

    @Override
    public synchronized void addAmount(final long amount) {
        System.out.println("Setting amount of money for local account " + id);
        this.amount += amount;
    }
}
