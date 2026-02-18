package org.exampl.untitledaii.industrial.machine;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.exampl.untitledaii.Untitledaii;
import org.exampl.untitledaii.industrial.ModContainers;

/**
 * Screen for Crusher machine GUI.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class CrusherScreen extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<CrusherContainer> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Untitledaii.MODID, "textures/gui/crusher.png");
    private static final int ENERGY_BAR_HEIGHT = 48;
    private static final int ENERGY_BAR_WIDTH = 14;
    private static final int ENERGY_BAR_X = 157;
    private static final int ENERGY_BAR_Y = 68;
    private static final int ARROW_WIDTH = 22;
    private static final int ARROW_HEIGHT = 16;
    private static final int ARROW_X = 79;
    private static final int ARROW_Y = 34;
    private static final int PROGRESS_ARROW_U = 176;
    private static final int PROGRESS_ARROW_V = 14;

    public CrusherScreen(CrusherContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw GUI background
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        CrusherBlockEntity crusher = this.menu.blockEntity;

        // Draw progress arrow
        float progress = crusher.getProgress();
        int arrowWidth = (int)(progress * ARROW_WIDTH);
        if (arrowWidth > 0) {
            guiGraphics.blit(TEXTURE,
                x + ARROW_X, y + ARROW_Y,
                PROGRESS_ARROW_U, PROGRESS_ARROW_V,
                arrowWidth, ARROW_HEIGHT);
        }

        // Draw energy bar
        int energyStored = crusher.getEnergyStorage().getEnergyStored();
        int maxEnergy = crusher.getEnergyStorage().getMaxEnergyStored();
        int energyHeight = (int)((float)energyStored / maxEnergy * ENERGY_BAR_HEIGHT);

        if (energyHeight > 0) {
            guiGraphics.blit(TEXTURE,
                x + ENERGY_BAR_X, y + ENERGY_BAR_Y - energyHeight,
                176, ENERGY_BAR_Y - energyHeight,
                ENERGY_BAR_WIDTH, energyHeight);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, "Crusher", 88, 6, 0x404040);
        guiGraphics.drawString(this.font, "Inventory", 8, 72, 0x404040);

        super.renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, partialTick, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
