package fr.inria.sop.diana.qoe.pingandroid.commands;

/**
 * Created by User on 13/07/2015.
 */
public interface ICommandExecutor<I> {
    I execute(ICommandOption... options);
}
