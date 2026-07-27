package net.tensura.abyss.client.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tensura.abyss.market.MarketManager;
import net.tensura.abyss.network.ServerboundMarketActionPacket;

/**
 * Mitsugoshi-Schwarzmarkt im Gothic/Schleim-Design (dunkler Schiefer + pulsierender
 * violetter Schleim-Rand). Zeigt den Muenz-Stand und die Kauf-Angebote; wandelt
 * Handels-Erzeugnisse in Muenzen um. Server-validiert ueber Packets.
 */
public class BlackMarketScreen extends Screen {

    private static final int SILVER     = AbyssGuiTheme.SILVER;
    private static final int SILVER_DIM = AbyssGuiTheme.SILVER_DIM;
    private static final int VIOLET     = AbyssGuiTheme.VIOLET;
    private static final int SLATE      = AbyssGuiTheme.OBSIDIAN;
    private static final int SLATE_EDGE = AbyssGuiTheme.EDGE;
    private static final int SLIME_RGB  = AbyssGuiTheme.SLIME_RGB;

    private static final int PANEL_W = 264;
    private static final int PANEL_H = 244;
    private static final int ROW_H = 22;

    private int coins;
    private int left, top;

    public BlackMarketScreen(int coins) {
        super(Component.literal("Mitsugoshi Black Market"));
        this.coins = coins;
    }

    public void updateCoins(int coins) {
        this.coins = coins;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        int rowY = top + 66;
        for (int i = 0; i < MarketManager.OFFERS.size(); i++) {
            final int index = i;
        addRenderableWidget(Button.builder(Component.literal("Buy"), b ->
                    send(ServerboundMarketActionPacket.BUY_BASE + index))
                    .bounds(left + PANEL_W - 78, rowY, 60, 18).build());
            rowY += ROW_H;
        }

        // Erzeugnisse -> Muenzen
        addRenderableWidget(Button.builder(Component.literal("Convert Trade Goods"), b ->
                send(ServerboundMarketActionPacket.CONVERT))
                .bounds(left + 24, top + PANEL_H - 52, PANEL_W - 48, 20).build());

        // zurueck
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> this.onClose())
                .bounds(left + 24, top + PANEL_H - 28, PANEL_W - 48, 20).build());
    }

    private void send(int action) {
        PacketDistributor.sendToServer(new ServerboundMarketActionPacket(action));
    }

    private int slimeColor() {
        double phase = (Util.getMillis() % 2600L) / 2600.0;
        float pulse = 0.6F + 0.4F * (float) (0.5 + 0.5 * Math.sin(phase * Math.PI * 2.0));
        int a = Math.max(0, Math.min(255, (int) (pulse * 255)));
        return (a << 24) | SLIME_RGB;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(g);
        int x2 = left + PANEL_W, y2 = top + PANEL_H;
        g.fill(left, top, x2, y2, SLATE);
        g.fill(left + 4, top + 4, x2 - 4, top + 5, SLATE_EDGE);
        g.fill(left + 4, y2 - 5, x2 - 4, y2 - 4, SLATE_EDGE);
        g.fill(left + 4, top + 4, left + 5, y2 - 4, SLATE_EDGE);
        g.fill(x2 - 5, top + 4, x2 - 4, y2 - 4, SLATE_EDGE);
        // pulsierender Schleim-Rand
        int c = slimeColor();
        int t = 4;
        g.fill(left - t, top - t, x2 + t, top, c);
        g.fill(left - t, y2, x2 + t, y2 + t, c);
        g.fill(left - t, top, left, y2, c);
        g.fill(x2, top, x2 + t, y2, c);
        g.fill(left + 60, y2 + t, left + 66, y2 + t + 7, c);
        g.fill(left + 180, y2 + t, left + 186, y2 + t + 5, c);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cx = left + PANEL_W / 2;
        g.drawCenteredString(this.font, Component.literal("M I T S U G O S H I"), cx, top + 14, SILVER);
        g.drawCenteredString(this.font, Component.literal("§oBlack Market of the Shadows"), cx, top + 28, VIOLET);
        g.drawString(this.font, "Coins:", left + 24, top + 46, SILVER_DIM, false);
        g.drawString(this.font, String.valueOf(coins), left + 82, top + 46, SILVER, false);

        int rowY = top + 66;
        for (MarketManager.Offer offer : MarketManager.OFFERS) {
            g.drawString(this.font, offer.count() + "x " + offer.label(), left + 24, rowY + 5, VIOLET, false);
            g.drawString(this.font, offer.cost() + " coins", left + 150, rowY + 5, SILVER_DIM, false);
            rowY += ROW_H;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
