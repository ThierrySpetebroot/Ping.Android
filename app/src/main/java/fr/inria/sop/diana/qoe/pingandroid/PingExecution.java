package fr.inria.sop.diana.qoe.pingandroid;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by User on 16/07/2015.
 */
public class PingExecution implements Serializable {
    private Date _date;
    private IPingResult _result;

    public Date getDate() {
        return _date;
    }
    public IPingResult getResult() {
        return _result;
    }

    public PingExecution(Date date, IPingResult result) {
        _date = date;
        _result = result;
    }
}
