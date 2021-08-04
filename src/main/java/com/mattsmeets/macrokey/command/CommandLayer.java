package com.mattsmeets.macrokey.command;

import com.mattsmeets.macrokey.MacroKey;
import com.mattsmeets.macrokey.model.Layer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandLayer extends StrippedCommand {
    public final String
            layerMasterText = I18n.format("text.layer.master");

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            return;
        }

        if (args.length == 1) {
            this.printLayerInformation(sender);

            return;
        }

        if (args[1].equals("toggle")) {
            this.nextLayer(server, sender, new String[] {args[0]});

            return;
        }

        sender.sendMessage(new TextComponentString(this.getUsage(sender)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> list = new ArrayList<String>();
        list.add("toggle");

        return list;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /macrokey layer [toggle]";
    }

    private void printLayerInformation(ICommandSender sender) {
        Layer activeLayer = MacroKey.instance.bindingsRepository.getActiveLayer(false);

        String layerDisplayName = layerMasterText;
        int countMacroEnabled = 0;

        if (activeLayer != null) {
            layerDisplayName = activeLayer.displayName;
            countMacroEnabled = activeLayer.macros.size();
        } else {
            countMacroEnabled = MacroKey.instance.bindingsRepository.getMacros(false).size();
        }

        sender.sendMessage(
                new TextComponentTranslation(
                        "command.layer.information",
                        layerDisplayName,
                        countMacroEnabled
                )
        );
    }

    private void nextLayer(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        MacroKey.instance.bindingsRepository.setNextActiveLayer(true);

        this.execute(server, sender, args);
    }
}
