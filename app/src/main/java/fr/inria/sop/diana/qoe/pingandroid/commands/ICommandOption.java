package fr.inria.sop.diana.qoe.pingandroid.commands;

public interface ICommandOption {
    ICommandOptionDescriptor getDescriptor();
    String getKey();
    boolean hasKey(); // #TODO remove, every option has a key / id (if it's mandatory it's implicit - see man)
}
