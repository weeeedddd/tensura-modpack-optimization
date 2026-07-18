package net.tensura.abyss.client.screen;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tensura.abyss.network.ClientboundOpenGuildScreenPayload;
import net.tensura.abyss.network.ServerboundMarketActionPacket;

/**
 * Shadow Garden guild interface — obsidian &amp; deep-violet redesign.
 *
 * Rendered purely with {@link GuiGraphics} (no texture assets):
 *   • layered obsidian body (vertical gradient bands) with subtle cracks
 *   • 1px violet outer frame, gold hairline inlay, gold corner studs
 *   • pulsing molten-slime border glow with viscous drips
 *   • stat rows as inset obsidian cards (label left, value right)
 */
public class GuildMainScreen extends Screen {

    // ── Palette ──
    private static final int GOLD       = 0xFFD4AF37;
    private static final int GOLD_DIM   = 0xFF8A6E24;
    private static final int VIOLET     = 0xFFB56BE0;
    private static final int VIOLET_DIM = 0xFF6B3F94;
    private static final int LABEL      = 0xFF8C86A0;
    private static final int VALUE      = 0xFFEFEAFF;
    private static final int CRACK      = 0xFF14101E;
    private static final int CARD_BG    = 0xC8120C1E;
    private static final int CARD_EDGE  = 0xFF241634;
    private static final int SLIME_RGB  = 0x7A2FBF;

    // Obsidian gradient bands (top -> bottom)
    private static final int[] OBSIDIAN = { 0xF80A0612, 0xF80C0716, 0xF80F091C, 0xF8110A20, 0xF80D081A };

    private static final int PANEL_W = 252;
    private static final int PANEL_H = 208;

    private final ClientboundOpenGuildScreenPayload data;
    private int left, top;

    public GuildMainScreen(ClientboundOpenGuildScreenPayload data) {
        super(Component.literal("Shadow Garden"));
        this.data = data;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        int btnW = 168, btnH = 20, gap = 5;
        int bx = left + (PANEL_W - btnW) / 2;
        int by = top + PANEL_H - 3 * btnH - 2 * gap - 12;

        addRenderableWidget(Button.builder(Component.literal("§5◆ §dInvite to the Shadows §5◆"), b ->
                Minecraft.getInstance().setScreen(new GuildInviteScreen(this)))
                .bounds(bx, by, btnW, btnH).build());

        addRenderableWidget(Button.builder(Component.literal("§6◆ §eMitsugoshi Black Market §6◆"), b ->
                PacketDistributor.sendToServer(new ServerboundMarketActionPacket(ServerboundMarketActionPacket.OPEN)))
                .bounds(bx, by + btnH + gap, btnW, btnH).build());

        addRenderableWidget(Button.builder(Component.literal("§8Withdraw into darkness"), b -> this.onClose())
                .bounds(bx, by + (btnH + gap) * 2, btnW, btnH).build());
    }

    /** Pulsing slime color: alpha breathes between 0.55 and 1.0. */
    private int slimeColor() {
        double phase = (Util.getMillis() % 2600L) / 2600.0;
        float pulse = 0.55F + 0.45F * (float) (0.5 + 0.5 * Math.sin(phase * Math.PI * 2.0));
        int a = Math.max(0, Math.min(255, (int) (pulse * 255)));
        return (a << 24) | SLIME_RGB;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(g);
        drawObsidianBody(g);
        drawFrame(g);
        drawSlimeBorder(g);
    }

    /** Layered obsidian: vertical gradient bands + hairline cracks. */
    private void drawObsidianBody(GuiGraphics g) {
        int bandH = PANEL_H / OBSIDIAN.length;
        for (int i = 0; i < OBSIDIAN.length; i++) {
            int y1 = top + i * bandH;
            int y2 = (i == OBSIDIAN.length - 1) ? top + PANEL_H : y1 + bandH;
            g.fill(left, y1, left + PANEL_W, y2, OBSIDIAN[i]);
        }
        // subtle cracks, kept away from the text column
        crack(g, 26, 30, 40, 66);
        crack(g, 40, 66, 33, 104);
        crack(g, 33, 104, 50, 140);
        crack(g, 224, 34, 210, 72);
        crack(g, 210, 72, 226, 112);
        crack(g, 226, 112, 212, 152);
        crack(g, 118, 168, 130, 194);
    }

    /** 1px violet outer frame + gold hairline inlay + gold corner studs. */
    private void drawFrame(GuiGraphics g) {
        int x2 = left + PANEL_W, y2 = top + PANEL_H;
        // violet frame
        g.fill(left - 1, top - 1, x2 + 1, top, VIOLET_DIM);
        g.fill(left - 1, y2, x2 + 1, y2 + 1, VIOLET_DIM);
        g.fill(left - 1, top, left, y2, VIOLET_DIM);
        g.fill(x2, top, x2 + 1, y2, VIOLET_DIM);
        // gold hairline inlay
        g.fill(left + 5, top + 5, x2 - 5, top + 6, GOLD_DIM);
        g.fill(left + 5, y2 - 6, x2 - 5, y2 - 5, GOLD_DIM);
        g.fill(left + 5, top + 5, left + 6, y2 - 5, GOLD_DIM);
        g.fill(x2 - 6, top + 5, x2 - 5, y2 - 5, GOLD_DIM);
        // corner studs
        stud(g, left + 3, top + 3);
        stud(g, x2 - 5, top + 3);
        stud(g, left + 3, y2 - 5);
        stud(g, x2 - 5, y2 - 5);
    }

    private void stud(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 2, y + 2, GOLD);
    }

    private void crack(GuiGraphics g, int x1, int y1, int x2, int y2) {
        int ax = left + x1, ay = top + y1, bx = left + x2, by = top + y2;
        int steps = Math.max(Math.abs(bx - ax), Math.abs(by - ay));
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            int px = ax + (bx - ax) * i / steps;
            int py = ay + (by - ay) * i / steps;
            g.fill(px, py, px + 1, py + 1, CRACK);
        }
    }

    /** Pulsing molten-slime border + viscous drips along the bottom edge. */
    private void drawSlimeBorder(GuiGraphics g) {
        int c = slimeColor();
        int x2 = left + PANEL_W, y2 = top + PANEL_H;
        int t = 3;
        g.fill(left - 1 - t, top - 1 - t, x2 + 1 + t, top - 1, c);
        g.fill(left - 1 - t, y2 + 1, x2 + 1 + t, y2 + 1 + t, c);
        g.fill(left - 1 - t, top - 1, left - 1, y2 + 1, c);
        g.fill(x2 + 1, top - 1, x2 + 1 + t, y2 + 1, c);
        g.fill(left + 42, y2 + 1 + t, left + 47, y2 + 1 + t + 6, c);
        g.fill(left + 126, y2 + 1 + t, left + 131, y2 + 1 + t + 9, c);
        g.fill(left + 198, y2 + 1 + t, left + 203, y2 + 1 + t + 5, c);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        drawTexts(g);
    }

    private void drawTexts(GuiGraphics g) {
        int cx = left + PANEL_W / 2;
        int y = top + 16;
        g.drawCenteredString(this.font, Component.literal("S H A D O W  G A R D E N"), cx, y, GOLD);
        y += 13;
        g.drawCenteredString(this.font, Component.literal("§5◆ §d" + data.guildName() + " §5◆"), cx, y, VIOLET);
        y += 14;
        // divider
        g.fill(left + 28, y, left + PANEL_W - 28, y + 1, CARD_EDGE);
        g.fill(cx - 2, y - 1, cx + 2, y + 2, VIOLET_DIM);
        y += 8;

        card(g, y,      "Adventurer Rank", data.adventurerRank(), GOLD);
        card(g, y + 19, "Your Standing",   data.memberRank(), VALUE);
        card(g, y + 38, "Member Number",   "#" + data.memberNumber(), VALUE);
        card(g, y + 57, "Sworn Shadows",   data.memberCount() + " / " + data.memberLimit(), VIOLET);
    }

    /** Inset obsidian stat card: label left (muted), value right (bright). */
    private void card(GuiGraphics g, int y, String label, String value, int valueColor) {
        int x1 = left + 22, x2 = left + PANEL_W - 22, y2 = y + 16;
        g.fill(x1, y, x2, y2, CARD_BG);
        g.fill(x1, y, x2, y + 1, CARD_EDGE);
        g.fill(x1, y2 - 1, x2, y2, CARD_EDGE);
        g.fill(x1, y, x1 + 1, y2, VIOLET_DIM);        // accent spine
        g.fill(x2 - 1, y, x2, y2, CARD_EDGE);
        g.drawString(this.font, label, x1 + 7, y + 4, LABEL, false);
        g.drawString(this.font, value, x2 - 7 - this.font.width(value), y + 4, valueColor, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
