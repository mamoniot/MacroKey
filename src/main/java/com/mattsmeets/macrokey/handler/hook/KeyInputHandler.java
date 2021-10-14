package com.mattsmeets.macrokey.handler.hook;

import com.mattsmeets.macrokey.config.ModConfig;
import com.mattsmeets.macrokey.MacroKey;
import com.mattsmeets.macrokey.model.Macro;
import com.mattsmeets.macrokey.model.Layer;
import com.mattsmeets.macrokey.gui.GuiRadialMenu;
import static com.mattsmeets.macrokey.MacroKey.instance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class KeyInputHandler {
    public static String spawnRadial = null;//this is dumb, but java is dumb

    public final HashSet<Integer> pressedKeys = new HashSet<>();
    public final HashSet<Integer> toggleKeys = new HashSet<>();

    public final ArrayList<Macro> macrosToRun = new ArrayList<Macro>();
    public final ArrayList<Macro> macrosToRepeat = new ArrayList<Macro>();

    public int delta = 0;


    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        if(Keyboard.isRepeatEvent()) return;//do nothing on repeat keys when a key is being held down
        int keyCode = Keyboard.getEventKey();

        // find if the current key being pressed is the dedicated
        // MacroKey gui button. If so, open its GUI
        if (instance.forgeKeybindings[0].isPressed()) {
            Minecraft.getMinecraft().player.openGui(
                instance,
                ModConfig.guiMacroManagementId,
                Minecraft.getMinecraft().world,
                (int) Minecraft.getMinecraft().player.posX,
                (int) Minecraft.getMinecraft().player.posY,
                (int) Minecraft.getMinecraft().player.posZ
            );
        }

        // find all macro's by the current key pressed, while not syncing
        ArrayList<Macro> macros = instance.bindingsRepository.findMacroByKeycode(keyCode, instance.bindingsRepository.getActiveLayer(false), false);

        // if the list is not empty
        if (macros == null || macros.size() == 0)  return;

        if (Keyboard.getEventKeyState()) {

            if(this.pressedKeys.contains(keyCode)) {//in case we miss a keyboard event
                onKeyEvent(false, macros, keyCode);
            } else {
                this.pressedKeys.add(keyCode);
            }

            onKeyEvent(true, macros, keyCode);
        } else {

            if(!this.pressedKeys.contains(keyCode)) {//in case we miss a keyboard event
                onKeyEvent(true, macros, keyCode);
            } else {
                this.pressedKeys.remove(keyCode);
            }

            onKeyEvent(false, macros, keyCode);
        }
        //NOTE: radial menu popups always cause us to miss keyboard events
    }

    public void onKeyEvent(boolean ispressed, ArrayList<Macro> macros, int keyCode) {
        //NOTE: this code can break if a repeat macro is changed while a key is being held down
        int usedKeys = 1;
        for(Macro macro : macros) {
            boolean cancelled =
                ((macro.flags&Macro.FLAG_SHIFT_DOWN) > 0 && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                || ((macro.flags&Macro.FLAG_SHIFT_UP) > 0 && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                || ((macro.flags&Macro.FLAG_CTRL_DOWN) > 0 && !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
                || ((macro.flags&Macro.FLAG_CTRL_UP) > 0 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
                || ((macro.flags&Macro.FLAG_ALT_DOWN) > 0 && !Keyboard.isKeyDown(Keyboard.KEY_LMENU))
                || ((macro.flags&Macro.FLAG_ALT_UP) > 0 && Keyboard.isKeyDown(Keyboard.KEY_LMENU))
                || ((macro.flags&Macro.FLAG_NOTONEVEN) > 0 && !toggleKeys.contains(macro.keyCode))
                || ((macro.flags&Macro.FLAG_NOTONODD) > 0 && toggleKeys.contains(macro.keyCode));
            if(cancelled) continue;
            if(ispressed) {
                if((macro.flags&Macro.FLAG_SHIFT_DOWN) > 0 || (macro.flags&Macro.FLAG_SHIFT_UP) > 0) usedKeys |= 1<<1;
                if((macro.flags&Macro.FLAG_CTRL_DOWN) > 0 ||(macro.flags&Macro.FLAG_CTRL_UP) > 0) usedKeys |= 1<<2;
                if((macro.flags&Macro.FLAG_ALT_DOWN) > 0 || (macro.flags&Macro.FLAG_ALT_UP) > 0) usedKeys |= 1<<3;
            }

            if((macro.flags&Macro.FLAG_ONDOWN) > 0) {
                if(ispressed) {
                    this.macrosToRun.add(macro);
                }
            } else if((macro.flags&Macro.FLAG_ONUP) > 0) {
                if(!ispressed) {
                    this.macrosToRun.add(macro);
                }
            } else if((macro.flags&Macro.FLAG_REPEAT_ONDOWN) > 0) {
                if(ispressed) {
                    this.macrosToRepeat.add(macro);
                } else {
                    for(int i = 0; i < this.macrosToRepeat.size(); ) {
                        if(this.macrosToRepeat.get(i).umid.equals(macro.umid)) {
                            this.macrosToRepeat.remove(i);
                        } else {
                            i++;
                        }
                    }
                }
            }
        }


        if(this.toggleKeys.contains(keyCode)) {
            this.toggleKeys.remove(keyCode);
        } else {
            this.toggleKeys.add(keyCode);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        // check if we are in-game
        if (player == null) return;

        // every tick post an event for the normal,
        // non repeating commands to trigger
        if(macrosToRun.size() > 0) {
            Macro macro = macrosToRun.get(0);
            macro.execute(player);
            macrosToRun.remove(0);//NOTE: a ring buffer would be more efficient
        }

        boolean isLimited = delta >= ModConfig.repeatDelay;

        // rate-limiting so users can define
        // how fast a repeating command should execute
        // retrieve the given delay within the config,
        // this will by default be 20 ticks
        if (!isLimited) {
            delta++;
            return;
        }
        delta = 0;

        for(int i = 0; i < macrosToRepeat.size(); i++) {
            Macro macro = macrosToRepeat.get(i);
            macro.execute(player);
        }

        if(spawnRadial != null) {
            ArrayList<Macro> macros = instance.bindingsRepository.radialMacros.get(spawnRadial);
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
            spawnRadial = null;
        }
    }


}
