package net.tensura.abyss.bridge;

import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.RaceAPI;
import io.github.manasmods.manascore.race.api.Races;
import io.github.manasmods.manascore.storage.api.StorageHolder;
import io.github.manasmods.tensura.storage.ep.ExistenceStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.tensura.abyss.TensuraAbyss;

import java.util.Optional;

/**
 * TENSURA BRIDGE — the single place that touches Tensura/ManasCore internals.
 *
 * Verified via javap against the REAL 1.21.1 jars (tensura-reincarnated
 * 643695:7905367, manascore 4.0.0.2):
 *   • EP/Magicules:  {@link ExistenceStorage} obtained through
 *     {@code ((StorageHolder) player).manasCore$getStorage(ExistenceStorage.getKey())}
 *     with {@code getMagicule()/setMagicule(double)/getEP()}.
 *   • Race:          {@link RaceAPI#getRaceFrom} → {@link Races#getRace()}
 *     → {@link ManasRaceInstance#getRaceId()} / {@link Races#setRace}.
 *
 * Every call is wrapped in try/catch with a silent scoreboard fallback
 * ("sg_magicule"/"sg_maxep") so a Tensura API change can never crash the pack.
 * KubeJS scripts load this class via {@code Java.loadClass(...)}.
 */
public final class TensuraBridge {
    private TensuraBridge() {}

    // ── Fallback scoreboards (shared with the KubeJS scripts) ──
    public static final String MAGICULE_OBJ = "sg_magicule";
    public static final String MAXEP_OBJ = "sg_maxep";

    private static boolean degradeLogged = false;

    private static void logDegradeOnce(Throwable t) {
        if (!degradeLogged) {
            degradeLogged = true;
            TensuraAbyss.LOGGER.info(
                "[TensuraBridge] Tensura storage API unavailable ({}) — using the "
                + "silent scoreboard fallback ('{}'/'{}').",
                t.getClass().getSimpleName(), MAGICULE_OBJ, MAXEP_OBJ);
        }
    }

    /** The player's Tensura existence storage, or null when unavailable. */
    private static ExistenceStorage storage(Player player) {
        try {
            return ((StorageHolder) player).manasCore$getStorage(ExistenceStorage.getKey());
        } catch (Throwable t) {
            logDegradeOnce(t);
            return null;
        }
    }

    // ═══════════════════════════ SCOREBOARD FALLBACK ═══════════════════════════
    private static Scoreboard scoreboard(Player p) {
        return p.level().getScoreboard();
    }

    private static Objective ensureObjective(Scoreboard sb, String name) {
        Objective o = sb.getObjective(name);
        if (o == null) {
            o = sb.addObjective(name, ObjectiveCriteria.DUMMY,
                    Component.literal(name), ObjectiveCriteria.RenderType.INTEGER, false, null);
        }
        return o;
    }

    private static int readScore(Player p, String obj) {
        Scoreboard sb = scoreboard(p);
        Objective o = sb.getObjective(obj);
        if (o == null) return 0;
        ScoreAccess acc = sb.getOrCreatePlayerScore((ScoreHolder) p, o);
        return acc.get();
    }

    private static void writeScore(Player p, String obj, int value) {
        Scoreboard sb = scoreboard(p);
        Objective o = ensureObjective(sb, obj);
        ScoreAccess acc = sb.getOrCreatePlayerScore((ScoreHolder) p, o);
        acc.set(value);
    }

    // ═══════════════════════════════ PUBLIC API ═══════════════════════════════

    /** Current magicule value (real Tensura storage; scoreboard fallback). */
    public static double getMagicules(Player player) {
        ExistenceStorage s = storage(player);
        if (s != null) {
            try { return s.getMagicule(); } catch (Throwable t) { logDegradeOnce(t); }
        }
        return readScore(player, MAGICULE_OBJ);
    }

    /** Sets the magicule value absolutely and syncs it to the client HUD. */
    public static void setMagicules(Player player, double amount) {
        ExistenceStorage s = storage(player);
        if (s != null) {
            try {
                s.setMagicule(amount);
                ((StorageHolder) player).manasCore$sync();
                return;
            } catch (Throwable t) { logDegradeOnce(t); }
        }
        writeScore(player, MAGICULE_OBJ, (int) Math.round(amount));
    }

    /** Adds magicules (e.g. Refined Dark Slime: +10,000). */
    public static void addMagicules(Player player, double delta) {
        setMagicules(player, getMagicules(player) + delta);
    }

    /** Total Existence Points (the "I Am Atomic" gate). Fallback: sg_maxep. */
    public static double getMaxEP(Player player) {
        ExistenceStorage s = storage(player);
        if (s != null) {
            try { return s.getEP(); } catch (Throwable t) { logDegradeOnce(t); }
        }
        return readScore(player, MAXEP_OBJ);
    }

    /** The player's current ManasCore race id, or null when raceless. */
    public static ResourceLocation getRaceId(Player player) {
        try {
            Optional<ManasRaceInstance> race = RaceAPI.getRaceFrom(player).getRace();
            return race.map(ManasRaceInstance::getRaceId).orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }

    /** True when the player's active race matches {@code raceId} exactly. */
    public static boolean hasRace(Player player, ResourceLocation raceId) {
        return raceId != null && raceId.equals(getRaceId(player));
    }

    /** Convenience overload for KubeJS ({@code BRIDGE.hasRaceId(p, "modid:path")}). */
    public static boolean hasRaceId(Player player, String raceId) {
        try {
            return hasRace(player, ResourceLocation.parse(raceId));
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Sets the player's race through the native ManasCore race system
     * (the same path the Tensura menu uses). Skills are kept.
     *
     * @return true when the race change went through.
     */
    public static boolean setTensuraRace(Player player, String racePath) {
        try {
            ResourceLocation id = ResourceLocation.parse(racePath);
            return RaceAPI.getRaceFrom(player).setRace(id, false);
        } catch (Throwable t) {
            return false;
        }
    }

    /** Guild gate: the player carries any ManasCore race at all. */
    public static boolean hasTensuraRace(Player player) {
        return getRaceId(player) != null;
    }
}
