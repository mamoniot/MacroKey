package com.mattsmeets.macrokey.model;
import com.mattsmeets.macrokey.gui.GuiRadialMenu;
import com.mattsmeets.macrokey.model.Layer;
import static com.mattsmeets.macrokey.MacroKey.instance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Model for Macro's (Bindings)
 */
public class Macro {
    public static final int FLAG_RADIAL = 1<<0;
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

    public int flags = FLAG_ONDOWN;

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
    public String radialKey = "";//NOTE: if this macro is saved then only change this value through changeMacroKeyCode

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


    public void execute(EntityPlayerSP player) {
        // send command or text to server. For the time being it is
        // not possible to execute client-only commands. Tested and its
        // cool that the mod can bind its own GUI to different keys
        // from within the GUI, but this caused some weird issues
        String command = this.command;
        if(!command.equals("")) {
            if(command.startsWith("/sneakykey radial ")) {
                String radialKey = command.substring(18);
                ArrayList<Macro> macros = instance.bindingsRepository.radialMacros.get(radialKey);
                if(macros != null) {
                    Layer layer = instance.bindingsRepository.getActiveLayer(false);

                    ArrayList<Macro> copy = new ArrayList<Macro>(macros.size());
                    for(Macro macro : macros) {
                        if(layer == null || layer.macros.contains(macro.umid)) {
                            copy.add(macro);
                        }
                    }
                    if(copy.size() > 0) Minecraft.getMinecraft().displayGuiScreen(new GuiRadialMenu(copy));
                }
            } else {
                player.sendChatMessage(command);
            }
        }
    }
}
