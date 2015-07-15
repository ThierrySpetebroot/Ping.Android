package fr.inria.sop.diana.qoe.pingandroid.commands.ping;

import fr.inria.sop.diana.qoe.pingandroid.commands.ICommandOptionDescriptor;
import fr.inria.sop.diana.qoe.pingandroid.commands.IKeyValuePairCommandOption;

public abstract class KeyValuePairCommandOption<T> implements IKeyValuePairCommandOption<T> {
    private T value;
    private String key;

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String getKey() {
        return key;
    }
}
