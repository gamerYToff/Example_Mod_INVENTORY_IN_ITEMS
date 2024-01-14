package com.containermod.container.graphic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import static com.containermod.ContainerModMain.MODID;

public class GuiContainerMod extends GuiContainer {

    private static final ResourceLocation resource = new ResourceLocation(MODID, "generic.png");
    private final Minecraft minecraft;
    private final Container container;

    public GuiContainerMod(Container container) {
        super(container);
        this.minecraft = Minecraft.getMinecraft();
        this.container = container;
        this.xSize = 176;
        this.ySize = 114 + 6 * 18;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        minecraft.getTextureManager().bindTexture(resource);
//        this.drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(resource);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, 6 * 18 + 17);
        this.drawTexturedModalRect(i, j + 6 * 18 + 17, 0, 126, this.xSize, 96);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
//        fontRenderer.drawString(container.inventory.getDisplayName().getUnformattedText(), 173, 8, 4210752);
//        String page = String.valueOf(container.inventory.getField(0) + 1);
//        int strSize = fontRenderer.getStringWidth(page);
//        fontRenderer.drawString(page, 203 - strSize / 2, 27, 4210752);
    }
}
