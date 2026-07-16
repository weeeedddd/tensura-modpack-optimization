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
 * Gilden-Hauptmenue im "Eminence in Shadow"-Stil:
 * dunkler Schieferstein mit feinen Rissen, umrandet von zaehfluessigem,
 * tiefviolettem Schleim mit pulsierendem "Atem"-Glow (Alpha 0.6 – 1.0),
 * Texte in Dunkelgold &amp; Violett.
 *
 * Rein mit {@link GuiGraphics} gerendert (keine Textur-Assets noetig).
 */
public class GuildMainScreen extends Screen {

    // Farbpalette
    private static final int GOLD       = 0xFFD4AF37;
    private static final int GOLD_DIM   = 0xFFB8912E;
    private static final int VIOLET     = 0xFFB56BE0;
    private static final int SLATE      = 0xF00B0B10;
    private static final int SLATE_EDGE = 0xFF16161F;
    private static final int CRACK      = 0xFF1E1E28;
    private static final int SLIME_RGB  = 0x7A2FBF; // violetter Schleim (ohne Alpha)

    private static final int PANEL_W = 236;
    private static final int PANEL_H = 196;

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

        int btnW = 150, btnH = 20, gap = 6;
        int bx = left + (PANEL_W - btnW) / 2;
        int by = top + PANEL_H - 84;

        // "Einladen" -> schliesst dieses GUI und oeffnet den Einladungs-Screen
        addRenderableWidget(Button.builder(Component.literal("§5» Einladen «"), b ->
                Minecraft.getInstance().setScreen(new GuildInviteScreen(this)))
                .bounds(bx, by, btnW, btnH).build());

        // "Schwarzmarkt" -> Server oeffnet das Mitsugoshi-Handels-GUI
        addRenderableWidget(Button.builder(Component.literal("§6» Mitsugoshi Schwarzmarkt «"), b ->
                PacketDistributor.sendToServer(new ServerboundMarketActionPacket(ServerboundMarketActionPacket.OPEN)))
                .bounds(bx, by + btnH + gap, btnW, btnH).build());

        // "Schliessen"
        addRenderableWidget(Button.builder(Component.literal("§8Schliessen"), b -> this.onClose())
                .bounds(bx, by + (btnH + gap) * 2, btnW, btnH).build());
    }

    /** Pulsierende Schleim-Farbe: Alpha fadet sanft zwischen 0.6 und 1.0. */
    private int slimeColor() {
        double phase = (Util.getMillis() % 2600L) / 2600.0;
        float pulse = 0.6F + 0.4F * (float) (0.5 + 0.5 * Math.sin(phase * Math.PI * 2.0));
        int a = Math.max(0, Math.min(255, (int) (pulse * 255)));
        return (a << 24) | SLIME_RGB;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(g);   // Welt dahinter abdunkeln
        drawSlatePanel(g);
        drawSlimeBorder(g);
    }

    private void drawSlatePanel(GuiGraphics g) {
        int x2 = left + PANEL_W, y2 = top + PANEL_H;
        // Grundflaeche
        g.fill(left, top, x2, y2, SLATE);
        // dezenter Innenrand (Steinkante)
        g.fill(left + 4, top + 4, x2 - 4, top + 5, SLATE_EDGE);
        g.fill(left + 4, y2 - 5, x2 - 4, y2 - 4, SLATE_EDGE);
        g.fill(left + 4, top + 4, left + 5, y2 - 4, SLATE_EDGE);
        g.fill(x2 - 5, top + 4, x2 - 4, y2 - 4, SLATE_EDGE);
        drawCracks(g);
    }

    /** Feine, gotische Risse im Schiefer (feste Segmente, relativ zum Panel). */
    private void drawCracks(GuiGraphics g) {
        crack(g, 30, 24, 46, 60);
        crack(g, 46, 60, 40, 96);
        crack(g, 40, 96, 58, 130);
        crack(g, 200, 30, 188, 70);
        crack(g, 188, 70, 205, 110);
        crack(g, 205, 110, 190, 150);
        crack(g, 120, 150, 132, 178);
        crack(g, 90, 40, 105, 66);
    }

    /** Zeichnet ein "Riss"-Segment als treppenfoermige 1px-Linie. */
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

    /** Pulsierender Schleim-Rand + ein paar zaehe "Tropfen". */
    private void drawSlimeBorder(GuiGraphics g) {
        int c = slimeColor();
        int x2 = left + PANEL_W, y2 = top + PANEL_H;
        int t = 4; // Randdicke
        g.fill(left - t, top - t, x2 + t, top, c);       // oben
        g.fill(left - t, y2, x2 + t, y2 + t, c);         // unten
        g.fill(left - t, top, left, y2, c);              // links
        g.fill(x2, top, x2 + t, y2, c);                  // rechts
        // zaehe Tropfen an der Unterkante
        g.fill(left + 40, y2 + t, left + 46, y2 + t + 6, c);
        g.fill(left + 120, y2 + t, left + 126, y2 + t + 9, c);
        g.fill(left + 190, y2 + t, left + 196, y2 + t + 5, c);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick); // renderBackground + Widgets
        drawTexts(g);
    }

    private void drawTexts(GuiGraphics g) {
        int cx = left + PANEL_W / 2;
        int y = top + 16;
        g.drawCenteredString(this.font, Component.literal("S H A D O W  G A R D E N"), cx, y, GOLD);
        y += 14;
        g.drawCenteredString(this.font, Component.literal(data.guildName()), cx, y, VIOLET);
        y += 24;

        int lx = left + 24;
        line(g, lx, y, "Abenteurer-Rang", data.adventurerRank());            y += 16;
        line(g, lx, y, "Dein Rang", data.memberRank());                      y += 16;
        line(g, lx, y, "Mitglieds-Nr.", "#" + data.memberNumber());          y += 16;
        line(g, lx, y, "Mitglieder", data.memberCount() + " / " + data.memberLimit());
    }

    /** Label (Dunkelgold) : Wert (Violett). */
    private void line(GuiGraphics g, int x, int y, String label, String value) {
        g.drawString(this.font, label + ":", x, y, GOLD_DIM, false);
        g.drawString(this.font, value, x + 118, y, VIOLET, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
