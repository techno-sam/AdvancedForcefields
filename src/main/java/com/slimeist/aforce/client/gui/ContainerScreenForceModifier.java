package com.slimeist.aforce.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.gui.ie_elements.GuiButtonCheckbox;
import com.slimeist.aforce.client.gui.ie_elements.GuiButtonIE;
import com.slimeist.aforce.client.gui.ie_elements.GuiReactiveList;
import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import com.slimeist.aforce.common.network.MessageForceModifierSync;
import com.slimeist.aforce.common.tiles.ForceModifierTileEntity;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import static com.slimeist.aforce.client.util.ClientUtils.mc;

//Heavily inspired from IE Turret

public class ContainerScreenForceModifier extends AbstractContainerScreen<ContainerForceModifier> {

    public ForceModifierTileEntity tile;
    private EditBox nameField;
    private ContainerForceModifier containerForceModifier;
    public ContainerScreenForceModifier(ContainerForceModifier containerForceModifier, Inventory playerInventory, Component title) {
        super(containerForceModifier, playerInventory, title);
        this.containerForceModifier = containerForceModifier;
        this.tile = this.containerForceModifier.tile;

        // Set the width and height of the gui.  Should match the size of the texture!
        imageWidth = 233+1;
        imageHeight = 211+1;
    }

    //ADJUST TO IMAGE

    final static  int FONT_Y_SPACING = 10;
    final static  int PLAYER_INV_LABEL_XPOS = ContainerForceModifier.PLAYER_INVENTORY_XPOS;
    final static  int PLAYER_INV_LABEL_YPOS = ContainerForceModifier.PLAYER_INVENTORY_YPOS - FONT_Y_SPACING;

    @Override
    public void init() {
        super.init();
        mc().keyboardHandler.setSendRepeatsToGui(true);
        this.nameField = new EditBox(this.font, leftPos+11, topPos+107, 100, 12, TextComponent.EMPTY);
        this.nameField.setTextColor(-1);
        this.nameField.setTextColorUneditable(-1);
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(30);

        this.renderables.clear();
        AdvancedForcefields.LOGGER.info("This: "+this+", tile: "+tile);//.toString()+", targetList: "+tile.targetList.toString());
        int extraY = 7;
        int sideX = 50;
        this.addRenderableWidget(new GuiReactiveList(this, leftPos+9, topPos+10+extraY, 60+45, 72,
                btn -> {
                    GuiReactiveList list = (GuiReactiveList)btn;
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    int rem = list.selectedOption;
                    if(rem >= 0&&tile.targetList.size() > 0 && rem<tile.targetList.size())
                    {
                        tile.targetList.remove(rem);
                        tag.putInt("remove", rem);
                        listOffset = list.getOffset()-1;
                        handleButtonClick(tag, listOffset);
                    }
                }, tile.targetList.toArray(new String[0]))
                .setPadding(0, 0, 2, 2));
        this.addRenderableWidget(new GuiButtonIE(leftPos+74+45, topPos+84+extraY+12, 24, 16, new TranslatableComponent("gui.aforce.modifier.add"), TEXTURE, 110, 212,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    String name = nameField.getValue();
                    if(!tile.targetList.contains(name))
                    {
                        listOffset = ((GuiReactiveList)renderables.get(0)).getMaxOffset();
                        tag.putString("add", name);
                        tile.targetList.add(name);
                    }
                    nameField.setValue("");
                    handleButtonClick(tag, listOffset);
                }));
        this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74+sideX, topPos+10+extraY, new TranslatableComponent("gui.aforce.modifier.blacklist"), !tile.whitelist,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.whitelist = btn.getState();
                    tag.putBoolean(ForceModifierTileEntity.TAG_WHITELIST, tile.whitelist);
                    handleButtonClick(tag, listOffset);
                }));
        this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74+sideX, topPos+26+extraY, new TranslatableComponent("gui.aforce.modifier.animals"), tile.targetAnimals,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.targetAnimals = !btn.getState();
                    tag.putBoolean(ForceModifierTileEntity.TAG_TARGET_ANIMALS, tile.targetAnimals);
                    handleButtonClick(tag, listOffset);
                }));
        this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74+sideX, topPos+42+extraY, new TranslatableComponent("gui.aforce.modifier.players"), tile.targetPlayers,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.targetPlayers = !btn.getState();
                    tag.putBoolean(ForceModifierTileEntity.TAG_TARGET_PLAYERS, tile.targetPlayers);
                    handleButtonClick(tag, listOffset);
                }));
        this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74+sideX, topPos+58+extraY, new TranslatableComponent("gui.aforce.modifier.neutrals"), tile.targetNeutrals,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.targetNeutrals = !btn.getState();
                    tag.putBoolean(ForceModifierTileEntity.TAG_TARGET_NEUTRALS, tile.targetNeutrals);
                    handleButtonClick(tag, listOffset);
                }));
        this.addRenderableWidget(new GuiButtonIE(leftPos+74+sideX, topPos+70+extraY, 7,7, new TextComponent(""), ELEMENTS, 9, 87,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.priority += 1;
                    tag.putInt(ForceModifierTileEntity.TAG_PRIORITY, tile.priority);
                    handleButtonClick(tag, listOffset);
                }).setHoverOffset(9,0));
        this.addRenderableWidget(new GuiButtonIE(leftPos+74+sideX, topPos+86+extraY, 7,7, new TextComponent(""), ELEMENTS, 9, 96,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.priority -= 1;
                    tag.putInt(ForceModifierTileEntity.TAG_PRIORITY, tile.priority);
                    handleButtonClick(tag, listOffset);
                }).setHoverOffset(9,0));
    }

    protected void handleButtonClick(CompoundTag nbt, int listOffset)
    {
        if(!nbt.isEmpty())
        {
            AdvancedForcefields.packetHandler.sendToServer(new MessageForceModifierSync(tile, nbt));
            this.init();
            if(listOffset >= 0)
                ((GuiReactiveList)this.renderables.get(0)).setOffset(listOffset);
        }
    }

    @Override
    public void render(PoseStack transform, int mx, int my, float partial)
    {
        this.renderBackground(transform);
        super.render(transform, mx, my, partial);
        this.renderTooltip(transform, mx, my);
        this.nameField.render(transform, mx, my, partial);

        ArrayList<Component> tooltip = new ArrayList<>();
        //tooltip.add(new TranslationTextComponent("Version: "+AdvancedForcefields.VERSION));

        if(!tooltip.isEmpty())
            renderTooltip(transform, tooltip, Optional.empty(), mx, my);
    }

    @Override
    protected void renderBg(PoseStack transform, float f, int mx, int my)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ClientUtils.bindTexture(TEXTURE);
        this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        //this.blit(transform, x, y, u, v, w, h);
        if (this.nameField.isFocused()) {
            this.blit(transform, leftPos+7, topPos+103, 0, 212, 109, 15);
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        // draw the label for the top of the screen
        final int LABEL_XPOS = 5;
        final int LABEL_YPOS = 5;
        this.font.draw(matrixStack, this.title, LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());     ///    this.font.drawString

        // draw the label for the player inventory slots
        this.font.draw(matrixStack, this.playerInventoryTitle,                  ///    this.font.drawString
                PLAYER_INV_LABEL_XPOS, PLAYER_INV_LABEL_YPOS, Color.darkGray.getRGB());

        // draw the label for the priority
        this.font.drawShadow(matrixStack, new TranslatableComponent("gui.aforce.modifier.priority", tile.priority),
                75+50, 7+78, 0xE0E0E0);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        mc().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int p_keyPressed_3_)
    {
        if(this.nameField.isFocused())
        {
            if(key== GLFW.GLFW_KEY_ENTER)
            {
                String name = this.nameField.getValue();
                if(!tile.targetList.contains(name))
                {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("add", name);
                    tile.targetList.add(name);
                    AdvancedForcefields.packetHandler.sendToServer(new MessageForceModifierSync(tile, tag));

                    this.init();
                    ((GuiReactiveList)this.renderables.get(0)).setOffset(((GuiReactiveList)this.renderables.get(0)).getMaxOffset());
                }
            }
            else
                this.nameField.keyPressed(key, scancode, p_keyPressed_3_);
            return true;
        }
        else
            return super.keyPressed(key, scancode, p_keyPressed_3_);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        return this.nameField.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    // This is the resource location for the background image
    private static final ResourceLocation TEXTURE = new ResourceLocation(AdvancedForcefields.MOD_ID, "textures/gui/force_modifier.png");
    private static final ResourceLocation ELEMENTS = new ResourceLocation(AdvancedForcefields.MOD_ID, "textures/gui/hud_elements.png");
}