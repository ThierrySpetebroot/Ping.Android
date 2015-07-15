package fr.inria.sop.diana.qoe.pingandroid.commands;

/**
 * Created by User on 13/07/2015.
 */
public interface ICommandOptionDescriptor {
    boolean isMandatory();
    Class getTargetClass();
    Class getCommandClass();
    String getName();
    String getKey();

    boolean hasKey();
}
