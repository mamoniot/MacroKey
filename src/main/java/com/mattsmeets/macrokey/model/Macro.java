package com.mattsmeets.macrokey.model;

import net.minecraft.client.entity.EntityPlayerSP;

import java.util.UUID;

/**
 * Model for Macro's (Bindings)
 */
public class Macro {
    public static final int FLAG_ACTIVE = 1<<0;
    public static final int FLAG_ONDOWN = 1<<1;
    public static final int FLAG_ONUP = 1<<2;
    public static final int FLAG_REPEAT_ONDOWN = 1<<3;
    public static final int FLAG_NOTONEVEN = 1<<4;
    public static final int FLAG_NOTONODD = 1<<5;

    public static final int FLAG_SHIFT_DOWN = 1<<16;
    public static final int FLAG_SHIFT_UP = 1<<17;
    public static final int FLAG_CTRL_DOWN = 1<<18;
    public static final int FLAG_CTRL_UP = 1<<19;
    public static final int FLAG_ALT_DOWN = 1<<20;
    public static final int FLAG_ALT_UP = 1<<21;

    public int flags = FLAG_ONDOWN | FLAG_ACTIVE;

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

    public int keyCode = 0;//NOTE: if this macro is saved then only change this value through changeMacroKeyCode

    // public Macro() {
    //     this.umid = UUID.randomUUID();
    // }
    public Macro clone() {
        Macro ret = new Macro();
        ret.flags = this.flags;
        ret.umid = this.umid;
        ret.command = this.command;
        ret.keyCode = this.keyCode;
        return ret;
    }
}
