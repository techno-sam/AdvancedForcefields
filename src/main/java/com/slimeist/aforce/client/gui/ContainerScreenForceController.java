package com.slimeist.aforce.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.containers.force_controller.ContainerForceController;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * ContainerScreenFurnace is a gui similar to that of a furnace. It has a progress bar and a burn time indicator.
 * Both indicators have mouse over text
 *
 * The Screen is drawn in several layers, most importantly:
 * Background - renderBackground() - eg a grey fill
 * Background texture - drawGuiContainerBackgroundLayer() (eg the frames for the slots)
 * Foreground layer - typically text labels
 * renderHoveredToolTip - for tool tips when the mouse is hovering over something of interest
 */
public class ContainerScreenForceController extends AbstractContainerScreen<ContainerForceController> {

    private ContainerForceController containerForceController;
    public ContainerScreenForceController(ContainerForceController containerForceController, Inventory playerInventory, Component title) {
        super(containerForceController, playerInventory, title);
        this.containerForceController = containerForceController;

        // Set the width and height of the gui.  Should match the size of the texture!
        imageWidth = 175;
        imageHeight = 165;
    }

    //ADJUST TO IMAGE
    final static  int FLAME_XPOS = 31;
    final static  int FLAME_YPOS = 37;
    final static  int FLAME_ICON_U = 176;   // texture position of flame icon [u,v]
    final static  int FLAME_ICON_V = 0;
    final static  int FLAME_WIDTH = 14;
    final static  int FLAME_HEIGHT = 14;
    final static  int FLAME_X_SPACING = 18;

    final static  int FONT_Y_SPACING = 10;
    final static  int PLAYER_INV_LABEL_XPOS = ContainerForceController.PLAYER_INVENTORY_XPOS;
    final static  int PLAYER_INV_LABEL_YPOS = ContainerForceController.PLAYER_INVENTORY_YPOS - FONT_Y_SPACING;

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    // Draw the Tool tip text if hovering over something of interest on the screen
    // renderHoveredToolTip
    @Override
    protected void renderTooltip(PoseStack matrixStack, int mouseX, int mouseY) {
        if (!this.minecraft.player.inventoryMenu.getCarried().isEmpty())
            return;  // no tooltip if the player is dragging something

        List<Component> hoveringText = new ArrayList<Component>();

        // If the mouse is over one of the burn time indicators, add the burn time indicator hovering text
        if (isInRect(leftPos + FLAME_XPOS, topPos + FLAME_YPOS, FLAME_WIDTH, FLAME_HEIGHT, mouseX, mouseY)) {
            hoveringText.add(new TextComponent("Fuel Time:"));
            hoveringText.add(new TextComponent(containerForceController.secondsOfFuelRemaining() + "s"));
        }

        // If hoveringText is not empty draw the hovering text.  Otherwise, use vanilla to render tooltip for the slots
        if (!hoveringText.isEmpty()) {
            renderComponentTooltip(matrixStack, hoveringText, mouseX, mouseY);  //renderToolTip
        } else {
            super.renderTooltip(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ClientUtils.bindTexture(TEXTURE);

        // width and height are the size provided to the window when initialised after creation.
        // xSize, ySize are the expected size of the texture-? usually seems to be left as a default.
        // The code below is typical for vanilla containers, so I've just copied that- it appears to centre the texture within
        //  the available window
        // draw the background for this window
        int edgeSpacingX = (this.width - this.imageWidth) / 2;
        int edgeSpacingY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, edgeSpacingX, edgeSpacingY, 0, 0, this.imageWidth, this.imageHeight);

        // draw the fuel remaining bar for each fuel slot flame
        double burnRemaining = containerForceController.fractionOfFuelRemaining();
        int yOffset = (int) ((1.0 - burnRemaining) * FLAME_HEIGHT);
        this.blit(matrixStack, leftPos + FLAME_XPOS, topPos + FLAME_YPOS + yOffset,
                FLAME_ICON_U, FLAME_ICON_V + yOffset, FLAME_WIDTH, FLAME_HEIGHT - yOffset);
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
    }

    // Returns true if the given x,y coordinates are within the given rectangle
    public static boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY){
        return ((mouseX >= x && mouseX <= x+xSize) && (mouseY >= y && mouseY <= y+ySize));
    }

    // This is the resource location for the background image
    private static final ResourceLocation TEXTURE = new ResourceLocation(AdvancedForcefields.MOD_ID, "textures/gui/force_controller.png");
}