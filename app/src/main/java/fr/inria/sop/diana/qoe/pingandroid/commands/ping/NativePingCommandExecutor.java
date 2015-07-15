package fr.inria.sop.diana.qoe.pingandroid.commands.ping;

import fr.inria.sop.diana.qoe.pingandroid.commands.ICommandExecutor;
import fr.inria.sop.diana.qoe.pingandroid.commands.ICommandOption;

/**
 * Created by User on 13/07/2015.
 */
public class NativePingCommandExecutor implements ICommandExecutor<IPingResult> {

    private static final String BASE_COMMAND = "ping";

    @Override
    public IPingResult execute(ICommandOption... options) {
        for(ICommandOption option : options) {
            if(!(IPingCommandExecutor.class.isAssignableFrom(option.getDescriptor().getCommandClass()))) {
                throw new IllegalArgumentException("Invalid Command Option, the option has a different target command");
            }

            switch (option.getKey()) {

            }
        }



        return null;
    }
}
