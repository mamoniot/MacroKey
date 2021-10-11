package com.mattsmeets.macrokey.handler.hook;

import com.mattsmeets.macrokey.MacroKey;
import com.mattsmeets.macrokey.config.ModConfig;
import com.mattsmeets.macrokey.model.Layer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;

import static com.mattsmeets.macrokey.MacroKey.instance;

public class GuiEventHandler {

    // private String
    //         layerMasterText = I18n.format("text.layer.master");

    private GuiButton switchButton = null;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void event(GuiScreenEvent.InitGuiEvent event) {
        GuiScreen gui = event.getGui();
        // check if the current GUI is the in-game menu
        // and if the layer switch button is not disabled
        if (!(event.getGui() instanceof GuiIngameMenu) || ModConfig.buttonLayerSwitcherId == -1) {
            return;
        }

        // Layer layer = instance.bindingsRepository.getActiveLayer(false);
        // render the layer switcher button
        event.getButtonList().add(
                switchButton = new GuiButton(
                        ModConfig.buttonLayerSwitcherId,
                        gui.width / 2 + ModConfig.buttonLayerSwitchSettings[0],
                        gui.height / 4 + ModConfig.buttonLayerSwitchSettings[1],
                        ModConfig.buttonLayerSwitchSettings[2],
                        ModConfig.buttonLayerSwitchSettings[3],
                        I18n.format("gui.macrokey.open")
                ));
    }

    @SubscribeEvent
    public void postActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (!(event.getGui() instanceof GuiIngameMenu)
                || event.getButton().id != ModConfig.buttonLayerSwitcherId) {
            return;
        }

        // Layer layer = MacroKey.instance.bindingsRepository.setNextActiveLayer(true);

        // event.getButton().displayString = I18n.format("gui.macrokey.open");

        Minecraft.getMinecraft().player.openGui(
            instance,
            ModConfig.guiMacroManagementId,
            Minecraft.getMinecraft().world,
            (int) Minecraft.getMinecraft().player.posX,
            (int) Minecraft.getMinecraft().player.posY,
            (int) Minecraft.getMinecraft().player.posZ
        );
    }

    // @SubscribeEvent(receiveCanceled = true)
    // @SideOnly(Side.CLIENT)
    // public void mouseInputEvent(GuiScreenEvent.MouseInputEvent.Post event) {
    //     if (!(event.getGui() instanceof GuiIngameMenu) ||
    //             ModConfig.buttonLayerSwitcherId == -1 ||
    //             switchButton == null ||
    //             !switchButton.isMouseOver()) {
    //         return;
    //     }

    //     if (Mouse.getEventButton() != 0 && !Mouse.isButtonDown(0)) {
    //         return;
    //     }
    // }


    //tooltip script
    // @SubscribeEvent(receiveCanceled = true)
    // @SideOnly(Side.CLIENT)
    // public void render(GuiScreenEvent.DrawScreenEvent.Post event) {
    //     if (!(event.getGui() instanceof GuiIngameMenu) ||
    //             ModConfig.buttonLayerSwitcherId == -1 ||
    //             switchButton == null ||
    //             !switchButton.isMouseOver()) {
    //         return;
    //     }

    //     GuiScreen screen = event.getGui();

    //     screen.drawHoveringText(
    //             I18n.format("text.layer.hover.right_click"),
    //             Mouse.getEventX() / 2,
    //             screen.height - (Mouse.getY() / 2)
    //     );
    // }
}
