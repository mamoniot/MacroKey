package com.mattsmeets.macrokey.handler.hook;

import com.mattsmeets.macrokey.config.ModConfig;
import com.mattsmeets.macrokey.MacroKey;
import com.mattsmeets.macrokey.event.ExecuteOnTickEvent;
import com.mattsmeets.macrokey.model.Macro;
import com.mattsmeets.macrokey.model.lambda.ExecuteOnTickInterface;

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

import static com.mattsmeets.macrokey.MacroKey.instance;

public class KeyInputHandler {

    private final HashSet<Integer> pressedKeys = new HashSet<>();
    private final HashSet<Integer> toggleKeys = new HashSet<>();

    private final ArrayList<Macro> macrosToRun = new ArrayList<Macro>();
    private final ArrayList<Macro> macrosToRepeat = new ArrayList<Macro>();

    private final Set<ExecuteOnTickInterface> executorsToRun = new HashSet<>();
    private int delta = 0;


    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        int keyCode = Keyboard.getEventKey();

        // find if the current key being pressed is the dedicated
        // MacroKey gui button. If so, open its GUI
        if (instance.forgeKeybindings[0].isPressed()) {
            MinecraftForge.EVENT_BUS.post(new ExecuteOnTickEvent(ExecuteOnTickInterface.openMacroKeyGUI));
        }

        // find all macro's by the current key pressed, while not syncing
        ArrayList<Macro> macros = instance.bindingsRepository.findMacroByKeycode(keyCode, instance.bindingsRepository.getActiveLayer(false), false);

        // if the list is not empty
        if (macros == null || macros.size() == 0)  return;

        if (Keyboard.getEventKeyState() && !this.pressedKeys.contains(keyCode)) {
            /*
             * if the key has not been pressed during last events, send
             * an event, and add it to the current index of pressed keys
             */
            if(this.toggleKeys.contains(keyCode)) {
                this.toggleKeys.remove(keyCode);
            } else {
                this.toggleKeys.add(keyCode);
            }
            this.pressedKeys.add(keyCode);
            onKeyEvent(true, macros);
        } else if (!Keyboard.getEventKeyState() && this.pressedKeys.contains(keyCode)) {
            /*
             * if the key has been pressed during last events, send
             * an event, and remove it from the current index of pressed keys
             */
            this.pressedKeys.remove(keyCode);
            onKeyEvent(false, macros);
        }//NOTE: this may break if a key is held down while it is added as a macro
    }

    public void onKeyEvent(boolean ispressed, ArrayList<Macro> macros) {
        //NOTE: this code can break if a repeat macro is changed while a key is being held down
        for(Macro macro : macros) {
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
    }

    @SubscribeEvent
    public void onExecutorEvent(ExecuteOnTickEvent event) {
        executorsToRun.add(event.getExecutor());
    }


    public boolean macroIsActive(Macro macro) {
        boolean cancelled = (macro.flags&Macro.FLAG_ACTIVE) == 0
            || ((macro.flags&Macro.FLAG_SHIFT) > 0 && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            || ((macro.flags&Macro.FLAG_CTRL) > 0 && !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
            || ((macro.flags&Macro.FLAG_ALT) > 0 && !Keyboard.isKeyDown(Keyboard.KEY_LMENU))
            || ((macro.flags&Macro.FLAG_NOTONEVEN) > 0 && !toggleKeys.contains(macro.keyCode))
            || ((macro.flags&Macro.FLAG_NOTONODD) > 0 && toggleKeys.contains(macro.keyCode));
        return !cancelled;
    }

    public void execute(Macro macro, EntityPlayerSP player) {
        // send command or text to server. For the time being it is
        // not possible to execute client-only commands. Tested and its
        // cool that the mod can bind its own GUI to different keys
        // from within the GUI, but this caused some weird issues
        if(!macro.command.equals("")) {
            player.sendChatMessage(macro.command);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        // check if we are in-game
        if (player == null) {
            return;
        }

        // every tick post an event for the normal,
        // non repeating commands to trigger
        if(macrosToRun.size() > 0) {
            Macro macro = macrosToRun.get(0);
            if(macroIsActive(macro)) execute(macro, player);
            macrosToRun.remove(0);//NOTE: a ring buffer would be more efficient
        }

        boolean isLimited = delta >= ModConfig.repeatDelay;
        // loop through all executors and run them.
        this.executorsToRun
                .forEach(executor -> executor.execute(isLimited));

        // remove the command from the pending
        // list if it is not to be re-executed
        this.executorsToRun.clear();

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
            if(macroIsActive(macro)) execute(macro, player);
        }

    }


}
