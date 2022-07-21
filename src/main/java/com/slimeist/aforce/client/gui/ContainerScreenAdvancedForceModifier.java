package com.slimeist.aforce.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.gui.elements.SelectorSuggestionHelper;
import com.slimeist.aforce.client.gui.ie_elements.GuiButtonCheckbox;
import com.slimeist.aforce.client.gui.ie_elements.GuiButtonIE;
import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.containers.force_modifier.ContainerAdvancedForceModifier;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import com.slimeist.aforce.common.network.MessageForceModifierSync;
import com.slimeist.aforce.common.tiles.AdvancedForceModifierTileEntity;
import com.slimeist.aforce.common.tiles.SimpleForceModifierTileEntity;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import static com.slimeist.aforce.client.util.ClientUtils.mc;

//Heavily inspired from IE Turret

public class ContainerScreenAdvancedForceModifier extends AbstractContainerScreen<ContainerAdvancedForceModifier> {

    public AdvancedForceModifierTileEntity tile;
    private EditBox selectorField;
    private ContainerAdvancedForceModifier containerAdvancedForceModifier;
    private SelectorSuggestionHelper selectorSuggestions;

    public ContainerScreenAdvancedForceModifier(ContainerAdvancedForceModifier containerAdvancedForceModifier, Inventory playerInventory, Component title) {
        super(containerAdvancedForceModifier, playerInventory, title);
        this.containerAdvancedForceModifier = containerAdvancedForceModifier;
        this.tile = this.containerAdvancedForceModifier.tile;

        // Set the width and height of the gui.  Should match the size of the texture!
        imageWidth = 233 + 1;
        imageHeight = 211 + 1;
    }

    //ADJUST TO IMAGE

    final static int FONT_Y_SPACING = 10;
    final static int PLAYER_INV_LABEL_XPOS = ContainerForceModifier.PLAYER_INVENTORY_XPOS;
    final static int PLAYER_INV_LABEL_YPOS = ContainerForceModifier.PLAYER_INVENTORY_YPOS - FONT_Y_SPACING;

    private static boolean validate(String selector) {
        return MiscUtil.predicateFromSelector(selector).isPresent();
    }

    @Override
    public void init() {
        super.init();
        mc().keyboardHandler.setSendRepeatsToGui(true);
        this.selectorField = new EditBox(this.font, leftPos + 11, topPos + 107, 190, 12, TextComponent.EMPTY);
        this.selectorField.setTextColor(-1);
        this.selectorField.setTextColorUneditable(-1);
        this.selectorField.setBordered(false);
        this.selectorField.setMaxLength(32500); //match command block
        this.selectorField.setValue(tile.entitySelector);
        this.selectorField.setFormatter((s, i) -> {
            String contents = this.selectorField.getValue();
            return FormattedCharSequence.forward(s, validate(contents) ? Style.EMPTY : Style.EMPTY.withColor(ChatFormatting.RED));
        });
        this.selectorField.setResponder(this::onEdited);

        this.renderables.clear();
        AdvancedForcefields.LOGGER.info("This: " + this + ", tile: " + tile);//.toString()+", targetList: "+tile.targetList.toString());
        int extraY = 7;
        int sideX = 8;
        this.addRenderableWidget(new GuiButtonIE(leftPos + 203, topPos + 103, 24, 16, new TranslatableComponent("gui.aforce.advanced_modifier.set"), TEXTURE, 194, 212,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    String selector = selectorField.getValue();
                    /*if (!validate(selector)) {
                        this.init();
                        return;
                    }*/
                    if (!tile.entitySelector.equals(selector)) {
                        tag.putString("set", selector);
                        tile.entitySelector = selector;
                    }
                    selectorField.setValue(selector);
                    handleButtonClick(tag);
                }));
        this.addRenderableWidget(new GuiButtonCheckbox(leftPos + sideX, topPos + 55 + extraY, new TranslatableComponent("gui.aforce.modifier.blacklist"), !tile.whitelist,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.whitelist = btn.getState();
                    tag.putBoolean(SimpleForceModifierTileEntity.TAG_WHITELIST, tile.whitelist);
                    handleButtonClick(tag);
                }));
        this.addRenderableWidget(new GuiButtonIE(leftPos + sideX, topPos + 70 + extraY, 7, 7, new TextComponent(""), ELEMENTS, 9, 87,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.priority += 1;
                    tag.putInt(SimpleForceModifierTileEntity.TAG_PRIORITY, tile.priority);
                    handleButtonClick(tag);
                }).setHoverOffset(9, 0));
        this.addRenderableWidget(new GuiButtonIE(leftPos + sideX, topPos + 86 + extraY, 7, 7, new TextComponent(""), ELEMENTS, 9, 96,
                btn -> {
                    CompoundTag tag = new CompoundTag();
                    int listOffset = -1;
                    tile.priority -= 1;
                    tag.putInt(SimpleForceModifierTileEntity.TAG_PRIORITY, tile.priority);
                    handleButtonClick(tag);
                }).setHoverOffset(9, 0));

        this.selectorSuggestions = new SelectorSuggestionHelper(this.minecraft, this, this.selectorField, this.font, 0, 7, true, Integer.MIN_VALUE);
        this.selectorSuggestions.setAllowSuggestions(true);
        this.selectorSuggestions.setYOffset(107+12+topPos-height);//this.selectorField.y);
        this.selectorSuggestions.updateCommandInfo();
    }

    private void onEdited(String e) {
        this.selectorSuggestions.updateCommandInfo();
    }

    protected void handleButtonClick(CompoundTag nbt) {
        if (!nbt.isEmpty()) {
            AdvancedForcefields.packetHandler.sendToServer(new MessageForceModifierSync(tile, nbt));
            this.init();
        }
    }

    @Override
    public void resize(Minecraft p_231152_1_, int p_231152_2_, int p_231152_3_) {
        String s = this.selectorField.getValue();
        boolean b = this.selectorField.isFocused();
        this.init(p_231152_1_, p_231152_2_, p_231152_3_);
        this.selectorField.setValue(s);
        this.selectorField.setFocus(b);
        this.selectorSuggestions.updateCommandInfo();
    }

    @Override
    public void render(PoseStack transform, int mx, int my, float partial) {
        this.renderBackground(transform);
        super.render(transform, mx, my, partial);
        this.renderTooltip(transform, mx, my);
        this.selectorField.render(transform, mx, my, partial);

        ArrayList<Component> tooltip = new ArrayList<>();
        //tooltip.add(new TranslationTextComponent("Version: "+AdvancedForcefields.VERSION));

        if (!tooltip.isEmpty())
            renderTooltip(transform, tooltip, Optional.empty(), mx, my);
        this.selectorSuggestions.render(transform, mx, my);
    }

    @Override
    protected void renderBg(PoseStack transform, float f, int mx, int my) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ClientUtils.bindTexture(TEXTURE);
        this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        //this.blit(transform, x, y, u, v, w, h);
        if (this.selectorField.isFocused()) {
            this.blit(transform, leftPos + 7, topPos + 103, 0, 212, 193, 15);
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
                9, 7 + 78, 0xE0E0E0); //x is sideX + 1
    }

    @Override
    public void onClose() {
        super.onClose();
        mc().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int p_keyPressed_3_) {
        if (this.selectorSuggestions.keyPressed(key, scancode, p_keyPressed_3_)) {
            return true;
        } else if (this.selectorField.isFocused()) {
            if (key == GLFW.GLFW_KEY_ENTER) {
                String selector = this.selectorField.getValue();
                /*if (!validate(selector)) {
                    this.init();
                    return true;
                }*/
                if (!tile.entitySelector.equals(selector)) {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("set", selector);
                    tile.entitySelector = selector;
                    AdvancedForcefields.packetHandler.sendToServer(new MessageForceModifierSync(tile, tag));

                    this.init();
                }
            } else
                this.selectorField.keyPressed(key, scancode, p_keyPressed_3_);
            return true;
        } else
            return super.keyPressed(key, scancode, p_keyPressed_3_);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        return this.selectorField.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.selectorSuggestions.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return this.selectorField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) {
        return this.selectorSuggestions.mouseScrolled(p_231043_5_) || super.mouseScrolled(p_231043_1_, p_231043_3_, p_231043_5_);
    }

    // This is the resource location for the background image
    private static final ResourceLocation TEXTURE = new ResourceLocation(AdvancedForcefields.MOD_ID, "textures/gui/advanced_force_modifier.png");
    private static final ResourceLocation ELEMENTS = new ResourceLocation(AdvancedForcefields.MOD_ID, "textures/gui/hud_elements.png");
}