package fr.inria.sop.diana.qoe.pingandroid.commands;

/**
 * Created by User on 13/07/2015.
 */
public class FlagOption implements ICommandOption {
    public static final ICommandOptionDescriptor COMMAND_OPTION_DESCRIPTOR = new FlagOptionDescriptor(null, "Flag Option Descriptor Template");

    @Override
    public ICommandOptionDescriptor getDescriptor() {
        return COMMAND_OPTION_DESCRIPTOR;
    }

    @Override
    public String getKey() {
        return COMMAND_OPTION_DESCRIPTOR.getKey();
    }

    @Override
    public boolean hasKey() {
        return true;
    }

    public static class FlagOptionDescriptor implements ICommandOptionDescriptor {

        private String name;
        private String key;

        public FlagOptionDescriptor(String key, String name) {
            this.key = key;
            this.name = name;
        }

        @Override
        public boolean isMandatory() {
            return false;
        }

        @Override
        public Class getTargetClass() {
            return FlagOption.class;
        }

        @Override
        public Class getCommandClass() {
            return null;
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
            return true;
        }
    }
}
