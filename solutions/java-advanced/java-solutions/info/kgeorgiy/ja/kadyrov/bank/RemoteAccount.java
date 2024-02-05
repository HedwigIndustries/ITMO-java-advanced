package info.kgeorgiy.ja.kadyrov.bank;

import java.rmi.RemoteException;

public class RemoteAccount extends AbstractAccount implements Account {
    public RemoteAccount(final String id) throws RemoteException {
        super(id);
    }
}
