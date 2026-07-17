package net.tensura.abyss.registry;

import net.tensura.abyss.TensuraAbyss;
import net.tensura.abyss.race.AbyssRace;
import net.tensura.abyss.race.AbyssRaceDef;

// ── ECHTE 1.21.1-API (io.github.manasmods, per javap verifiziert) ──
import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.api.RaceAPI;
import dev.architectury.registry.registries.DeferredRegister;

import java.util.List;

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

            // ── PFAD 1 — Shadow Slime ──
            new AbyssRaceDef("shadow_slime", 45.0, 4.0, 0.105, 40.0, 70.0),
            new AbyssRaceDef("magicule_slime", 72.0, 6.0, 0.11, 95.0, 115.0),
            new AbyssRaceDef("abyss_slime", 117.0, 8.0, 0.115, 170.0, 185.0),
            new AbyssRaceDef("shadow_garden_guard", 180.0, 11.0, 0.12, 300.0, 275.0),
            new AbyssRaceDef("dark_slime_sovereign", 270.0, 14.0, 0.13, 510.0, 415.0),
            new AbyssRaceDef("shadow_lord", 385.0, 18.0, 0.14, 850.0, 600.0),
            new AbyssRaceDef("awakened_shadow_lord", 560.0, 24.0, 0.15, 1500.0, 875.0),
            new AbyssRaceDef("abyss_monarch", 810.0, 32.0, 0.165, 2800.0, 1265.0),
            new AbyssRaceDef("eminence_of_the_abyss", 1260.0, 45.0, 0.19, 5400.0, 2070.0),

            // ── PFAD 2 — Shadow Demon ──
            new AbyssRaceDef("low_shadow_demon", 48.0, 4.5, 0.105, 50.0, 60.0),
            new AbyssRaceDef("shadow_demon_peer", 76.0, 6.5, 0.11, 115.0, 100.0),
            new AbyssRaceDef("blood_shadow_demon", 124.0, 9.0, 0.115, 200.0, 160.0),
            new AbyssRaceDef("arcane_demon_guard", 190.0, 12.0, 0.12, 350.0, 240.0),
            new AbyssRaceDef("arch_demon_of_shadows", 285.0, 15.5, 0.13, 600.0, 360.0),
            new AbyssRaceDef("shadow_duke", 410.0, 20.0, 0.14, 1000.0, 520.0),
            new AbyssRaceDef("awakened_demon_king", 590.0, 26.5, 0.15, 1750.0, 760.0),
            new AbyssRaceDef("void_overlord", 855.0, 35.0, 0.165, 3250.0, 1100.0),
            new AbyssRaceDef("diablos_eminence", 1330.0, 50.0, 0.19, 6250.0, 1800.0),

            // ── PFAD 3 — Ancient Shadow Hero ──
            new AbyssRaceDef("human_apprentice", 55.0, 5.0, 0.11, 34.0, 55.0),
            new AbyssRaceDef("shadow_spellsword", 86.0, 7.0, 0.115, 76.0, 92.0),
            new AbyssRaceDef("shadow_blade", 140.0, 10.0, 0.12, 136.0, 148.0),
            new AbyssRaceDef("cult_breaker", 215.0, 13.0, 0.125, 240.0, 220.0),
            new AbyssRaceDef("master_of_garden", 320.0, 17.0, 0.135, 410.0, 330.0),
            new AbyssRaceDef("ancient_knight", 460.0, 21.5, 0.145, 680.0, 480.0),
            new AbyssRaceDef("true_hero_of_shadows", 660.0, 29.0, 0.155, 1190.0, 700.0),
            new AbyssRaceDef("light_shadow_monarch", 960.0, 38.5, 0.17, 2210.0, 1010.0),
            new AbyssRaceDef("sovereign_of_midnight", 1500.0, 54.0, 0.195, 4250.0, 1650.0),

            // ── PFAD 4 — Progenitor Blood-Shadow ──
            new AbyssRaceDef("vampire_spawn", 60.0, 4.5, 0.108, 38.0, 57.0),
            new AbyssRaceDef("blood_shadow", 96.0, 6.5, 0.113, 86.0, 95.0),
            new AbyssRaceDef("mist_walker", 156.0, 8.5, 0.118, 153.0, 152.0),
            new AbyssRaceDef("crimson_noble", 240.0, 11.5, 0.123, 270.0, 228.0),
            new AbyssRaceDef("night_stalker", 360.0, 15.0, 0.133, 460.0, 342.0),
            new AbyssRaceDef("pureblood_vampire", 516.0, 19.0, 0.143, 765.0, 494.0),
            new AbyssRaceDef("awakened_blood_lord", 744.0, 25.5, 0.153, 1340.0, 722.0),
            new AbyssRaceDef("monarch_of_the_red_moon", 1080.0, 34.0, 0.168, 2490.0, 1045.0),
            new AbyssRaceDef("progenitor_of_the_abyss", 1680.0, 48.0, 0.192, 5000.0, 1710.0),

            // ── GEHEIME RASSE ──
            new AbyssRaceDef("stylish_bandit_slayer", 2000.0, 55.0, 0.21, 7500.0, 2400.0)
    );

    // Alle Rassen beim DeferredRegister anmelden (laeuft beim Klassen-Laden).
    static {
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
