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

    // private stash of pressed keys
    private Set<Integer> pressedKeys;

    public KeyInputHandler() {
        this.pressedKeys = new HashSet<>();
    }

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
        ArrayList<Macro> macros =
                instance.bindingsRepository.findMacroByKeycode(keyCode, instance.bindingsRepository.getActiveLayer(false), false);

        // if the list is not empty
        if (macros == null || macros.size() == 0) {
            return;
        }

        if (Keyboard.getEventKeyState() && !this.pressedKeys.contains(keyCode)) {
            /*
             * if the key has not been pressed during last events, send
             * an event, and add it to the current index of pressed keys
             */
            onKeyEvent(true, macros);
            this.pressedKeys.add(keyCode);
        } else if (!Keyboard.getEventKeyState() && this.pressedKeys.contains(keyCode)) {
            /*
             * if the key has been pressed during last events, send
             * an event, and remove it from the current index of pressed keys
             */
            onKeyEvent(false, macros);
            this.pressedKeys.remove(keyCode);
        }//NOTE: this may break if a key is held down while it is added as a macro
    }



    private ArrayList<Macro> macrosToRun = new ArrayList<Macro>();
    private ArrayList<Macro> macrosToRepeat = new ArrayList<Macro>();

    private Set<ExecuteOnTickInterface> executorsToRun = new HashSet<>();

    public void onKeyEvent(boolean ispressed, ArrayList<Macro> macros) {
        //NOTE: this code can break if a repeat macro is changed while a key is being held down
        for(Macro macro : macros) {
            if(macro.type == Macro.ONDOWN) {
                if(ispressed) {
                    this.macrosToRun.add(macro);
                }
            } else if(macro.type == Macro.ONUP) {
                if(!ispressed) {
                    this.macrosToRun.add(macro);
                }
            } else if(macro.type == Macro.REPEAT_ONDOWN) {
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

    private int delta = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        // check if we are in-game
        if (player == null) {
            return;
        }

        // every tick post an event for the normal,
        // non repeating commands to trigger
        for(int i = 0; i < macrosToRun.size(); i++) {
            Macro macro = macrosToRun.get(i);
            boolean cancelled = !macro.active
                || (macro.mustHoldShift && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                || (macro.mustHoldCtrl && !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
                || (macro.mustHoldAlt && !Keyboard.isKeyDown(Keyboard.KEY_LMENU));
            if(!cancelled) macro.execute(player);
        }
        this.macrosToRun.clear();

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
            boolean cancelled = !macro.active
                || (macro.mustHoldShift && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                || (macro.mustHoldCtrl && !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
                || (macro.mustHoldAlt && !Keyboard.isKeyDown(Keyboard.KEY_LMENU));
            if(!cancelled) macro.execute(player);
        }

    }


}
