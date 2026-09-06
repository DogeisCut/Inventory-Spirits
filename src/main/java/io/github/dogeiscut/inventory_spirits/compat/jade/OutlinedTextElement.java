package io.github.dogeiscut.inventory_spirits.compat.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;

public class OutlinedTextElement extends Element {
    private static final int OUTLINE_COLOR = 0;

    private final Component text;
    private final int color;

    public OutlinedTextElement(Component text, int color) {
        this.text = text;
        this.color = color;
    }

    @Override
    public Vec2 getSize() {
        Font font = Minecraft.getInstance().font;
        return new Vec2(font.width(this.text) + 2, font.lineHeight + 2);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        Font font = Minecraft.getInstance().font;
        int ix = Math.round(x) + 1;
        int iy = Math.round(y) + 1;

        guiGraphics.drawString(font, this.text, ix + 1, iy, OUTLINE_COLOR, false);
        guiGraphics.drawString(font, this.text, ix - 1, iy, OUTLINE_COLOR, false);
        guiGraphics.drawString(font, this.text, ix, iy + 1, OUTLINE_COLOR, false);
        guiGraphics.drawString(font, this.text, ix, iy - 1, OUTLINE_COLOR, false);
        guiGraphics.drawString(font, this.text, ix, iy, this.color, false);
    }
}