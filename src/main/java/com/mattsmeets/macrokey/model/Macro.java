package com.mattsmeets.macrokey.model;

import net.minecraft.client.entity.EntityPlayerSP;

import java.util.UUID;

/**
 * Model for Macro's (Bindings)
 */
public class Macro {
    public static final int ONDOWN = 1;
    public static final int ONUP = 2;
    public static final int REPEAT_ONDOWN = 3;
    public static final int TOTAL_TYPES = 3;

    public int type = ONDOWN;

    /**
     * Unique Macro Identifier
     */
    public UUID umid;

    /**
     * Keycode of the button that is bound
     */
    // public int keyCode = 0;


    /**
     * Command in string form
     */
    public String command = "";

    /**
     * If the macro is active (default: true)
     */
    public boolean mustHoldShift = false;
    public boolean mustHoldCtrl = false;
    public boolean mustHoldAlt = false;
    public boolean active = true;

    public int keyCode = 0;//NOTE: if this macro is saved then only change this value through changeMacroKeyCode

    public Macro() {
        this.umid = UUID.randomUUID();
    }
    public Macro clone() {
        Macro ret = new Macro();
        ret.type = this.type;
        ret.umid = this.umid;
        ret.command = this.command;
        ret.active = this.active;
        ret.keyCode = this.keyCode;
        return ret;
    }

    public void execute(EntityPlayerSP player) {
        // send command or text to server. For the time being it is
        // not possible to execute client-only commands. Tested and its
        // cool that the mod can bind its own GUI to different keys
        // from within the GUI, but this caused some weird issues
        if(!command.equals("")) {
            player.sendChatMessage(command);
        }
    }

}
