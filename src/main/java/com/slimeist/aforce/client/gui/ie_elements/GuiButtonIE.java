/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.slimeist.aforce.client.gui.ie_elements;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiButtonIE extends Button
{
	protected final ResourceLocation texture;
	protected final int texU;
	protected final int texV;

	public GuiButtonIE(int x, int y, int w, int h, ITextComponent name, ResourceLocation texture, int u, int v, IIEPressable handler)
	{
		super(x, y, w, h, name, handler);
		this.texture = texture;
		this.texU = u;
		this.texV = v;
	}

	int[] hoverOffset;

	public GuiButtonIE setHoverOffset(int x, int y)
	{
		this.hoverOffset = new int[]{x, y};
		return this;
	}

	private boolean isPressable(double mouseX, double mouseY)
	{
		return this.active&&this.visible&&mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
	}

	@Override
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bind(texture);
			FontRenderer fontrenderer = mc.font;
			this.isHovered = isPressable(mouseX, mouseY);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
			RenderSystem.blendFunc(770, 771);
			if(hoverOffset!=null&&this.isHovered)
				this.blit(transform, x, y, texU+hoverOffset[0], texV+hoverOffset[1], width, height);
			else
				this.blit(transform, x, y, texU, texV, width, height);
			if(!getMessage().getString().isEmpty())
			{
				int txtCol = 0xE0E0E0;
				if(!this.active)
					txtCol = 0xA0A0A0;
				else if(this.isHovered)
					txtCol = 0xfff78034;
				this.drawCenteredString(transform, fontrenderer, getMessage(), this.x+this.width/2, this.y+(this.height-8)/2, txtCol);
			}
		}
	}

	@Override
	public void onPress()
	{
		this.onPress.onPress(this);
	}

	public interface IIEPressable<B extends GuiButtonIE> extends IPressable
	{
		void onIEPress(B var1);

		@Override
		default void onPress(Button var1)
		{
			Preconditions.checkArgument(var1 instanceof GuiButtonIE);
			this.onIEPress((B)var1);
		}
	}
}
