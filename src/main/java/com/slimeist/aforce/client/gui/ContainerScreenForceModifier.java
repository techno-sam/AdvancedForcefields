package com.slimeist.aforce.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.gui.ie_elements.GuiButtonCheckbox;
import com.slimeist.aforce.client.gui.ie_elements.GuiButtonIE;
import com.slimeist.aforce.client.gui.ie_elements.GuiReactiveList;
import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;

import com.slimeist.aforce.common.network.MessageForceModifierSync;
import com.slimeist.aforce.common.tiles.SimpleForceModifierTileEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

import static com.slimeist.aforce.client.util.ClientUtils.mc;

//Heavily inspired from IE Turret

public class ContainerScreenForceModifier extends ContainerScreen<ContainerForceModifier> {

    public SimpleForceModifierTileEntity tile;
    private TextFieldWidget nameField;
    private ContainerForceModifier containerForceModifier;
    public ContainerScreenForceModifier(ContainerForceModifier containerForceModifier, PlayerInventory playerInventory, ITextComponent title) {
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
        this.nameField = new TextFieldWidget(this.font, leftPos+11, topPos+107, 100, 12, StringTextComponent.EMPTY);
        this.nameField.setTextColor(-1);
        this.nameField.setTextColorUneditable(-1);
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(30);

        this.buttons.clear();
        AdvancedForcefields.LOGGER.info("This: "+this+", tile: "+tile);//.toString()+", targetList: "+tile.targetList.toString());
        int extraY = 7;
        int sideX = 50;
        this.addButton(new GuiReactiveList(this, leftPos+9, topPos+10+extraY, 60+45, 72,
                btn -> {
                    GuiReactiveList list = (GuiReactiveList)btn;
                    CompoundNBT tag = new CompoundNBT();
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
        this.addButton(new GuiButtonIE(leftPos+74+45, topPos+84+extraY+12, 24, 16, new TranslationTextComponent("gui.aforce.modifier.add"), TEXTURE, 110, 212,
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
        this.addButton(new GuiButtonCheckbox(leftPos+74+sideX, topPos+10+extraY, new TranslationTextComponent("gui.aforce.modifier.blacklist"), !tile.whitelist,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.whitelist = btn.getState();
                    tag.putBoolean(SimpleForceModifierTileEntity.TAG_WHITELIST, tile.whitelist);
                    handleButtonClick(tag, listOffset);
                }));
        this.addButton(new GuiButtonCheckbox(leftPos+74+sideX, topPos+26+extraY, new TranslationTextComponent("gui.aforce.modifier.animals"), tile.targetAnimals,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.targetAnimals = !btn.getState();
                    tag.putBoolean(SimpleForceModifierTileEntity.TAG_TARGET_ANIMALS, tile.targetAnimals);
                    handleButtonClick(tag, listOffset);
                }));
        this.addButton(new GuiButtonCheckbox(leftPos+74+sideX, topPos+42+extraY, new TranslationTextComponent("gui.aforce.modifier.players"), tile.targetPlayers,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.targetPlayers = !btn.getState();
                    tag.putBoolean(SimpleForceModifierTileEntity.TAG_TARGET_PLAYERS, tile.targetPlayers);
                    handleButtonClick(tag, listOffset);
                }));
        this.addButton(new GuiButtonCheckbox(leftPos+74+sideX, topPos+58+extraY, new TranslationTextComponent("gui.aforce.modifier.neutrals"), tile.targetNeutrals,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.targetNeutrals = !btn.getState();
                    tag.putBoolean(SimpleForceModifierTileEntity.TAG_TARGET_NEUTRALS, tile.targetNeutrals);
                    handleButtonClick(tag, listOffset);
                }));
        this.addButton(new GuiButtonIE(leftPos+74+sideX, topPos+70+extraY, 7,7, new StringTextComponent(""), ELEMENTS, 9, 87,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.priority += 1;
                    tag.putInt(SimpleForceModifierTileEntity.TAG_PRIORITY, tile.priority);
                    handleButtonClick(tag, listOffset);
                }).setHoverOffset(9,0));
        this.addButton(new GuiButtonIE(leftPos+74+sideX, topPos+86+extraY, 7,7, new StringTextComponent(""), ELEMENTS, 9, 96,
                btn -> {
                    CompoundNBT tag = new CompoundNBT();
                    int listOffset = -1;
                    tile.priority -= 1;
                    tag.putInt(SimpleForceModifierTileEntity.TAG_PRIORITY, tile.priority);
                    handleButtonClick(tag, listOffset);
                }).setHoverOffset(9,0));
    }

    protected void handleButtonClick(CompoundNBT nbt, int listOffset)
    {
        if(!nbt.isEmpty())
        {
            AdvancedForcefields.packetHandler.sendToServer(new MessageForceModifierSync(tile, nbt));
            this.init();
            if(listOffset >= 0)
                ((GuiReactiveList)this.buttons.get(0)).setOffset(listOffset);
        }
    }

    @Override
    public void render(MatrixStack transform, int mx, int my, float partial)
    {
        this.renderBackground(transform);
        super.render(transform, mx, my, partial);
        this.renderTooltip(transform, mx, my);
        this.nameField.render(transform, mx, my, partial);

        ArrayList<ITextComponent> tooltip = new ArrayList<>();
        //tooltip.add(new TranslationTextComponent("Version: "+AdvancedForcefields.VERSION));

        if(!tooltip.isEmpty())
            GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
    }

    @Override
    protected void renderBg(MatrixStack transform, float f, int mx, int my)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ClientUtils.bindTexture(TEXTURE);
        this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        //this.blit(transform, x, y, u, v, w, h);
        if (this.nameField.isFocused()) {
            this.blit(transform, leftPos+7, topPos+103, 0, 212, 109, 15);
        }
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

        // draw the label for the priority
        this.font.drawShadow(matrixStack, new TranslationTextComponent("gui.aforce.modifier.priority", tile.priority),
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
                    CompoundNBT tag = new CompoundNBT();
                    tag.putString("add", name);
                    tile.targetList.add(name);
                    AdvancedForcefields.packetHandler.sendToServer(new MessageForceModifierSync(tile, tag));

                    this.init();
                    ((GuiReactiveList)this.buttons.get(0)).setOffset(((GuiReactiveList)this.buttons.get(0)).getMaxOffset());
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