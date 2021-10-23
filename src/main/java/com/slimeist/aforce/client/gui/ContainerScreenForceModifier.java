package com.slimeist.aforce.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ContainerScreenForceModifier extends ContainerScreen<ContainerForceModifier> {

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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    // Draw the Tool tip text if hovering over something of interest on the screen
    // renderHoveredToolTip
    @Override
    protected void renderTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (!this.minecraft.player.inventory.getCarried().isEmpty())
            return;  // no tooltip if the player is dragging something

        List<ITextComponent> hoveringText = new ArrayList<ITextComponent>();
        /*
        // If the mouse is over one of the burn time indicators, add the burn time indicator hovering text
        if (isInRect(leftPos + FLAME_XPOS, topPos + FLAME_YPOS, FLAME_WIDTH, FLAME_HEIGHT, mouseX, mouseY)) {
            hoveringText.add(new StringTextComponent("Fuel Time:"));
            hoveringText.add(new StringTextComponent(containerForceModifier.secondsOfFuelRemaining() + "s"));
        }*/

        // If hoveringText is not empty draw the hovering text.  Otherwise, use vanilla to render tooltip for the slots
        if (!hoveringText.isEmpty()) {
            renderComponentTooltip(matrixStack, hoveringText, mouseX, mouseY);  //renderToolTip
        } else {
            super.renderTooltip(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(TEXTURE);

        // width and height are the size provided to the window when initialised after creation.
        // xSize, ySize are the expected size of the texture-? usually seems to be left as a default.
        // The code below is typical for vanilla containers, so I've just copied that- it appears to centre the texture within
        //  the available window
        // draw the background for this window
        int edgeSpacingX = (this.width - this.imageWidth) / 2;
        int edgeSpacingY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, edgeSpacingX, edgeSpacingY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        // draw the label for the top of the screen
        final int LABEL_XPOS = 5;
        final int LABEL_YPOS = 5;
        this.font.draw(matrixStack, this.title, LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());     ///    this.font.drawString

        // draw the label for the player inventory slots
        this.font.draw(matrixStack, this.inventory.getDisplayName(),                  ///    this.font.drawString
                PLAYER_INV_LABEL_XPOS, PLAYER_INV_LABEL_YPOS, Color.darkGray.getRGB());
    }

    // Returns true if the given x,y coordinates are within the given rectangle
    public static boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY){
        return ((mouseX >= x && mouseX <= x+xSize) && (mouseY >= y && mouseY <= y+ySize));
    }

    // This is the resource location for the background image
    private static final ResourceLocation TEXTURE = new ResourceLocation(AdvancedForcefields.MOD_ID, "textures/gui/force_modifier.png");
}