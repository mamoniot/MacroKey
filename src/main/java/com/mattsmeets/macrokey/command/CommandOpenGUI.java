package com.mattsmeets.macrokey.command;

import com.mattsmeets.macrokey.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mattsmeets.macrokey.MacroKey.instance;

public class CommandOpenGUI extends StrippedCommand {

    @Override
    @SideOnly(Side.CLIENT)
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        Minecraft.getMinecraft().player.openGui(
            instance,
            ModConfig.guiMacroManagementId,
            Minecraft.getMinecraft().world,
            (int) Minecraft.getMinecraft().player.posX,
            (int) Minecraft.getMinecraft().player.posY,
            (int) Minecraft.getMinecraft().player.posZ
        );
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> list = new ArrayList<String>();

        return list;
    }
}
