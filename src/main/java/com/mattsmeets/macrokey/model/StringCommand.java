package com.mattsmeets.macrokey.model;

import com.mattsmeets.macrokey.MacroKey;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Old vanilla macrokey command execution
 */
public class StringCommand {

    /**
     * Command to execute
     */
    public final String command;

    public StringCommand(String command) {

        this.command = command;
    }


    public void execute(EntityPlayerSP player) {
        // send command or text to server. For the time being it is
        // not possible to execute client-only commands. Tested and its
        // cool that the mod can bind its own GUI to different keys
        // from within the GUI, but this caused some weird issues
        player.sendChatMessage(command);
    }

    public String toString() {
        return command;
    }
}
