package net.tensura.abyss.client.screen;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tensura.abyss.network.ServerboundGuildInvitePacket;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Einladungs-Screen: ein altes, dunkles Pergament-Dokument mit leuchtend
 * violettem Schleim-Siegel. Namensfeld mit TAB-Autocomplete (Online-Spieler)
 * und ein mehrzeiliges Feld fuer die Geheimbotschaft.
 */
public class GuildInviteScreen extends Screen {

    private static final int PARCH      = 0xFFB8A06E;
    private static final int PARCH_EDGE = 0xFF5A4A2E;
    private static final int INK        = 0xFF2A1E12;
    private static final int INK_DIM    = 0xFF4A3A22;
    private static final int VIOLET     = 0xFFB56BE0;
    private static final int SEAL_RGB   = 0x8A34D0;

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 210;

    private final Screen parent;
    private int left, top;

    private EditBox nameField;
    private MultiLineEditBox noteField;

    // Autocomplete-Zustand
    private String completionBase = null;
    private List<String> completions = new ArrayList<>();
    private int completionIndex = 0;

    public GuildInviteScreen(Screen parent) {
        super(Component.literal("Covert Order"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        nameField = new EditBox(this.font, left + 24, top + 44, PANEL_W - 48, 18,
                Component.literal("Player name"));
        nameField.setMaxLength(32);
        nameField.setHint(Component.literal("Enter a name… (TAB)"));
        addRenderableWidget(nameField);

        noteField = new MultiLineEditBox(this.font, left + 24, top + 84, PANEL_W - 48, 66,
                Component.literal("Join the shadows…"),
                Component.literal("Secret message"));
        noteField.setValue("");
        addRenderableWidget(noteField);

        int btnW = 96, btnH = 20;
        addRenderableWidget(Button.builder(Component.literal("§5» Send «"), b -> send())
                .bounds(left + 24, top + PANEL_H - 30, btnW, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("§8Back"), b ->
                Minecraft.getInstance().setScreen(parent))
                .bounds(left + PANEL_W - 24 - btnW, top + PANEL_H - 30, btnW, btnH).build());

        setInitialFocus(nameField);
    }

    private void send() {
        String name = nameField.getValue().trim();
        if (name.isEmpty()) return;
        String note = noteField.getValue();

        PacketDistributor.sendToServer(new ServerboundGuildInvitePacket(name, note));

        // kurzer, mystischer Sound
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 0.7F));

        Minecraft.getInstance().setScreen(null); // zurueck ins Spiel
    }

    // ── TAB-Autocomplete ueber Online-Spieler ──
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB && nameField.isFocused()) {
            autocomplete();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void autocomplete() {
        String current = nameField.getValue();
        if (completionBase == null || !current.equalsIgnoreCase(prevCompletion())) {
            completionBase = current;
            completions = onlineNamesMatching(current);
            completionIndex = 0;
        }
        if (completions.isEmpty()) return;
        String pick = completions.get(completionIndex % completions.size());
        completionIndex++;
        nameField.setValue(pick); // Cursor steht danach am Ende
    }

    private String prevCompletion() {
        if (completions.isEmpty()) return completionBase == null ? "" : completionBase;
        int idx = (completionIndex - 1);
        if (idx < 0) return completionBase;
        return completions.get(idx % completions.size());
    }

    private List<String> onlineNamesMatching(String prefix) {
        List<String> out = new ArrayList<>();
        var conn = Minecraft.getInstance().getConnection();
        if (conn == null) return out;
        String p = prefix.toLowerCase(Locale.ROOT);
        for (PlayerInfo info : conn.getOnlinePlayers()) {
            String n = info.getProfile().getName();
            if (n != null && (p.isEmpty() || n.toLowerCase(Locale.ROOT).startsWith(p))) {
                out.add(n);
            }
        }
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    // ── Rendering: dunkles Pergament + Schleim-Siegel ──
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(g);
        int x2 = left + PANEL_W, y2 = top + PANEL_H;
        // Pergament-Rand
        g.fill(left - 3, top - 3, x2 + 3, y2 + 3, PARCH_EDGE);
        // Pergament-Flaeche
        g.fill(left, top, x2, y2, PARCH);
        // Alterungs-Flecken
        g.fill(left + 12, top + 14, left + 40, top + 20, 0x22000000);
        g.fill(x2 - 46, top + 30, x2 - 18, top + 36, 0x1A000000);
        g.fill(left + 20, y2 - 40, left + 60, y2 - 36, 0x22000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cx = left + PANEL_W / 2;
        g.drawCenteredString(this.font, Component.literal("» Covert Order «"), cx, top + 14, INK);
        g.drawString(this.font, "An:", left + 24, top + 32, INK_DIM, false);
        g.drawString(this.font, "Botschaft:", left + 24, top + 72, INK_DIM, false);
        drawSeal(g, x_seal(), top + PANEL_H - 34);
    }

    private int x_seal() {
        return left + PANEL_W / 2;
    }

    /** Leuchtend violettes Schleim-Siegel mit pulsierendem Glow + Goldrand. */
    private void drawSeal(GuiGraphics g, int cx, int cy) {
        double phase = (Util.getMillis() % 2400L) / 2400.0;
        float pulse = 0.6F + 0.4F * (float) (0.5 + 0.5 * Math.sin(phase * Math.PI * 2.0));
        int a = Math.max(0, Math.min(255, (int) (pulse * 255)));
        int seal = (a << 24) | SEAL_RGB;
        int rim = 0xFFD4AF37;
        int r = 11;
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r * r) {
                    int col = (d2 >= (r - 1) * (r - 1)) ? rim : seal;
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, col);
                }
            }
        }
        g.drawCenteredString(this.font, Component.literal("S"), cx, cy - 4, 0xFFF0E0FF);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
