package net.tensura.abyss.client.hud;

import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.RaceAPI;
import io.github.manasmods.manascore.storage.api.StorageHolder;
import io.github.manasmods.tensura.storage.ep.ExistenceStorage;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.tensura.abyss.client.ClientHudState;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * TENSURA ABYSS — sidebar status HUD (right edge, vertically centered).
 *
 * Pure client-side render layer: no scoreboard packets, no server ticking,
 * therefore zero jitter/flicker — values are read fresh each frame from
 * ManasCore's client-synced storages.
 *
 * Rows: player name · active race · shadow rank · magicules · EP · guild.
 * Palette: deep translucent panel, §5 violet accents, soft gray labels.
 */
public final class AbyssSidebarHud implements LayeredDraw.Layer {

    // ── Palette (ARGB) ──
    private static final int PANEL_BG     = 0xAA0B0713; // deep translucent violet-black
    private static final int PANEL_EDGE   = 0xFF3B2360; // violet border
    private static final int ACCENT       = 0xFF9D6BFF; // bright violet
    private static final int TITLE        = 0xFFCBB3FF;
    private static final int LABEL        = 0xFF8C86A0;
    private static final int VALUE        = 0xFFEFEAFF;
    private static final int GOLD         = 0xFFE8C36A;

    private static final int PAD = 6;
    private static final int ROW_H = 11;
    private static final NumberFormat FMT = NumberFormat.getIntegerInstance(Locale.US);

    @Override
    public void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || mc.getDebugOverlay().showDebugScreen()) return;

        Font font = mc.font;

        // ── Gather rows (label, value, valueColor) ──
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Name", player.getGameProfile().getName()});

        String race = "Unawakened";
        try {
            Optional<ManasRaceInstance> inst = RaceAPI.getRaceFrom(player).getRace();
            if (inst.isPresent()) race = inst.get().getDisplayName().getString();
        } catch (Throwable ignored) {}
        rows.add(new String[]{"Race", race});

        rows.add(new String[]{"Shadow Rank", shadowRank(player)});

        double magicules = 0, ep = 0;
        try {
            ExistenceStorage s = ((StorageHolder) player).manasCore$getStorage(ExistenceStorage.getKey());
            if (s != null) { magicules = s.getMagicule(); ep = s.getEP(); }
        } catch (Throwable ignored) {}
        rows.add(new String[]{"Magicules", FMT.format(Math.round(magicules))});
        rows.add(new String[]{"EP", FMT.format(Math.round(ep))});

        String guild = ClientHudState.guildName();
        if (!guild.isEmpty()) rows.add(new String[]{"Guild", guild});

        // ── Layout ──
        String title = "— TENSURA ABYSS —";
        int innerW = font.width(title);
        for (String[] row : rows) {
            innerW = Math.max(innerW, font.width(row[0] + "  " + row[1]));
        }
        int panelW = innerW + PAD * 2;
        int panelH = PAD + ROW_H + 3 + rows.size() * ROW_H + PAD - 2;

        int screenW = g.guiWidth();
        int x = screenW - panelW - 4;
        int y = (g.guiHeight() - panelH) / 2;

        // ── Panel: translucent body + 1px violet frame + accent spine ──
        g.fill(x, y, x + panelW, y + panelH, PANEL_BG);
        g.fill(x, y, x + panelW, y + 1, PANEL_EDGE);
        g.fill(x, y + panelH - 1, x + panelW, y + panelH, PANEL_EDGE);
        g.fill(x, y, x + 1, y + panelH, PANEL_EDGE);
        g.fill(x + panelW - 1, y, x + panelW, y + panelH, PANEL_EDGE);
        g.fill(x, y, x + 1, y + panelH, ACCENT); // left accent spine

        // ── Title + divider ──
        int ty = y + PAD - 1;
        g.drawString(font, Component.literal(title), x + (panelW - font.width(title)) / 2, ty, TITLE, true);
        int divY = ty + ROW_H;
        g.fill(x + PAD, divY, x + panelW - PAD, divY + 1, PANEL_EDGE);

        // ── Rows ──
        int ry = divY + 4;
        for (String[] row : rows) {
            g.drawString(font, Component.literal(row[0]), x + PAD, ry, LABEL, false);
            String value = row[1];
            int vColor = switch (row[0]) {
                case "Race" -> ACCENT;
                case "Shadow Rank" -> "None".equals(value) ? LABEL : ACCENT;
                case "Magicules", "EP" -> GOLD;
                default -> VALUE;
            };
            g.drawString(font, Component.literal(value), x + panelW - PAD - font.width(value), ry, vColor, false);
            ry += ROW_H;
        }
    }

    /** Maps the vanilla team (set by the pledge system) to a display rank. */
    private static String shadowRank(LocalPlayer player) {
        PlayerTeam team = player.getTeam() instanceof PlayerTeam pt ? pt : null;
        if (team == null) return "None";
        return switch (team.getName()) {
            case "sg_lord"    -> "Shadow Lord";
            case "sg_seven"   -> "Seven Shadows";
            case "sg_numbers" -> "Numbers";
            case "sg_shadow"  -> "Shadow";
            default            -> "None";
        };
    }
}
