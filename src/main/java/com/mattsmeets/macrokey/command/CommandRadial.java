package com.mattsmeets.macrokey.command;

import com.mattsmeets.macrokey.model.Macro;
import com.mattsmeets.macrokey.model.Layer;
import com.mattsmeets.macrokey.gui.GuiRadialMenu;
import static com.mattsmeets.macrokey.MacroKey.instance;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CommandRadial extends CommandBase implements ICommand {
    // public static final Macro radial = new Macro();

    @Override
    public String getName() {
        return "radial";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage (only works when bound to a key in SneakyKeys): /radial [<trigger key>]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 0) {
            sender.sendMessage(new TextComponentString(this.getUsage(sender)));

            return;
        } else {
            String radialKey = args[0];
            // radial.command = "/radial " + radialKey;
            // this.macrosToRun.add(macro);

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
        }

    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        // if (args.length >= 1 && this.subCommands.containsKey(args[0].toLowerCase())) {
        //     return this.subCommands.get(args[0].toLowerCase()).getTabCompletions(server, sender, args, targetPos);
        // }
        ArrayList<String> list = new ArrayList<String>();

        String start = "";
        if (args.length > 0) {
            start = args[args.length - 1];
        }

        for (String key : instance.bindingsRepository.radialMacros.keySet()) {
            if(key.length() > 0 && key.startsWith(start)) {
                list.add(key);
            }
        }
        Collections.sort(list);
        return list;
    }
}
