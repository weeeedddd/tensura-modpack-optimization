package net.tensura.abyss.registry;

import net.tensura.abyss.TensuraAbyss;
import net.tensura.abyss.race.AbyssRace;
import net.tensura.abyss.race.AbyssRaceDef;

// ── ECHTE 1.21.1-API (io.github.manasmods, per javap verifiziert) ──
import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.api.RaceAPI;
import dev.architectury.registry.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registriert die 37 Tensura-Abyss-Custom-Rassen (4 Pfade x 9 + geheime Rasse)
 * in der ManasCore/Tensura-Race-Registry, damit sie im ORIGINALEN
 * Evolutionsmenue auftauchen und von Tensura: Ascension erkannt werden.
 *
 * <p><b>Mechanik (per javap gegen die 1.21.1-Jars verifiziert):</b> Rassen
 * werden ueber einen <em>Architectury</em>-{@link DeferredRegister} in die von
 * {@link RaceAPI#getRaceRegistryKey()} bereitgestellte Registry eingetragen.
 * Der registrierte Typ ist ManasCores {@link ManasRace}; jede Rasse ist ein
 * {@link AbyssRace} (erbt von {@code io.github.manasmods.tensura.race.TensuraRace}).
 *
 * <p>Stat-Werte generiert aus config/tensura/ascension-races.toml (konsistent).
 * Die 7 Legacy-Rassen sind bewusst nicht dabei (abgeloestes virtuelles System).
 */
public final class AbyssRaces {
    private AbyssRaces() {}

    /** Architectury-DeferredRegister auf die ManasCore-Race-Registry. */
    public static final DeferredRegister<ManasRace> RACES =
            DeferredRegister.create(TensuraAbyss.MOD_ID, RaceAPI.getRaceRegistryKey());

    /** Alle 37 Abyss-Rassen (4 Pfade x 9 Stufen + geheime Rasse). */
    public static final List<AbyssRaceDef> DEFS = List.of(
            // Shadow Slime: resilient controller, culminating in I Am Atomic.
            def("shadow_slime", "slime", 1, 10, 1.0, .103, 40, 70, 0),
            def("magicule_slime", "slime", 2, 14, 1.5, .107, 95, 115, 0),
            def("abyss_slime", "slime", 3, 20, 2.0, .111, 170, 185, 1),
            def("shadow_garden_guard", "slime", 4, 28, 3.0, .116, 300, 275, 2),
            def("dark_slime_sovereign", "slime", 5, 38, 4.5, .122, 510, 415, 3),
            def("shadow_lord", "slime", 6, 50, 6.0, .129, 850, 600, 4),
            def("awakened_shadow_lord", "slime", 7, 66, 8.0, .136, 1500, 875, 5),
            def("abyss_monarch", "slime", 8, 86, 10.5, .143, 2800, 1265, 6),
            def("eminence_of_the_abyss", "slime", 9, 110, 14.0, .150, 5400, 2070, 7),

            // Shadow Demon: highest raw offense, lowest defensive curve.
            def("low_shadow_demon", "demon", 1, 8, 1.5, .102, 50, 60, 0),
            def("shadow_demon_peer", "demon", 2, 12, 2.0, .106, 115, 100, 0),
            def("blood_shadow_demon", "demon", 3, 17, 3.0, .110, 200, 160, 1),
            def("arcane_demon_guard", "demon", 4, 24, 4.0, .115, 350, 240, 1.5),
            def("arch_demon_of_shadows", "demon", 5, 33, 5.5, .121, 600, 360, 2.5),
            def("shadow_duke", "demon", 6, 45, 7.0, .127, 1000, 520, 3.5),
            def("awakened_demon_king", "demon", 7, 60, 9.5, .133, 1750, 760, 4.5),
            def("void_overlord", "demon", 8, 78, 12.0, .139, 3250, 1100, 5.5),
            def("diablos_eminence", "demon", 9, 100, 15.5, .145, 6250, 1800, 6.5),

            // Ancient Hero: mobile martial specialist.
            def("human_apprentice", "hero", 1, 6, 2.0, .105, 34, 55, 0),
            def("shadow_spellsword", "hero", 2, 10, 2.5, .110, 76, 92, 0),
            def("shadow_blade", "hero", 3, 15, 3.5, .115, 136, 148, .5),
            def("cult_breaker", "hero", 4, 22, 4.5, .121, 240, 220, 1),
            def("master_of_garden", "hero", 5, 31, 6.0, .127, 410, 330, 2),
            def("ancient_knight", "hero", 6, 42, 7.5, .134, 680, 480, 3),
            def("true_hero_of_shadows", "hero", 7, 56, 10.0, .141, 1190, 700, 4),
            def("light_shadow_monarch", "hero", 8, 72, 13.0, .148, 2210, 1010, 5),
            def("sovereign_of_midnight", "hero", 9, 90, 16.0, .155, 4250, 1650, 6),

            // Progenitor: sustain and night mobility.
            def("vampire_spawn", "vampire", 1, 12, 1.0, .104, 38, 57, 0),
            def("blood_shadow", "vampire", 2, 17, 1.5, .108, 86, 95, 0),
            def("mist_walker", "vampire", 3, 23, 2.5, .113, 153, 152, 1),
            def("crimson_noble", "vampire", 4, 31, 3.5, .119, 270, 228, 2),
            def("night_stalker", "vampire", 5, 42, 5.0, .125, 460, 342, 3),
            def("pureblood_vampire", "vampire", 6, 56, 6.5, .132, 765, 494, 4),
            def("awakened_blood_lord", "vampire", 7, 72, 8.5, .139, 1340, 722, 5),
            def("monarch_of_the_red_moon", "vampire", 8, 92, 11.0, .146, 2490, 1045, 6),
            def("progenitor_of_the_abyss", "vampire", 9, 115, 14.0, .152, 5000, 1710, 7),

            // Quest-only capstone: powerful, but no longer a four-digit-health cheat code.
            def("stylish_bandit_slayer", "secret", 9, 130, 18.0, .160, 7500, 2400, 8)
    );

    private static AbyssRaceDef def(String id, String path, int stage, double health,
                                    double attack, double speed, double aura,
                                    double magicules, double toughness) {
        return new AbyssRaceDef(id, path, stage, health, attack, speed, aura, magicules, toughness);
    }

    // ═══════════════ EVOLUTION CHAINS (native menu integration) ═══════════════
    // Each tree is a strict 9-stage ladder. These chains feed AbyssRace's
    // getNextEvolutions/getPreviousEvolutions overrides so the NATIVE Tensura
    // evolution menu shows the full path instead of dead-ending after the
    // starter race. The secret race stays quest-only (no chain entry).
    private static final String[][] TREES = {
            { "shadow_slime", "magicule_slime", "abyss_slime", "shadow_garden_guard",
              "dark_slime_sovereign", "shadow_lord", "awakened_shadow_lord",
              "abyss_monarch", "eminence_of_the_abyss" },
            { "low_shadow_demon", "shadow_demon_peer", "blood_shadow_demon",
              "arcane_demon_guard", "arch_demon_of_shadows", "shadow_duke",
              "awakened_demon_king", "void_overlord", "diablos_eminence" },
            { "human_apprentice", "shadow_spellsword", "shadow_blade", "cult_breaker",
              "master_of_garden", "ancient_knight", "true_hero_of_shadows",
              "light_shadow_monarch", "sovereign_of_midnight" },
            { "vampire_spawn", "blood_shadow", "mist_walker", "crimson_noble",
              "night_stalker", "pureblood_vampire", "awakened_blood_lord",
              "monarch_of_the_red_moon", "progenitor_of_the_abyss" }
    };

    /** EP needed to REACH stage index i (mirrors the KubeJS magicule curve). */
    private static final double[] STAGE_EP =
            { 0, 5_000, 15_000, 40_000, 100_000, 250_000, 600_000, 1_400_000, 3_000_000 };

    /** race id -> next race id in its tree (absent = final form). */
    public static final Map<String, String> NEXT = new HashMap<>();
    /** race id -> previous race id in its tree (absent = starter). */
    public static final Map<String, String> PREV = new HashMap<>();
    /** race id -> EP required to evolve INTO that race. */
    public static final Map<String, Double> EVOLUTION_EP = new HashMap<>();

    // Alle Rassen beim DeferredRegister anmelden (laeuft beim Klassen-Laden).
    static {
        for (String[] tree : TREES) {
            for (int i = 0; i < tree.length; i++) {
                if (i + 1 < tree.length) NEXT.put(tree[i], tree[i + 1]);
                if (i > 0) PREV.put(tree[i], tree[i - 1]);
                EVOLUTION_EP.put(tree[i], STAGE_EP[i]);
            }
        }
        for (AbyssRaceDef def : DEFS) {
            RACES.register(def.id(), () -> new AbyssRace(def));
        }
    }

    /**
     * Finalisiert die Registrierung. Wird einmalig aus dem Mod-Konstruktor
     * aufgerufen (Architectury-DeferredRegister#register).
     */
    public static void register() {
        RACES.register();
        TensuraAbyss.LOGGER.info("[Tensura Abyss] {} Custom-Rassen registriert.", DEFS.size());
    }

    /** Anzahl der Rassen (fuers Log). */
    public static int count() {
        return DEFS.size();
    }
}
