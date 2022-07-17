/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.slimeist.aforce.client.gui.ie_elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.slimeist.aforce.client.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.TextComponent;

import net.minecraft.client.gui.components.Button.OnPress;

public class GuiButtonItem extends Button
{
	public boolean state;
	ItemStack item;

	public GuiButtonItem(int x, int y, ItemStack stack, boolean state, OnPress handler)
	{
		super(x, y, 18, 18, TextComponent.EMPTY, handler);
		this.state = state;
		this.item = stack;
	}

	@Override
	public void render(PoseStack transform, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture(GuiReactiveList.TEXTURE);
			this.isHovered = mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
			RenderSystem.defaultBlendFunc();
			this.blit(transform, x, y, 24+(state?18: 0), 128, width, height);
			//TODO this.mouseDragged(mc, mouseX, mouseY);

			if(!item.isEmpty())
			{
				Minecraft mc = Minecraft.getInstance();
				mc.getItemRenderer().renderAndDecorateItem(item, x+1, y+1);

				if(!state)
				{
					RenderSystem.disableDepthTest();
					fill(transform, x+1, y+1, x+17, x+17, 0x77444444);
					RenderSystem.enableDepthTest();
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean b = super.mouseClicked(mouseX, mouseY, button);
		if(b)
			this.state = !state;
		return b;
	}
}
