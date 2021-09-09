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
import java.util.UUID;

public class GuiModifyMacro extends GuiScreen {
    private final GuiScreen parentScreen;

    private final String
            defaultScreenTitleText = I18n.format("gui.modify.text.title.new"),
            editScreenTitleText = I18n.format("gui.modify.text.title.edit"),
            TypeText = I18n.format("gui.modify.text.type"),
            enableCommandText = I18n.format("gui.modify.text.enable"),
            toggleText = I18n.format("gui.modify.text.toggle"),
            commandBoxTitleText = I18n.format("gui.modify.text.command"),
            keyBoxTitleText = I18n.format("gui.modify.text.key"),
            saveButtonText = I18n.format("gui.modify.text.save");

    private final String
            TrueText = I18n.format("gui.modify.text.hold"),
            FalseText = I18n.format("gui.modify.text.ignore"),
            ShiftText = I18n.format("gui.modify.text.shift"),
            CtrlText = I18n.format("gui.modify.text.ctrl"),
            AltText = I18n.format("gui.modify.text.alt"),
            enabledText = I18n.format("enabled"),
            disabledText = I18n.format("disabled"),
            downText = I18n.format("gui.modify.text.down"),
            upText = I18n.format("gui.modify.text.up"),
            repeatText = I18n.format("gui.modify.text.repeat"),
            bothText = I18n.format("gui.modify.text.both"),
            firstText = I18n.format("gui.modify.text.first"),
            secondText = I18n.format("gui.modify.text.second"),
            cancelText = I18n.format("gui.cancel");

    private boolean existing;
    private final Macro result;//NOTE: this macro is only registered if existing = true;

    private GuiTextField command;

    private GuiButton btnKeyBinding;
    private GuiButton commandActive, commandType, commandToggle;
    private GuiButton addButton, cancelButton;
    private GuiButton shiftButton, ctrlButton, altButton;

    private boolean changingKey = false;

    public GuiModifyMacro(GuiScreen guiScreen, Macro key) {
        // does the macro already exist, if not create a new one
        if(key == null) {
            this.result = new Macro();
            this.result.umid = UUID.randomUUID();
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
        this.buttonList.add(this.addButton = new GuiButton(0, this.width/2 - 155, this.height - 29, 150, 20, saveButtonText));
        this.buttonList.add(this.cancelButton = new GuiButton(1, this.width/2 - 155 + 160, this.height - 29, 150, 20, cancelText));

        this.buttonList.add(this.btnKeyBinding = new GuiButton(3, this.width/2 - 75, 100, 150, 20, GameSettings.getKeyDisplayString(0)));

        this.buttonList.add(this.commandActive = new GuiButton(5, this.width/2 - 75, 130, 65, 20, enabledText));
        this.buttonList.add(this.commandType = new GuiButton(4, this.width/2 - 75, 152, 65, 20, downText));
        this.buttonList.add(this.commandToggle = new GuiButton(6, this.width/2 - 75, 174, 65, 20, bothText));

        this.buttonList.add(this.shiftButton = new GuiButton(11, this.width/2 + 25, 130, 50, 20, FalseText));
        this.buttonList.add(this.ctrlButton = new GuiButton(12, this.width/2 + 25, 152, 50, 20, FalseText));
        this.buttonList.add(this.altButton = new GuiButton(13, this.width/2 + 25, 174, 50, 20, FalseText));

        this.command = new GuiTextField(9, this.fontRenderer, this.width/2 - 100, 50, 200, 20);
        this.command.setFocused(true);
        this.command.setMaxStringLength(Integer.MAX_VALUE);

        if (this.existing) {
            this.command.setText(this.result.command.toString());
            int flags = this.result.flags;

            this.btnKeyBinding.displayString = GameSettings.getKeyDisplayString(result.keyCode);

            this.commandActive.displayString = (flags&Macro.FLAG_ACTIVE) > 0 ? enabledText : disabledText;
            if((flags&Macro.FLAG_ONDOWN) > 0) {
                this.commandType.displayString = downText;
            } else if((flags&Macro.FLAG_ONUP) > 0) {
                this.commandType.displayString = upText;
            } else if((flags&Macro.FLAG_REPEAT_ONDOWN) > 0) {
                this.commandType.displayString = repeatText;
            }
            if((flags&Macro.FLAG_NOTONEVEN) > 0) {
                this.commandToggle.displayString = firstText;
            } else if((flags&Macro.FLAG_NOTONODD) > 0) {
                this.commandToggle.displayString = secondText;
            } else {
                this.commandToggle.displayString = bothText;
            }

            this.shiftButton.displayString = (flags&Macro.FLAG_SHIFT) > 0 ? TrueText : FalseText;
            this.ctrlButton.displayString = (flags&Macro.FLAG_CTRL) > 0 ? TrueText : FalseText;
            this.altButton.displayString = (flags&Macro.FLAG_ALT) > 0 ? TrueText : FalseText;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button.id == 0) {
            this.result.command = command.getText();
            if(!this.existing) {
                this.existing = true;
                MacroKey.instance.bindingsRepository.addMacro(this.result, true);
            } else {
                MacroKey.instance.bindingsRepository.saveConfiguration();
            }
            this.mc.displayGuiScreen(parentScreen);
        } else if(button.id == 1) {
            this.mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        // draw title
        this.drawCenteredString(this.fontRenderer, existing ? this.editScreenTitleText : this.defaultScreenTitleText, this.width/2, 8, 16777215);

        // render add and cancel buttons
        this.addButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.cancelButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);

        // draw keycode as keyboard key
        this.btnKeyBinding.displayString = GameSettings.getKeyDisplayString(this.result.keyCode);

        // this.commandType.displayString = this.result.type == Macro.REPEAT_ONDOWN ? enabledText : disabledText;
        // this.commandActive.displayString = this.result.active ? enabledText : disabledText;

        this.commandActive.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.commandType.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.commandToggle.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.shiftButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.ctrlButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.altButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);

        this.command.drawTextBox();

        this.drawString(this.fontRenderer, enableCommandText, this.width/2 + 51 - mc.fontRenderer.getStringWidth(enableCommandText) - 129, 136, -6250336);
        this.drawString(this.fontRenderer, TypeText, this.width/2 + 51 - mc.fontRenderer.getStringWidth(TypeText) - 129, 158, -6250336);
        this.drawString(this.fontRenderer, toggleText, this.width/2 + 51 - mc.fontRenderer.getStringWidth(toggleText) - 129, 180, -6250336);

        this.drawString(this.fontRenderer, ShiftText, this.width/2 + 23 - mc.fontRenderer.getStringWidth(ShiftText), 136, -6250336);
        this.drawString(this.fontRenderer, CtrlText, this.width/2 + 23 - mc.fontRenderer.getStringWidth(CtrlText), 158, -6250336);
        this.drawString(this.fontRenderer, AltText, this.width/2 + 23 - mc.fontRenderer.getStringWidth(AltText), 180, -6250336);


        this.drawCenteredString(this.fontRenderer, commandBoxTitleText, this.width/2, 37, -6250336);
        this.drawCenteredString(this.fontRenderer, keyBoxTitleText, this.width/2, 90, -6250336);

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

        if (this.commandType.mousePressed(mc, mouseX, mouseY)) {
            if((this.result.flags&Macro.FLAG_ONDOWN) > 0) {
                this.result.flags &= ~Macro.FLAG_ONDOWN;
                this.result.flags |= Macro.FLAG_ONUP;

                this.commandType.displayString = upText;
            } else if((this.result.flags&Macro.FLAG_ONUP) > 0) {
                this.result.flags &= ~Macro.FLAG_ONUP;
                this.result.flags |= Macro.FLAG_REPEAT_ONDOWN;

                this.commandType.displayString = repeatText;
            } else if((this.result.flags&Macro.FLAG_REPEAT_ONDOWN) > 0) {
                this.result.flags &= ~Macro.FLAG_REPEAT_ONDOWN;
                this.result.flags |= Macro.FLAG_ONDOWN;

                this.commandType.displayString = downText;
            }
        }

        if (this.commandToggle.mousePressed(mc, mouseX, mouseY)) {
            if((this.result.flags&Macro.FLAG_NOTONEVEN) > 0) {
                this.result.flags &= ~Macro.FLAG_NOTONEVEN;
                this.result.flags |= Macro.FLAG_NOTONODD;

                this.commandToggle.displayString = secondText;
            } else if((this.result.flags&Macro.FLAG_NOTONODD) > 0) {
                this.result.flags &= ~Macro.FLAG_NOTONODD;

                this.commandToggle.displayString = bothText;
            } else {
                this.result.flags |= Macro.FLAG_NOTONEVEN;

                this.commandToggle.displayString = firstText;
            }
        }


        if(this.commandActive.mousePressed(mc, mouseX, mouseY)) {
            this.result.flags ^= Macro.FLAG_ACTIVE;
            this.commandActive.displayString = (this.result.flags&Macro.FLAG_ACTIVE) > 0 ? enabledText : disabledText;
        }
        if(this.shiftButton.mousePressed(mc, mouseX, mouseY)) {
            this.result.flags ^= Macro.FLAG_SHIFT;
            this.shiftButton.displayString = (this.result.flags&Macro.FLAG_SHIFT) > 0 ? TrueText : FalseText;
        }
        if(this.ctrlButton.mousePressed(mc, mouseX, mouseY)) {
            this.result.flags ^= Macro.FLAG_CTRL;
            this.ctrlButton.displayString = (this.result.flags&Macro.FLAG_CTRL) > 0 ? TrueText : FalseText;
        }
        if(this.altButton.mousePressed(mc, mouseX, mouseY)) {
            this.result.flags ^= Macro.FLAG_ALT;
            this.altButton.displayString = (this.result.flags&Macro.FLAG_ALT) > 0 ? TrueText : FalseText;
        }
    }

    public void updateScreen() {
        this.command.updateCursorCounter();
    }
}
