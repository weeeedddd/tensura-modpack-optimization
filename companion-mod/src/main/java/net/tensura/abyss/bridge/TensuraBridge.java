package net.tensura.abyss.bridge;

import net.minecraft.world.entity.player.Player;
import net.tensura.abyss.TensuraAbyss;

import java.lang.reflect.Method;

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │  TENSURA-BRIDGE — der EINZIGE Ort, der Tensura-Interna beruehrt.            │
 * │                                                                            │
 * │  Bewusst per REFLECTION gebaut: Diese Klasse KOMPILIERT ohne Tensura am    │
 * │  Classpath und faellt zur Laufzeit sauber zurueck, falls ein Pfad/Name     │
 * │  nicht stimmt (kein Crash). Inspiriert von Robinator1103s ArcanePotions,   │
 * │  das ueber das NeoForge-1.21.1-Attachment-System auf Tensura-Spielerdaten  │
 * │  (Magicules/EP/Skills) zugreift.                                           │
 * │                                                                            │
 * │  >>> DIE FOLGENDEN KONSTANTEN GEGEN DIE INSTALLIERTEN JARS VERIFIZIEREN <<< │
 * │  (Klassen-/Methodennamen aus Tensura Reincarnated & Ascensions eintragen,  │
 * │   z.B. via `javap` auf die jars oder aus dem Open-Source-Repo ablesen.)    │
 * └────────────────────────────────────────────────────────────────────────────┘
 */
public final class TensuraBridge {
    private TensuraBridge() {}

    // ── ZU VERIFIZIERENDE PFADE (Platzhalter-Namen bewusst als Konstanten) ──
    // Tensura Reincarnated: Zugriff auf die Player-Capability/Attachment.
    private static final String TENSURA_CAP_CLASS =
            "com.github.manasmods.tensura.capability.ep.TensuraEPCapability";
    private static final String TENSURA_CAP_GETTER = "getFrom";      // (Player) -> LazyOptional/Optional<Cap>
    private static final String CAP_GET_EP        = "getEP";         // () -> double  (aktuelle Existence Points)
    private static final String CAP_GET_MAX_EP    = "getMaxEP";      // () -> double
    private static final String CAP_SET_EP        = "setEP";         // (double)
    private static final String CAP_GET_MAGICULE  = "getMagicule";   // () -> double
    private static final String CAP_SET_MAGICULE  = "setMagicule";   // (double)

    // Tensura Ascensions: Rassen-/Evolutions-Steuerung.
    private static final String ASCENSION_RACE_HELPER =
            "com.github.manasmods.tensura_ascensions.race.RaceHelper";
    private static final String ASCENSION_SET_RACE = "setRace";      // (Player, ResourceLocation/String)
    private static final String TENSURA_RACE_HELPER =
            "com.github.manasmods.tensura.registry.race.TensuraRaces";

    private static boolean warned = false;
    private static void warnOnce(Throwable t) {
        if (!warned) {
            warned = true;
            TensuraAbyss.LOGGER.warn("[TensuraBridge] Tensura-API nicht erreichbar/Namen pruefen: {}",
                    t.toString());
        }
    }

    /** Holt das Tensura-EP/Magicule-Capability-Objekt fuer den Spieler (oder null). */
    private static Object cap(Player player) {
        try {
            Class<?> capClass = Class.forName(TENSURA_CAP_CLASS);
            Method getter = capClass.getMethod(TENSURA_CAP_GETTER, Player.class);
            Object result = getter.invoke(null, player);
            if (result == null) return null;
            // LazyOptional/Optional -> auspacken, falls noetig
            try {
                Method orElse = result.getClass().getMethod("orElse", Object.class);
                Object unwrapped = orElse.invoke(result, new Object[]{ null });
                return unwrapped != null ? unwrapped : result;
            } catch (NoSuchMethodException ignored) {
                return result; // war schon das Cap-Objekt
            }
        } catch (Throwable t) {
            warnOnce(t);
            return null;
        }
    }

    private static double callDouble(Object target, String method, double fallback) {
        if (target == null) return fallback;
        try {
            Method m = target.getClass().getMethod(method);
            Object v = m.invoke(target);
            return v instanceof Number n ? n.doubleValue() : fallback;
        } catch (Throwable t) {
            warnOnce(t);
            return fallback;
        }
    }

    private static void callSetDouble(Object target, String method, double value) {
        if (target == null) return;
        try {
            Method m = target.getClass().getMethod(method, double.class);
            m.invoke(target, value);
        } catch (Throwable t) {
            warnOnce(t);
        }
    }

    // ═══════════════ OEFFENTLICHE API (fuer KubeJS & Gilden-Backend) ═══════════════

    /** Aktueller Magicule-Wert des Spielers (0.0, wenn nicht verfuegbar). */
    public static double getMagicules(Player player) {
        return callDouble(cap(player), CAP_GET_MAGICULE, 0.0);
    }

    /** Setzt den Magicule-Wert absolut. */
    public static void setMagicules(Player player, double amount) {
        callSetDouble(cap(player), CAP_SET_MAGICULE, amount);
        syncTensura(player);
    }

    /** Erhoeht den Magicule-Wert (z.B. Dunkler Schleim: +10.000). */
    public static void addMagicules(Player player, double delta) {
        Object c = cap(player);
        double cur = callDouble(c, CAP_GET_MAGICULE, 0.0);
        callSetDouble(c, CAP_SET_MAGICULE, cur + delta);
        syncTensura(player);
    }

    /** Maximale Existence Points (fuer das I-Am-Atomic-Gate: >= 5.000.000). */
    public static double getMaxEP(Player player) {
        return callDouble(cap(player), CAP_GET_MAX_EP, 0.0);
    }

    /** Aktuelle Existence Points. */
    public static double getEP(Player player) {
        return callDouble(cap(player), CAP_GET_EP, 0.0);
    }

    /**
     * Loest die In-Mod-Rassenaenderung ueber Tensura Ascensions aus.
     * racePath z.B. "tensura_abyss:shadow_garden_member".
     * @return true, wenn der Aufruf technisch durchlief.
     */
    public static boolean setTensuraRace(Player player, String racePath) {
        try {
            Class<?> helper = Class.forName(ASCENSION_RACE_HELPER);
            // Zwei gaengige Signaturen versuchen: (Player, String) oder (Player, ResourceLocation)
            try {
                Method m = helper.getMethod(ASCENSION_SET_RACE, Player.class, String.class);
                m.invoke(null, player, racePath);
                return true;
            } catch (NoSuchMethodException nsme) {
                Class<?> rl = Class.forName("net.minecraft.resources.ResourceLocation");
                Method parse = rl.getMethod("parse", String.class);
                Object id = parse.invoke(null, racePath);
                Method m = helper.getMethod(ASCENSION_SET_RACE, Player.class, rl);
                m.invoke(null, player, id);
                return true;
            }
        } catch (Throwable t) {
            warnOnce(t);
            return false;
        }
    }

    /** Prueft, ob der Spieler ueberhaupt eine Tensura-Rasse besitzt (Gilden-Gate). */
    public static boolean hasTensuraRace(Player player) {
        // Ein Spieler mit >0 Magicule ODER gesetzter Rasse gilt als "Possessed oder hoeher".
        return getMagicules(player) > 0.0 || getMaxEP(player) > 0.0;
    }

    /** Stoesst Tensuras eigene Sync-Logik an, falls vorhanden (best effort). */
    private static void syncTensura(Player player) {
        try {
            Class<?> capClass = Class.forName(TENSURA_CAP_CLASS);
            try {
                Method sync = capClass.getMethod("sync", Player.class);
                sync.invoke(null, player);
            } catch (NoSuchMethodException ignored) { /* kein Sync verfuegbar */ }
        } catch (Throwable ignored) { /* egal — Werte sind gesetzt */ }
    }
}
