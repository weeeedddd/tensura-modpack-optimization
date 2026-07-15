package net.tensura.abyss.bridge;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.tensura.abyss.TensuraAbyss;

import java.lang.reflect.Method;

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │  TENSURA-BRIDGE — der EINZIGE Ort, der Tensura-Interna beruehrt.            │
 * │                                                                            │
 * │  Bewusst per REFLECTION und maximal crash-sicher:                          │
 * │   • Kompiliert OHNE Tensura am Classpath.                                   │
 * │   • Lazy-Init + gecachte Method-Handles  -> KEINE Reflection pro Aufruf.    │
 * │   • Schlaegt die Tensura-API einmal fehl, wird der Modus DAUERHAFT auf      │
 * │     "unavailable" gesetzt  -> ab dann lag-frei nur noch Scoreboard.         │
 * │   • Es wird HOECHSTENS EINE INFO-Zeile geloggt (kein Error-/WARN-Spam,      │
 * │     keine Stacktraces).                                                     │
 * │   • Fallback = das Scoreboard "sg_magicule" / "sg_maxep" (identisch zu      │
 * │     den KubeJS-Skripten).                                                   │
 * └────────────────────────────────────────────────────────────────────────────┘
 *
 * >>> ZU VERIFIZIEREN: die Klassen-/Methodennamen unten gegen die installierten
 *     Tensura-/Ascensions-Jars abgleichen (z.B. via `javap` oder dem Open-Source-
 *     Repo). Stimmen sie nicht, laeuft alles verlustfrei ueber das Scoreboard.
 */
public final class TensuraBridge {
    private TensuraBridge() {}

    // ── Fallback-Scoreboards ──
    public static final String MAGICULE_OBJ = "sg_magicule";
    public static final String MAXEP_OBJ = "sg_maxep";

    // ── ZU VERIFIZIERENDE PFADE ──
    private static final String TENSURA_CAP_CLASS =
            "com.github.manasmods.tensura.capability.ep.TensuraEPCapability";
    private static final String CAP_GETTER    = "getFrom";     // static (Player) -> cap/optional
    private static final String M_GET_MAGICULE = "getMagicule"; // () -> double
    private static final String M_SET_MAGICULE = "setMagicule"; // (double)
    private static final String M_GET_MAXEP    = "getMaxEP";     // () -> double
    private static final String ASCENSION_RACE_HELPER =
            "com.github.manasmods.tensura_ascensions.race.RaceHelper";
    private static final String M_SET_RACE = "setRace";         // (Player, String|ResourceLocation)

    // ── Zustands-Cache (lazy) ──
    private enum State { UNKNOWN, AVAILABLE, UNAVAILABLE }
    private static volatile State epState = State.UNKNOWN;
    private static volatile State raceState = State.UNKNOWN;
    private static boolean degradeLogged = false;

    // gecachte Handles (nur wenn AVAILABLE)
    private static Method capGetter, mGetMag, mSetMag, mGetMaxEp;
    private static Method mSetRaceStr, mSetRaceRl;
    private static Class<?> resourceLocationClass, raceHelperClass;

    // ═══════════════════════════════ INIT ═══════════════════════════════
    private static void logDegradeOnce() {
        if (!degradeLogged) {
            degradeLogged = true;
            TensuraAbyss.LOGGER.info(
                "[TensuraBridge] Tensura-EP-API nicht erkannt — nutze lautlosen " +
                "Scoreboard-Fallback ('{}'/'{}'). Das ist erwartbar, falls die " +
                "Companion-Mod ohne Tensura laeuft.", MAGICULE_OBJ, MAXEP_OBJ);
        }
    }

    private static boolean epReady() {
        State s = epState;
        if (s == State.AVAILABLE) return true;
        if (s == State.UNAVAILABLE) return false;
        synchronized (TensuraBridge.class) {
            if (epState != State.UNKNOWN) return epState == State.AVAILABLE;
            try {
                Class<?> cap = Class.forName(TENSURA_CAP_CLASS);
                capGetter = cap.getMethod(CAP_GETTER, Player.class);
                // Getter/Setter werden am zurueckgegebenen Cap-Objekt gesucht (dynamisch),
                // hier nur die Existenz der Kernklasse pruefen:
                epState = State.AVAILABLE;
                return true;
            } catch (Throwable t) {
                epState = State.UNAVAILABLE;
                logDegradeOnce();
                return false;
            }
        }
    }

    /** Holt (einmalig aufgeloest) das Cap-Objekt; null bei Problemen -> Fallback. */
    private static Object cap(Player player) {
        try {
            Object result = capGetter.invoke(null, player);
            if (result == null) return null;
            try {
                Method orElse = result.getClass().getMethod("orElse", Object.class);
                Object unwrapped = orElse.invoke(result, new Object[]{ null });
                return unwrapped != null ? unwrapped : result;
            } catch (NoSuchMethodException ignored) {
                return result;
            }
        } catch (Throwable t) {
            markEpUnavailable();
            return null;
        }
    }

    private static void markEpUnavailable() {
        epState = State.UNAVAILABLE;
        logDegradeOnce();
    }

    private static Method cachedMethod(Object target, String name, Class<?>... args) throws NoSuchMethodException {
        return target.getClass().getMethod(name, args);
    }

    // ═══════════════════════════ SCOREBOARD-FALLBACK ═══════════════════════════
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

    /** Aktueller Magicule-Wert (Tensura, sonst Scoreboard-Fallback). */
    public static double getMagicules(Player player) {
        if (epReady()) {
            Object c = cap(player);
            if (c != null) {
                try {
                    if (mGetMag == null) mGetMag = cachedMethod(c, M_GET_MAGICULE);
                    Object v = mGetMag.invoke(c);
                    if (v instanceof Number n) return n.doubleValue();
                } catch (Throwable t) { markEpUnavailable(); }
            }
        }
        return readScore(player, MAGICULE_OBJ);
    }

    /** Setzt den Magicule-Wert absolut. */
    public static void setMagicules(Player player, double amount) {
        if (epReady()) {
            Object c = cap(player);
            if (c != null) {
                try {
                    if (mSetMag == null) mSetMag = cachedMethod(c, M_SET_MAGICULE, double.class);
                    mSetMag.invoke(c, amount);
                    return;
                } catch (Throwable t) { markEpUnavailable(); }
            }
        }
        writeScore(player, MAGICULE_OBJ, (int) Math.round(amount));
    }

    /** Erhoeht den Magicule-Wert (z.B. Dunkler Schleim: +10.000). */
    public static void addMagicules(Player player, double delta) {
        double cur = getMagicules(player);
        setMagicules(player, cur + delta);
    }

    /** Maximale Existence Points (I-Am-Atomic-Gate). Fallback: sg_maxep. */
    public static double getMaxEP(Player player) {
        if (epReady()) {
            Object c = cap(player);
            if (c != null) {
                try {
                    if (mGetMaxEp == null) mGetMaxEp = cachedMethod(c, M_GET_MAXEP);
                    Object v = mGetMaxEp.invoke(c);
                    if (v instanceof Number n) return n.doubleValue();
                } catch (Throwable t) { markEpUnavailable(); }
            }
        }
        return readScore(player, MAXEP_OBJ);
    }

    /**
     * In-Mod-Rassenaenderung ueber Tensura Ascensions.
     * @return true, wenn der echte API-Aufruf durchlief; false -> KubeJS nutzt
     *         Rang/Praefix als sichtbaren Fallback.
     */
    public static boolean setTensuraRace(Player player, String racePath) {
        if (!raceReady()) return false;
        try {
            if (mSetRaceStr != null) {
                mSetRaceStr.invoke(null, player, racePath);
                return true;
            }
            if (mSetRaceRl != null && resourceLocationClass != null) {
                Method parse = resourceLocationClass.getMethod("parse", String.class);
                Object id = parse.invoke(null, racePath);
                mSetRaceRl.invoke(null, player, id);
                return true;
            }
        } catch (Throwable t) {
            raceState = State.UNAVAILABLE; // dauerhaft degradieren, kein Spam
        }
        return false;
    }

    private static boolean raceReady() {
        State s = raceState;
        if (s == State.AVAILABLE) return true;
        if (s == State.UNAVAILABLE) return false;
        synchronized (TensuraBridge.class) {
            if (raceState != State.UNKNOWN) return raceState == State.AVAILABLE;
            try {
                raceHelperClass = Class.forName(ASCENSION_RACE_HELPER);
                try {
                    mSetRaceStr = raceHelperClass.getMethod(M_SET_RACE, Player.class, String.class);
                } catch (NoSuchMethodException nse) {
                    resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
                    mSetRaceRl = raceHelperClass.getMethod(M_SET_RACE, Player.class, resourceLocationClass);
                }
                raceState = State.AVAILABLE;
                return true;
            } catch (Throwable t) {
                raceState = State.UNAVAILABLE;
                return false; // stiller Fallback, keine Log-Zeile noetig
            }
        }
    }

    /** Gilden-Gate: Spieler mit Tensura-Rasse (Possessed+) ODER gesetztem Score. */
    public static boolean hasTensuraRace(Player player) {
        return getMagicules(player) > 0.0 || getMaxEP(player) > 0.0;
    }
}
