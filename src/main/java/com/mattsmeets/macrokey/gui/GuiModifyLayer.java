package com.mattsmeets.macrokey.gui;

import com.mattsmeets.macrokey.model.Layer;
import com.mattsmeets.macrokey.MacroKey;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;

public class GuiModifyLayer extends GuiScreen {

    private final GuiScreen parentScreen;
    private final Layer result;

    private final String
            defaultScreenTitleText = I18n.format("gui.modify.layer.text.title.new"),
            editScreenTitleText = I18n.format("gui.modify.layer.text.title.edit"),
            saveLayerButtonText = I18n.format("gui.modify.layer.text.save");

    private final String
            cancelText = I18n.format("gui.cancel");

    private GuiButton addButton, cancelButton;

    private GuiTextField textFieldName;

    private boolean existing;

    public GuiModifyLayer(GuiScreen guiScreen, Layer layer) {
        this.parentScreen = guiScreen;
        this.result = layer == null ? new Layer() : layer;
        this.existing = layer != null;
    }

    public GuiModifyLayer(GuiScreen guiScreen) {
        this(guiScreen, null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        this.drawCenteredString(this.fontRenderer, !existing ? this.defaultScreenTitleText : this.editScreenTitleText, this.width / 2, 8, 16777215);

        addButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        cancelButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);

        this.textFieldName.drawTextBox();
    }

    public void updateScreen() {
        this.textFieldName.updateCursorCounter();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(addButton = new GuiButton(0, this.width / 2 - 155, this.height - 29, 150, 20, this.saveLayerButtonText));
        this.buttonList.add(cancelButton = new GuiButton(1, this.width / 2 - 155 + 160, this.height - 29, 150, 20, this.cancelText));

        this.textFieldName = new GuiTextField(9, this.fontRenderer, this.width / 2 - 100, 50, 200, 20);
        this.textFieldName.setFocused(true);
        this.textFieldName.setMaxStringLength(20);

        if (existing) {
            textFieldName.setText(result.displayName);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        switch (button.id) {
            case 0:
                if (this.textFieldName.getText().length() <= 1) {
                    break;
                }

                this.result.displayName = this.textFieldName.getText();

                if (!this.existing) {
                    MacroKey.instance.bindingsRepository.addLayer(this.result, true);
                }
            case 1:
                this.mc.displayGuiScreen(parentScreen);
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.textFieldName.isFocused()) {
            this.textFieldName.textboxKeyTyped(typedChar, keyCode);

            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.textFieldName.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
