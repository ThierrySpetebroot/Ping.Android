package fr.inria.sop.diana.qoe.pingandroid;

public interface IConnectionManager<T, Destination> {
    public IConnection<T> getConnection(Destination d);
}
