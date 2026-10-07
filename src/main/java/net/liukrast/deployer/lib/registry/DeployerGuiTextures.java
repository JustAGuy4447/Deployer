package net.liukrast.deployer.lib.registry;

import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.liukrast.deployer.lib.Deployer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public enum DeployerGuiTextures implements ScreenElement, TextureSheetSegment {
    VALUE_SETTINGS_SMOOTH_BAR_L("value_edit_box", 0, 0, 2, 8),
    VALUE_SETTINGS_SMOOTH_BAR_R("value_edit_box", 3, 0, 2, 8),
    VALUE_SETTINGS_SMOOTH_BAR("value_edit_box", 6, 0, 250, 8),

    VALUE_EDIT_BOX("value_edit_box", 0, 9, 256, 14),

    BRASS_FRAME_CURSOR_LEFT("value_edit_box", 0, 24, 4, 4),
    BRASS_FRAME_CURSOR_RIGHT("value_edit_box", 5, 24, 4, 4),
    ;

    private final ResourceLocation location;
    private final int startX;
    private final int startY;
    private final int width;
    private final int height;

    DeployerGuiTextures(String path, int startX, int startY, int width, int height) {
        this.location = Deployer.CONSTANTS.id("textures/gui/" + path + ".png");
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public int getStartX() {
        return startX;
    }

    @Override
    public int getStartY() {
        return startY;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }
}
