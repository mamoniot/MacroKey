package com.mattsmeets.macrokey.gui.fragment;

import com.mattsmeets.macrokey.gui.GuiMacroManagement;
import com.mattsmeets.macrokey.gui.GuiModifyMacro;
import com.mattsmeets.macrokey.model.Layer;
import com.mattsmeets.macrokey.model.Macro;
import com.mattsmeets.macrokey.MacroKey;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.HashMap;
import java.util.Comparator;

import static com.mattsmeets.macrokey.MacroKey.instance;

public class MacroListFragment extends GuiListExtended {

    private final GuiMacroManagement guiMacroManagement;
    private final ArrayList<GuiListExtended.IGuiListEntry> listEntries;

    private final UUID currentLayerulid;

    public static class SortMacro implements Comparator<GuiListExtended.IGuiListEntry> {//java is dumb
        public static final SortMacro instance = new SortMacro();
        public int compare(GuiListExtended.IGuiListEntry a_, GuiListExtended.IGuiListEntry b_)
        {
            MacroListFragment.KeyEntry a = (MacroListFragment.KeyEntry)a_;
            MacroListFragment.KeyEntry b = (MacroListFragment.KeyEntry)b_;
            if((a.macro.flags&Macro.FLAG_RADIAL) > 0) {
                if((b.macro.flags&Macro.FLAG_RADIAL) > 0) {
                    return a.macro.radialKey.compareTo(b.macro.radialKey);
                } else {
                    return 1;
                }
            } else {
                if((b.macro.flags&Macro.FLAG_RADIAL) > 0) {
                    return -1;
                } else {
                    String as = GameSettings.getKeyDisplayString(a.macro.keyCode);
                    String bs = GameSettings.getKeyDisplayString(b.macro.keyCode);
                    return as.compareTo(bs);
                }
            }
        }
    }

    public MacroListFragment(GuiMacroManagement guiMacroManagement, UUID layer) {
        super(guiMacroManagement.mc, guiMacroManagement.width + 45, guiMacroManagement.height, 63, guiMacroManagement.height - 32, 20);

        this.guiMacroManagement = guiMacroManagement;
        this.currentLayerulid = layer;

        instance.bindingsRepository.loadConfiguration();
        HashMap<Integer, ArrayList<Macro>> macros = instance.bindingsRepository.keyMacros;

        this.listEntries = new ArrayList<GuiListExtended.IGuiListEntry>();

        for (HashMap.Entry<Integer, ArrayList<Macro>> entry : macros.entrySet()) {
            // int keycode = entry.getKey();
            ArrayList<Macro> ms = entry.getValue();
            for(int i = 0; i < ms.size(); i++) {
                Macro macro = ms.get(i);
                this.listEntries.add(new MacroListFragment.KeyEntry(macro));
            }
        }

        HashMap<String, ArrayList<Macro>> radialMacros = instance.bindingsRepository.radialMacros;

        for (HashMap.Entry<String, ArrayList<Macro>> entry : radialMacros.entrySet()) {
            // String radialKey = entry.getKey();
            ArrayList<Macro> ms = entry.getValue();
            for(int i = 0; i < ms.size(); i++) {
                Macro macro = ms.get(i);
                this.listEntries.add(new MacroListFragment.KeyEntry(macro));
            }
        }
        this.listEntries.sort(SortMacro.instance);
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return this.listEntries.get(index);
    }

    @Override
    protected int getSize() {
        return this.listEntries.size();
    }


    @SideOnly(Side.CLIENT)
    private class KeyEntry implements GuiListExtended.IGuiListEntry {

        public final Macro macro;

        public final GuiButton
        btnChangeKeyBinding,
        btnRemoveKeyBinding,
        btnEdit,
        btnEnabledInLayer;
        public final String
        removeMacroText = I18n.format("fragment.list.text.remove"),
        editMacroText = I18n.format("edit");
        public final String
        enabledText = I18n.format("enabled"),
        disabledText = I18n.format("disabled");
        public boolean enabledInLayer;
        public boolean deleted = false;

        public KeyEntry(Macro macro) {
            this.macro = macro;

            this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 75, 20, macro.command.toString());
            this.btnRemoveKeyBinding = new GuiButton(1, 0, 0, 15, 20, this.removeMacroText);
            this.btnEdit = new GuiButton(2, 0, 0, 30, 20, this.editMacroText);
            this.btnEnabledInLayer = new GuiButton(3, 0, 0, 75, 20, this.disabledText);

            Layer currentLayer = instance.bindingsRepository.getLayer(currentLayerulid, false);
            if (currentLayer != null) {
                enabledInLayer = currentLayer.macros.contains(this.macro.umid);
            }
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float f) {
            if (this.deleted) {
                return;
            }

            boolean macroKeyCodeModifyFlag = this.macro.equals(guiMacroManagement.macroModify);

            mc.fontRenderer.drawString(this.macro.command, x + 90 - mc.fontRenderer.getStringWidth(macro.command), y + 1 + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 16777215);

            if (currentLayerulid == null) {
                this.btnChangeKeyBinding.x = x + 95;
                this.btnChangeKeyBinding.y = y;
                if((this.macro.flags&Macro.FLAG_RADIAL) > 0) {
                    this.btnChangeKeyBinding.displayString = this.macro.radialKey;
                } else {
                    this.btnChangeKeyBinding.displayString = GameSettings.getKeyDisplayString(this.macro.keyCode);
                }

                this.btnEdit.x = x + 170;
                this.btnEdit.y = y;
                this.btnEdit.drawButton(mc, mouseX, mouseY, 0.0f);

                this.btnRemoveKeyBinding.x = x + 200;
                this.btnRemoveKeyBinding.y = y;
                this.btnRemoveKeyBinding.enabled = true;
                this.btnRemoveKeyBinding.drawButton(mc, mouseX, mouseY, 0.0f);
            } else {
                this.btnEnabledInLayer.x = x + 95;
                this.btnEnabledInLayer.y = y;

                if (enabledInLayer) {
                    this.btnEnabledInLayer.displayString = this.enabledText;
                } else {
                    this.btnEnabledInLayer.displayString = this.disabledText;
                }

                this.btnEnabledInLayer.drawButton(mc, mouseX, mouseY, 0.0f);
            }

            boolean currentKeyAlreadyUsedFlag = false;

            if (this.macro.keyCode != 0) {
                for (KeyBinding keybinding : mc.gameSettings.keyBindings) {
                    if (keybinding.getKeyCode() == this.macro.keyCode) {
                        currentKeyAlreadyUsedFlag = true;
                        break;
                    }
                }
            }

            if (macroKeyCodeModifyFlag) {
                this.btnChangeKeyBinding.displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.displayString + TextFormatting.WHITE + " <";
            } else if (currentKeyAlreadyUsedFlag) {
                this.btnChangeKeyBinding.displayString = TextFormatting.GOLD + this.btnChangeKeyBinding.displayString;
            }

            this.btnChangeKeyBinding.drawButton(mc, mouseX, mouseY, 0.0f);
        }


        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            if (this.btnEdit.mousePressed(mc, mouseX, mouseY)) {
                mc.displayGuiScreen(new GuiModifyMacro(guiMacroManagement, this.macro));

                return true;
            }
            if (this.btnChangeKeyBinding.mousePressed(mc, mouseX, mouseY)) {
                if((this.macro.flags&Macro.FLAG_RADIAL) == 0) {
                    guiMacroManagement.macroModify = this.macro;
                    return true;
                } else {
                    return false;
                }
            }
            if (this.btnRemoveKeyBinding.mousePressed(mc, mouseX, mouseY)) {
                try {
                    instance.bindingsRepository.deleteMacro(this.macro, true);

                    this.deleted = true;
                } finally {
                    mc.displayGuiScreen(guiMacroManagement);
                }

                return true;
            }
            if (this.btnEnabledInLayer.mousePressed(mc, mouseX, mouseY)) {
                Layer currentLayer = instance.bindingsRepository.getLayer(currentLayerulid, false);
                if(currentLayer != null) {
                    enabledInLayer = !enabledInLayer;
                    if (enabledInLayer) {
                        currentLayer.macros.add(this.macro.umid);
                    } else {
                        currentLayer.macros.remove(this.macro.umid);
                    }
                    instance.bindingsRepository.saveConfiguration();

                    return true;
                }
            }

            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.btnChangeKeyBinding.mouseReleased(x, y);
            this.btnEdit.mouseReleased(x, y);
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {
        }

    }
}
