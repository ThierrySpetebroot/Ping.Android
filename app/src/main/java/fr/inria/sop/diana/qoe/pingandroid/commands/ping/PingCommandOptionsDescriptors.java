package fr.inria.sop.diana.qoe.pingandroid.commands.ping;

import fr.inria.sop.diana.qoe.pingandroid.commands.ICommandOptionDescriptor;
import fr.inria.sop.diana.qoe.pingandroid.commands.ping.IPingCommandOptionDescriptor;
import fr.inria.sop.diana.qoe.pingandroid.commands.ping.NativePingCommandExecutor;

/**
 * Created by User on 13/07/2015.
 */
public enum PingCommandOptionsDescriptors implements ICommandOptionDescriptor {
    IP(true, PingCommandOptions.IpCommandOption.class, "ip", "target_ip");

    private boolean isMandatory;
    private Class targetClass;
    private String name;
    private String key;
    private boolean hasKey;


    PingCommandOptionsDescriptors(boolean isMandatory, Class targetImplementation, String name) {
        this.isMandatory = isMandatory;
        this.targetClass = targetClass;
        this.name = name;
        this.hasKey = false;
    }

    PingCommandOptionsDescriptors(boolean isMandatory, Class targetImplementation, String name, String key) {
        this.isMandatory = isMandatory;
        this.targetClass = targetClass;
        this.name = name;
        this.key = key;
        this.hasKey = key != null;
    }

    PingCommandOptionsDescriptors(IPingCommandOptionDescriptor descriptor) {
        this.isMandatory = descriptor.isMandatory();
        this.targetClass = descriptor.getTargetClass();
        this.name = descriptor.getName();
        this.key = descriptor.getKey();
        this.hasKey = descriptor.hasKey();
    }

    @Override
    public boolean isMandatory() {
        return isMandatory;
    }

    @Override
    public Class getTargetClass() {
        return targetClass;
    }

    @Override
    public Class getCommandClass() {
        return NativePingCommandExecutor.class;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean hasKey() {
        return false;
    }
}
