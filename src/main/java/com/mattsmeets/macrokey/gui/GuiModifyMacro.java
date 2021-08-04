package com.mattsmeets.macrokey.gui;

import com.mattsmeets.macrokey.model.Macro;
import com.mattsmeets.macrokey.model.StringCommand;
import com.mattsmeets.macrokey.MacroKey;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiModifyMacro extends GuiScreen {
    private final GuiScreen parentScreen;

    private final String
            defaultScreenTitleText = I18n.format("gui.modify.text.title.new"),
            editScreenTitleText = I18n.format("gui.modify.text.title.edit"),
            TypeText = I18n.format("gui.modify.text.type"),
            enableCommandText = I18n.format("gui.modify.text.enable"),
            commandBoxTitleText = I18n.format("gui.modify.text.command"),
            keyBoxTitleText = I18n.format("gui.modify.text.key"),
            saveButtonText = I18n.format("gui.modify.text.save");

    private final String
            TrueText = I18n.format("true"),
            FalseText = I18n.format("false"),
            ShiftText = I18n.format("gui.modify.text.shift"),
            CtrlText = I18n.format("gui.modify.text.ctrl"),
            AltText = I18n.format("gui.modify.text.alt"),
            enabledText = I18n.format("enabled"),
            disabledText = I18n.format("disabled"),
            downText = I18n.format("gui.modify.text.down"),
            upText = I18n.format("gui.modify.text.up"),
            repeatText = I18n.format("gui.modify.text.repeat"),
            cancelText = I18n.format("gui.cancel");

    private boolean existing;
    private final Macro result;//NOTE: this macro is only registered if existing = true;

    private GuiTextField command;

    private GuiButton btnKeyBinding;
    private GuiButton typeCommand, commandActive;
    private GuiButton addButton, cancelButton;
    private GuiButton shiftButton, ctrlButton, altButton;

    private boolean changingKey = false;

    public GuiModifyMacro(GuiScreen guiScreen, Macro key) {
        // does the macro already exist, if not create a new one
        if(key == null) {
            this.result = new Macro();
            this.existing = false;
        } else {
            this.result = key;
            this.existing = true;
        }
        this.parentScreen = guiScreen;
    }

    public GuiModifyMacro(GuiScreen guiScreen) {
        this(guiScreen, null);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(this.addButton = new GuiButton(0, this.width / 2 - 155, this.height - 29, 150, 20, saveButtonText));
        this.buttonList.add(this.cancelButton = new GuiButton(1, this.width / 2 - 155 + 160, this.height - 29, 150, 20, cancelText));

        this.buttonList.add(this.btnKeyBinding = new GuiButton(3, this.width / 2 - 75, 100, 150, 20, GameSettings.getKeyDisplayString(0)));

        this.buttonList.add(this.typeCommand = new GuiButton(4, this.width / 2 - 75, 140, 65, 20, downText));
        this.buttonList.add(this.commandActive = new GuiButton(5, this.width / 2 - 75, 162, 65, 20, enabledText));

        this.buttonList.add(this.shiftButton = new GuiButton(11, this.width / 2 + 25, 129, 50, 20, FalseText));
        this.buttonList.add(this.ctrlButton = new GuiButton(12, this.width / 2 + 25, 151, 50, 20, FalseText));
        this.buttonList.add(this.altButton = new GuiButton(13, this.width / 2 + 25, 173, 50, 20, FalseText));

        this.command = new GuiTextField(9, this.fontRenderer, this.width / 2 - 100, 50, 200, 20);
        this.command.setFocused(true);
        this.command.setMaxStringLength(Integer.MAX_VALUE);

        if (this.existing) {
            this.command.setText(result.command.toString());

            this.btnKeyBinding.displayString = GameSettings.getKeyDisplayString(result.keyCode);
            if(result.type == Macro.ONDOWN) {
                this.typeCommand.displayString = downText;
            } else if(result.type == Macro.ONUP) {
                this.typeCommand.displayString = upText;
            } else {
                this.typeCommand.displayString = repeatText;
            }
            this.commandActive.displayString = result.active ? enabledText : disabledText;

            this.shiftButton.displayString = this.result.mustHoldShift ? TrueText : FalseText;
            this.ctrlButton.displayString = this.result.mustHoldCtrl ? TrueText : FalseText;
            this.altButton.displayString = this.result.mustHoldAlt ? TrueText : FalseText;

        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 0:
                if (this.command.getText().length() <= 1) {
                    break;
                }

                this.result.command = command.getText();

                if (!this.existing) {
                    MacroKey.instance.bindingsRepository.addMacro(this.result, true);
                    this.existing = true;
                } else {
                    MacroKey.instance.bindingsRepository.saveConfiguration();
                }
            case 1:
                this.mc.displayGuiScreen(parentScreen);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        // draw title
        this.drawCenteredString(this.fontRenderer, existing ? this.editScreenTitleText : this.defaultScreenTitleText, this.width / 2, 8, 16777215);

        // render add and cancel buttons
        this.addButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.cancelButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);

        // draw keycode as keyboard key
        this.btnKeyBinding.displayString = GameSettings.getKeyDisplayString(this.result.keyCode);

        // this.typeCommand.displayString = this.result.type == Macro.REPEAT_ONDOWN ? enabledText : disabledText;
        // this.commandActive.displayString = this.result.active ? enabledText : disabledText;

        this.typeCommand.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.commandActive.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.shiftButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.ctrlButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.altButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);

        this.command.drawTextBox();

        this.drawString(this.fontRenderer, TypeText, this.width / 2 + 50 - mc.fontRenderer.getStringWidth(TypeText) - 129, 145, -6250336);
        this.drawString(this.fontRenderer, enableCommandText, this.width / 2 + 50 - mc.fontRenderer.getStringWidth(enableCommandText) - 129, 167, -6250336);

        this.drawString(this.fontRenderer, ShiftText, this.width / 2 + 24 - mc.fontRenderer.getStringWidth(ShiftText), 135, -6250336);
        this.drawString(this.fontRenderer, CtrlText, this.width / 2 + 24 - mc.fontRenderer.getStringWidth(CtrlText), 157, -6250336);
        this.drawString(this.fontRenderer, AltText, this.width / 2 + 24 - mc.fontRenderer.getStringWidth(AltText), 179, -6250336);


        this.drawCenteredString(this.fontRenderer, commandBoxTitleText, this.width / 2, 37, -6250336);
        this.drawCenteredString(this.fontRenderer, keyBoxTitleText, this.width / 2, 90, -6250336);

        this.btnKeyBinding.displayString = GameSettings.getKeyDisplayString(this.result.keyCode);


        if (this.changingKey) {
            this.btnKeyBinding.displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnKeyBinding.displayString + TextFormatting.WHITE + " <";
        } else {
            if (this.result.keyCode != 0) {
                boolean macroKeyCodeModifyFlag = false;
                for (KeyBinding keybinding : mc.gameSettings.keyBindings) {
                    if (keybinding.getKeyCode() == this.result.keyCode) {
                        macroKeyCodeModifyFlag = true;
                        break;
                    }
                }
                if (macroKeyCodeModifyFlag) {
                    this.btnKeyBinding.displayString = TextFormatting.GOLD + this.btnKeyBinding.displayString;
                }
            }
        }

        this.btnKeyBinding.drawButton(mc, mouseX, mouseY, 0.0f);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.changingKey) {
            int newCode = -1;
            if (keyCode == Keyboard.KEY_ESCAPE) {
                newCode = 0;
            } else if (keyCode != 0) {
                newCode = keyCode;
            } else if (typedChar > 0) {
                newCode = typedChar + 256;
            }
            if(this.existing) {
                if(newCode != -1) MacroKey.instance.bindingsRepository.changeMacroKeyCode(this.result, newCode, false);
            } else {
                this.result.keyCode = newCode;
            }

            this.changingKey = false;

            return;
        }

        if (this.command.isFocused()) {
            if (keyCode == Keyboard.KEY_ESCAPE)
                this.command.setFocused(false);

            this.command.textboxKeyTyped(typedChar, keyCode);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.command.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.changingKey) {
            this.changingKey = false;
        }

        if (this.btnKeyBinding.mousePressed(mc, mouseX, mouseY)) {
            this.changingKey = true;
        }

        if (this.typeCommand.mousePressed(mc, mouseX, mouseY)) {
            this.result.type = this.result.type%Macro.TOTAL_TYPES + 1;
            if(result.type == Macro.ONDOWN) {
                this.typeCommand.displayString = downText;
            } else if(result.type == Macro.ONUP) {
                this.typeCommand.displayString = upText;
            } else {
                this.typeCommand.displayString = repeatText;
            }
        }

        if (this.commandActive.mousePressed(mc, mouseX, mouseY)) {
            this.result.active = !this.result.active;
            this.commandActive.displayString = result.active ? enabledText : disabledText;
        }

        if(this.shiftButton.mousePressed(mc, mouseX, mouseY)) {
            this.result.mustHoldShift = !this.result.mustHoldShift;
            this.shiftButton.displayString = this.result.mustHoldShift ? TrueText : FalseText;
        }
        if(this.ctrlButton.mousePressed(mc, mouseX, mouseY)) {
            this.result.mustHoldCtrl = !this.result.mustHoldCtrl;
            this.ctrlButton.displayString = this.result.mustHoldCtrl ? TrueText : FalseText;
        }
        if(this.altButton.mousePressed(mc, mouseX, mouseY)) {
            this.result.mustHoldAlt = !this.result.mustHoldAlt;
            this.altButton.displayString = this.result.mustHoldAlt ? TrueText : FalseText;
        }
    }

    public void updateScreen() {
        this.command.updateCursorCounter();
    }
}
