package info.kgeorgiy.ja.kadyrov.bank;

import java.rmi.RemoteException;

public class RemotePerson extends AbstractPerson implements Person {
    public RemotePerson(final String firstName, final String lastName, final String passportID) throws RemoteException {
        super(firstName, lastName, passportID);
    }
}
