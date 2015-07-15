package fr.inria.sop.diana.qoe.pingandroid.commands.ping;

import java.net.InetAddress;

import fr.inria.sop.diana.qoe.pingandroid.commands.FlagOption;
import fr.inria.sop.diana.qoe.pingandroid.commands.ICommandOption;
import fr.inria.sop.diana.qoe.pingandroid.commands.ICommandOptionDescriptor;
import fr.inria.sop.diana.qoe.pingandroid.commands.IValueCommandOption;

/**
 * Created by User on 13/07/2015.
 */
public enum PingCommandOptions implements ICommandOption {
    QUIET(new FlagOption.FlagOptionDescriptor("q", "quiet output"));

    private ICommandOptionDescriptor descriptor;

    PingCommandOptions(ICommandOptionDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public ICommandOptionDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String getKey() {
        return descriptor.getKey();
    }

    @Override
    public boolean hasKey() {
        return descriptor.hasKey();
    }

    protected static class IpCommandOption implements IValueCommandOption<InetAddress> {

        public static final IPingCommandOptionDescriptor DESCRIPTOR = new IPingCommandOptionDescriptor() {

            @Override
            public boolean isMandatory() {
                return true;
            }

            @Override
            public Class getTargetClass() {
                return IpCommandOption.class;
            }

            @Override
            public Class getCommandClass() {
                return IPingCommandExecutor.class;
            }

            @Override
            public String getName() {
                return "target IP address";
            }

            @Override
            public String getKey() {
                return null;
            }

            @Override
            public boolean hasKey() {
                return false;
            }
        };

        private InetAddress address;

        IpCommandOption(InetAddress address) {
            this.address = address;
        }

        @Override
        public ICommandOptionDescriptor getDescriptor() {
            return DESCRIPTOR;
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public boolean hasKey() {
            return false;
        }

        @Override
        public InetAddress getValue() {
            return address;
        }
    }
}
