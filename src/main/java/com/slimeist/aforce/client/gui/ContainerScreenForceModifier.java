package com.slimeist.aforce.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.gui.ie_elements.GuiButtonCheckbox;
import com.slimeist.aforce.client.gui.ie_elements.GuiButtonIE;
import com.slimeist.aforce.client.gui.ie_elements.GuiReactiveList;
import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;

import com.slimeist.aforce.common.tiles.ForceModifierTileEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//Heavily inspired from IE Turret

public class ContainerScreenForceModifier extends ContainerScreen<ContainerForceModifier> {

    public ForceModifierTileEntity tile;
    private TextFieldWidget nameField;
    private ContainerForceModifier containerForceModifier;
    public ContainerScreenForceModifier(ContainerForceModifier containerForceModifier, PlayerInventory playerInventory, ITextComponent title) {
        super(containerForceModifier, playerInventory, title);
        this.containerForceModifier = containerForceModifier;

        // Set the width and height of the gui.  Should match the size of the texture!
        imageWidth = 233;
        imageHeight = 211;
    }

    //ADJUST TO IMAGE

    final static  int FONT_Y_SPACING = 10;
    final static  int PLAYER_INV_LABEL_XPOS = ContainerForceModifier.PLAYER_INVENTORY_XPOS;
    final static  int PLAYER_INV_LABEL_YPOS = ContainerForceModifier.PLAYER_INVENTORY_YPOS - FONT_Y_SPACING;

    @Override
    public void init() {
        super.init();
        ClientUtils.mc().keyboardHandler.setSendRepeatsToGui(true);
        this.nameField = new TextFieldWidget(this.font, leftPos+11, topPos+88, 58, 12, StringTextComponent.EMPTY);
        this.nameField.setTextColor(-1);
        this.nameField.setTextColorUneditable(-1);
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(30);

        this.buttons.clear();
        this.addButton(new GuiReactiveList(this, leftPos+10, topPos+10, 60, 72,
                btn -> {
                    GuiReactiveList list = (GuiReactiveList)btn;
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    int rem = list.selectedOption;
                    if(rem >= 0&&tile.targetList.size() > 0)
                    {
                        tile.targetList.remove(rem);
                        tag.putInt("remove", rem);
                        listOffset = list.getOffset()-1;
                        handleButtonClick(tag, listOffset);
                    }
                }, tile.targetList.toArray(new String[0]))
                .setPadding(0, 0, 2, 2));
        this.addButton(new GuiButtonIE(leftPos+74, topPos+84, 24, 16, new TranslationTextComponent("gui.turret.add"), TEXTURE, 176, 65,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    String name = nameField.getValue();
                    if(!tile.targetList.contains(name))
                    {
                        listOffset = ((GuiReactiveList)buttons.get(0)).getMaxOffset();
                        tag.putString("add", name);
                        tile.targetList.add(name);
                    }
                    nameField.setValue("");
                    handleButtonClick(tag, listOffset);
                }));
        this.addButton(new GuiButtonCheckbox(leftPos+74, topPos+10, I18n.get("gui.turret.blacklist"), !tile.whitelist,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.whitelist = btn.getState();
                    tag.putBoolean("whitelist", tile.whitelist);
                    handleButtonClick(tag, listOffset);
                }));
        this.addButton(new GuiButtonCheckbox(leftPos+74, topPos+26, I18n.get("gui.turret.animals"), tile.attackAnimals,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.attackAnimals = !btn.getState();
                    tag.putBoolean("attackAnimals", tile.attackAnimals);
                    handleButtonClick(tag, listOffset);
                }));
        this.addButton(new GuiButtonCheckbox(leftPos+74, topPos+42, I18n.get("gui.turret.players"), tile.attackPlayers,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.attackPlayers = !btn.getState();
                    tag.putBoolean("attackPlayers", tile.attackPlayers);
                    handleButtonClick(tag, listOffset);
                }));
        this.addButton(new GuiButtonCheckbox(leftPos+74, topPos+58, I18n.get("gui.turret.neutrals"), tile.attackNeutrals,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.attackNeutrals = !btn.getState();
                    tag.putBoolean("attackNeutrals", tile.attackNeutrals);
                    handleButtonClick(tag, listOffset);
                }));
    }

    protected void handleButtonClick(CompoundNBT nbt, int listOffset)
    {
        if(!nbt.isEmpty())
        {
            ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, nbt));
            this.init();
            if(listOffset >= 0)
                ((GuiReactiveList)this.buttons.get(0)).setOffset(listOffset);
        }
    }

    // This is the resource location for the background image
    private static final ResourceLocation TEXTURE = new ResourceLocation(AdvancedForcefields.MOD_ID, "textures/gui/force_modifier.png");
}