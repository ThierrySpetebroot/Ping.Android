package fr.inria.sop.diana.qoe.pingandroid;

import java.io.IOException;

/**
 * Created by User on 13/07/2015.
 */
public interface IConnection<T> {
    T receive() throws IOException;
    void send(T value) throws IOException;
    void close();
}
