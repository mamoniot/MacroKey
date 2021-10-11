package com.mattsmeets.macrokey.gui;

import com.mattsmeets.macrokey.model.Macro;
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
    public final GuiScreen parentScreen;

    public static final String
            defaultScreenTitleText = I18n.format("gui.modify.text.title.new"),
            editScreenTitleText = I18n.format("gui.modify.text.title.edit"),
            TypeText = I18n.format("gui.modify.text.type"),
            TriggerTypeText = I18n.format("gui.modify.text.triggertype"),
            toggleText = I18n.format("gui.modify.text.toggle"),
            commandBoxTitleText = I18n.format("gui.modify.text.command"),
            keyBoxTitleText = I18n.format("gui.modify.text.key"),
            saveButtonText = I18n.format("gui.modify.text.save");

    public static final String
            ignoreText = I18n.format("gui.modify.text.ignore"),
            ShiftText = I18n.format("gui.modify.text.shift"),
            CtrlText = I18n.format("gui.modify.text.ctrl"),
            AltText = I18n.format("gui.modify.text.alt"),
            radialText = I18n.format("gui.modify.text.trigger.radial"),
            keyText = I18n.format("gui.modify.text.trigger.key"),
            radialNameText = I18n.format("gui.modify.text.radialname"),
            downText = I18n.format("gui.modify.text.down"),
            upText = I18n.format("gui.modify.text.up"),
            repeatText = I18n.format("gui.modify.text.repeat"),
            bothText = I18n.format("gui.modify.text.both"),
            firstText = I18n.format("gui.modify.text.first"),
            secondText = I18n.format("gui.modify.text.second"),
            cancelText = I18n.format("gui.cancel");

    public boolean existing;
    public final Macro result;//NOTE: this macro is only registered if existing = true;

    public GuiTextField command, radialKey, radialName;

    public GuiButton btnKeyBinding;
    public GuiButton commandType, commandButtonType, commandToggle;
    public GuiButton addButton, cancelButton;
    public GuiButton shiftButton, ctrlButton, altButton;

    public boolean changingKey = false;

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

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(this.addButton = new GuiButton(0, this.width/2 - 155, this.height - 29, 150, 20, saveButtonText));
        this.buttonList.add(this.cancelButton = new GuiButton(1, this.width/2 - 155 + 160, this.height - 29, 150, 20, cancelText));

        this.buttonList.add(this.btnKeyBinding = new GuiButton(3, this.width/2 - 75, 100, 150, 20, GameSettings.getKeyDisplayString(0)));

        this.buttonList.add(this.commandType = new GuiButton(5, this.width/2 - 75, 130, 57, 20, keyText));
        this.buttonList.add(this.commandButtonType = new GuiButton(4, this.width/2 - 75, 152, 57, 20, downText));
        this.buttonList.add(this.commandToggle = new GuiButton(6, this.width/2 - 75, 174, 57, 20, bothText));

        this.buttonList.add(this.shiftButton = new GuiButton(11, this.width/2 + 19, 130, 57, 20, ignoreText));
        this.buttonList.add(this.ctrlButton = new GuiButton(12, this.width/2 + 19, 152, 57, 20, ignoreText));
        this.buttonList.add(this.altButton = new GuiButton(13, this.width/2 + 19, 174, 57, 20, ignoreText));

        this.command = new GuiTextField(9, this.fontRenderer, this.width/2 - 100, 50, 200, 20);
        this.command.setFocused(true);
        this.command.setMaxStringLength(5555);

        this.radialKey = new GuiTextField(9, this.fontRenderer, this.width/2 - 75, 100, 150, 20);
        this.radialKey.setMaxStringLength(15);
        this.radialName = new GuiTextField(9, this.fontRenderer, this.width/2 - 75, 206, 150, 20);
        this.radialName.setMaxStringLength(55);

        if (this.existing) {
            this.command.setText(this.result.command);
            this.radialKey.setText(this.result.radialKey);
            this.radialName.setText(this.result.radialName);
            setKeyText();

            int flags = this.result.flags;

            if((flags&Macro.FLAG_RADIAL) > 0) {
                this.commandType.displayString = radialText;
                this.commandButtonType.enabled = false;
                this.commandToggle.enabled = false;
                this.shiftButton.enabled = false;
                this.ctrlButton.enabled = false;
                this.altButton.enabled = false;
            } else {
                this.commandType.displayString = keyText;
                this.commandButtonType.enabled = true;
                this.commandToggle.enabled = true;
                this.shiftButton.enabled = true;
                this.ctrlButton.enabled = true;
                this.altButton.enabled = true;
            }

            if((flags&Macro.FLAG_ONDOWN) > 0) {
                this.commandButtonType.displayString = downText;
            } else if((flags&Macro.FLAG_ONUP) > 0) {
                this.commandButtonType.displayString = upText;
            } else if((flags&Macro.FLAG_REPEAT_ONDOWN) > 0) {
                this.commandButtonType.displayString = repeatText;
            }

            if((flags&Macro.FLAG_NOTONEVEN) > 0) {
                this.commandToggle.displayString = firstText;
            } else if((flags&Macro.FLAG_NOTONODD) > 0) {
                this.commandToggle.displayString = secondText;
            } else {
                this.commandToggle.displayString = bothText;
            }

            this.shiftButton.displayString = getAltDisplay(flags, Macro.FLAG_SHIFT_DOWN);
            this.ctrlButton.displayString = getAltDisplay(flags, Macro.FLAG_CTRL_DOWN);
            this.altButton.displayString = getAltDisplay(flags, Macro.FLAG_ALT_DOWN);
        }
    }
    public static final String getAltDisplay(int flags, int flag) {
        if((flags&flag) > 0) {
            return downText;
        } else if((flags&(flag<<1)) > 0) {
            return upText;
        } else {
            return ignoreText;
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


        // this.commandButtonType.displayString = this.result.type == Macro.REPEAT_ONDOWN ? radialText : keyText;
        // this.commandType.displayString = this.result.active ? radialText : keyText;

        this.command.drawTextBox();

        if((this.result.flags&Macro.FLAG_RADIAL) > 0) {
            this.radialKey.drawTextBox();
            this.radialName.drawTextBox();
            this.drawCenteredString(this.fontRenderer, radialNameText, this.width/2, 194, -6250336);
        } else {
            this.btnKeyBinding.drawButton(mc, mouseX, mouseY, 0.0f);
        }

        this.commandType.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.commandButtonType.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.commandToggle.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.shiftButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.ctrlButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);
        this.altButton.drawButton(parentScreen.mc, mouseX, mouseY, 0.0f);


        this.drawString(this.fontRenderer, TriggerTypeText, this.width/2 - 78 - mc.fontRenderer.getStringWidth(TriggerTypeText), 136, -6250336);
        this.drawString(this.fontRenderer, TypeText, this.width/2 - 78 - mc.fontRenderer.getStringWidth(TypeText), 158, -6250336);
        this.drawString(this.fontRenderer, toggleText, this.width/2 - 78 - mc.fontRenderer.getStringWidth(toggleText), 180, -6250336);

        this.drawString(this.fontRenderer, ShiftText, this.width/2 + 16 - mc.fontRenderer.getStringWidth(ShiftText), 136, -6250336);
        this.drawString(this.fontRenderer, CtrlText, this.width/2 + 16 - mc.fontRenderer.getStringWidth(CtrlText), 158, -6250336);
        this.drawString(this.fontRenderer, AltText, this.width/2 + 16 - mc.fontRenderer.getStringWidth(AltText), 180, -6250336);


        this.drawCenteredString(this.fontRenderer, commandBoxTitleText, this.width/2, 37, -6250336);
        this.drawCenteredString(this.fontRenderer, keyBoxTitleText, this.width/2, 88, -6250336);

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
            if(newCode != -1) {
                if(this.existing) {
                    MacroKey.instance.bindingsRepository.changeMacroKeyCode(this.result, newCode, false);
                } else {
                    this.result.keyCode = newCode;
                }
            }

            this.changingKey = false;
            setKeyText();
        } else {
            if (this.command.isFocused()) {
                if (keyCode == Keyboard.KEY_ESCAPE) this.command.setFocused(false);

                this.command.textboxKeyTyped(typedChar, keyCode);
            } else if (this.radialKey.isFocused()) {
                if (keyCode == Keyboard.KEY_ESCAPE) this.radialKey.setFocused(false);

                this.radialKey.textboxKeyTyped(typedChar, keyCode);

                if((this.result.flags&Macro.FLAG_RADIAL) > 0) {
                    String str = radialKey.getText();
                    if(this.existing) {
                        MacroKey.instance.bindingsRepository.changeMacroRadialKey(this.result, str, false);
                    } else {
                        this.result.radialKey = str;
                    }
                } else {
                    this.radialKey.setFocused(false);
                }
            } else if (this.radialName.isFocused()) {
                if (keyCode == Keyboard.KEY_ESCAPE) this.radialName.setFocused(false);

                this.radialName.textboxKeyTyped(typedChar, keyCode);

                if((this.result.flags&Macro.FLAG_RADIAL) > 0) {
                    String str = radialName.getText();
                    this.result.radialName = str;
                } else {
                    this.radialName.setFocused(false);
                }
            } else {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.command.mouseClicked(mouseX, mouseY, mouseButton);
        this.radialKey.mouseClicked(mouseX, mouseY, mouseButton);
        this.radialName.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.changingKey) {
            this.changingKey = false;
            setKeyText();
        }

        if ((this.result.flags&Macro.FLAG_RADIAL) <= 0) {
            if (this.btnKeyBinding.mousePressed(mc, mouseX, mouseY)) {
                this.changingKey = true;
                setKeyText();
            }
        }

        if (this.commandButtonType.mousePressed(mc, mouseX, mouseY)) {
            if((this.result.flags&Macro.FLAG_ONDOWN) > 0) {
                this.result.flags &= ~Macro.FLAG_ONDOWN;
                this.result.flags |= Macro.FLAG_ONUP;

                this.commandButtonType.displayString = upText;
            } else if((this.result.flags&Macro.FLAG_ONUP) > 0) {
                this.result.flags &= ~Macro.FLAG_ONUP;
                this.result.flags |= Macro.FLAG_REPEAT_ONDOWN;

                this.commandButtonType.displayString = repeatText;
            } else if((this.result.flags&Macro.FLAG_REPEAT_ONDOWN) > 0) {
                this.result.flags &= ~Macro.FLAG_REPEAT_ONDOWN;
                this.result.flags |= Macro.FLAG_ONDOWN;

                this.commandButtonType.displayString = downText;
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

        if(this.commandType.mousePressed(mc, mouseX, mouseY)) {
            if((this.result.flags&Macro.FLAG_RADIAL) > 0) {
                if(this.existing) {
                    MacroKey.instance.bindingsRepository.changeMacroKeyCode(this.result, this.result.keyCode, false);
                } else {
                    this.result.flags ^= Macro.FLAG_RADIAL;
                }
                this.commandType.displayString = keyText;
                this.commandButtonType.enabled = true;
                this.commandToggle.enabled = true;
                this.shiftButton.enabled = true;
                this.ctrlButton.enabled = true;
                this.altButton.enabled = true;
            } else {
                if(this.existing) {
                    MacroKey.instance.bindingsRepository.changeMacroRadialKey(this.result, this.result.radialKey, false);
                } else {
                    this.result.flags ^= Macro.FLAG_RADIAL;
                }
                this.commandType.displayString = radialText;
                this.commandButtonType.enabled = false;
                this.commandToggle.enabled = false;
                this.shiftButton.enabled = false;
                this.ctrlButton.enabled = false;
                this.altButton.enabled = false;
            }
        }

        if(this.shiftButton.mousePressed(mc, mouseX, mouseY)) {
            setAltDisplay(this.shiftButton, Macro.FLAG_SHIFT_DOWN);
        } else if(this.ctrlButton.mousePressed(mc, mouseX, mouseY)) {
            setAltDisplay(this.ctrlButton, Macro.FLAG_CTRL_DOWN);
        } else if(this.altButton.mousePressed(mc, mouseX, mouseY)) {
            setAltDisplay(this.altButton, Macro.FLAG_ALT_DOWN);
        }
    }

    public void setAltDisplay(GuiButton button, int flag) {
        if((this.result.flags&flag) > 0) {
            this.result.flags &= ~flag;
            this.result.flags |= flag<<1;

            button.displayString = upText;
        } else if((this.result.flags&(flag<<1)) > 0) {
            this.result.flags &= ~(flag<<1);

            button.displayString = ignoreText;
        } else {
            this.result.flags |= flag;

            button.displayString = downText;
        }
    }

    public void updateScreen() {
        this.command.updateCursorCounter();
    }


    public void setKeyText() {
        this.btnKeyBinding.displayString = GameSettings.getKeyDisplayString(this.result.keyCode);

        if(this.changingKey) {
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
    }
}
