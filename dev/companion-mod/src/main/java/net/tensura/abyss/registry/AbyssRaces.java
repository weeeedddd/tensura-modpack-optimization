package net.tensura.abyss.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tensura.abyss.TensuraAbyss;

import java.util.function.Supplier;

// ╔═══════════════════════════════════════════════════════════════════════════╗
// ║  >>> EINZIGE STELLE, DIE GEGEN DIE ECHTE TENSURA-API MUSS <<<              ║
// ║                                                                           ║
// ║  Verifiziere 3 Dinge gegen tensura-neoforge-2.0.1.1 (Pack) — z.B. Jar     ║
// ║  dekompilieren (Vineflower) oder die 1.21.1-Beispiel-Addons ansehen:      ║
// ║    github.com/BanditHelps/TensuraAddonExample                             ║
// ║    github.com/vel-mc/TensuraAddonExample4554                              ║
// ║                                                                           ║
// ║  1) IMPORT der Race-Klasse:                                               ║
// ║       Annahme:        net.tensura.api.race.Race                           ║
// ║       Wahrscheinlich: com.github.manasmods.tensura.race.Race              ║
// ║       -> Zeile TODO(API-IMPORT) aktivieren, dann Object->Race ersetzen.   ║
// ║  2) REGISTRY-KEY: Annahme "tensura:races" (TENSURA_RACE_REGISTRY unten).   ║
// ║  3) BUILDER/KONSTRUKTOR: siehe Helfer abyssRace(...).                      ║
// ╚═══════════════════════════════════════════════════════════════════════════╝

// TODO(API-IMPORT): einen der beiden aktivieren, sobald verifiziert:
// import net.tensura.api.race.Race;
// import com.github.manasmods.tensura.race.Race;

/**
 * Registriert die 37 Tensura-Abyss-Custom-Rassen (4 Pfade x 9 Stufen + geheime
 * Rasse) in Tensuras Race-Registry, damit sie im originalen Evolutionsmenue
 * auftauchen und von Tensura: Ascension erkannt werden.
 *
 * <p><b>Architektur:</b> Alle STAT-Werte (real & final, konsistent mit
 * config/tensura/ascension-races.toml) stehen als {@link AbyssRaceDef}-Eintraege.
 * Nur der letzte Schritt — aus den Werten ein echtes Tensura-{@code Race}-Objekt
 * bauen — ist der API-Platzhalter im Helfer {@link #abyssRace(AbyssRaceDef)}.
 *
 * <p>Sobald der Race-Import steht: das generische {@code Object} in {@link #RACES},
 * den {@code Supplier}-Feldern und {@link #abyssRace} durch {@code Race} ersetzen.
 *
 * <p>Die 7 Legacy-Rassen (possessed/seven_shadows/shadow) aus der TOML werden hier
 * bewusst NICHT registriert — sie gehoerten zum abgeloesten virtuellen System.
 */
public final class AbyssRaces {
    private AbyssRaces() {}

    // ── Registry-Key von Tensura (Annahme: "tensura:races") ──
    // TODO(API-KEY): Namespace/Pfad gegen die echte Registry pruefen.
    @SuppressWarnings("unchecked")
    private static final ResourceKey<Registry<Object>> TENSURA_RACE_REGISTRY =
            (ResourceKey<Registry<Object>>) (Object) ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath("tensura", "races"));

    // ── DeferredRegister auf Tensuras Race-Registry ──
    // Sobald der Race-Import steht: DeferredRegister<Race> statt <Object>.
    private static final DeferredRegister<Object> RACES =
            DeferredRegister.create(TENSURA_RACE_REGISTRY, TensuraAbyss.MOD_ID);

    private static int count = 0;

    /** Kompakte, mod-unabhaengige Rassen-Definition (nur Zahlen/Flags). */
    public record AbyssRaceDef(
            String id,
            double spiritualHealth,
            double physicalHealth,
            double movementSpeed,
            double baseAttack,
            boolean isAwakened     // True-Demon-Lord / Awakened-Zustand?
    ) {}

    // ═══════════════════════════════════════════════════════════════════════
    //  STAT-DEFINITIONEN (real & final — Werte aus ascension-races.toml)
    //  37 Rassen: 4 Pfade x 9 Stufen + geheime Rasse.
    // ═══════════════════════════════════════════════════════════════════════

    // ── PFAD 1 — Shadow Slime ──
    public static final Supplier<Object> SHADOW_SLIME = registerRace(
            new AbyssRaceDef("shadow_slime", 70.0, 45.0, 0.105, 4.0, false));
    public static final Supplier<Object> MAGICULE_SLIME = registerRace(
            new AbyssRaceDef("magicule_slime", 115.0, 72.0, 0.11, 6.0, false));
    public static final Supplier<Object> ABYSS_SLIME = registerRace(
            new AbyssRaceDef("abyss_slime", 185.0, 117.0, 0.115, 8.0, false));
    public static final Supplier<Object> SHADOW_GARDEN_GUARD = registerRace(
            new AbyssRaceDef("shadow_garden_guard", 275.0, 180.0, 0.12, 11.0, false));
    public static final Supplier<Object> DARK_SLIME_SOVEREIGN = registerRace(
            new AbyssRaceDef("dark_slime_sovereign", 415.0, 270.0, 0.13, 14.0, false));
    public static final Supplier<Object> SHADOW_LORD = registerRace(
            new AbyssRaceDef("shadow_lord", 600.0, 385.0, 0.14, 18.0, false));
    public static final Supplier<Object> AWAKENED_SHADOW_LORD = registerRace(
            new AbyssRaceDef("awakened_shadow_lord", 875.0, 560.0, 0.15, 24.0, true));
    public static final Supplier<Object> ABYSS_MONARCH = registerRace(
            new AbyssRaceDef("abyss_monarch", 1265.0, 810.0, 0.165, 32.0, true));
    public static final Supplier<Object> EMINENCE_OF_THE_ABYSS = registerRace(
            new AbyssRaceDef("eminence_of_the_abyss", 2070.0, 1260.0, 0.19, 45.0, true));

    // ── PFAD 2 — Shadow Demon ──
    public static final Supplier<Object> LOW_SHADOW_DEMON = registerRace(
            new AbyssRaceDef("low_shadow_demon", 60.0, 48.0, 0.105, 4.5, false));
    public static final Supplier<Object> SHADOW_DEMON_PEER = registerRace(
            new AbyssRaceDef("shadow_demon_peer", 100.0, 76.0, 0.11, 6.5, false));
    public static final Supplier<Object> BLOOD_SHADOW_DEMON = registerRace(
            new AbyssRaceDef("blood_shadow_demon", 160.0, 124.0, 0.115, 9.0, false));
    public static final Supplier<Object> ARCANE_DEMON_GUARD = registerRace(
            new AbyssRaceDef("arcane_demon_guard", 240.0, 190.0, 0.12, 12.0, false));
    public static final Supplier<Object> ARCH_DEMON_OF_SHADOWS = registerRace(
            new AbyssRaceDef("arch_demon_of_shadows", 360.0, 285.0, 0.13, 15.5, false));
    public static final Supplier<Object> SHADOW_DUKE = registerRace(
            new AbyssRaceDef("shadow_duke", 520.0, 410.0, 0.14, 20.0, false));
    public static final Supplier<Object> AWAKENED_DEMON_KING = registerRace(
            new AbyssRaceDef("awakened_demon_king", 760.0, 590.0, 0.15, 26.5, true));
    public static final Supplier<Object> VOID_OVERLORD = registerRace(
            new AbyssRaceDef("void_overlord", 1100.0, 855.0, 0.165, 35.0, true));
    public static final Supplier<Object> DIABLOS_EMINENCE = registerRace(
            new AbyssRaceDef("diablos_eminence", 1800.0, 1330.0, 0.19, 50.0, true));

    // ── PFAD 3 — Ancient Shadow Hero ──
    public static final Supplier<Object> HUMAN_APPRENTICE = registerRace(
            new AbyssRaceDef("human_apprentice", 55.0, 55.0, 0.11, 5.0, false));
    public static final Supplier<Object> SHADOW_SPELLSWORD = registerRace(
            new AbyssRaceDef("shadow_spellsword", 92.0, 86.0, 0.115, 7.0, false));
    public static final Supplier<Object> SHADOW_BLADE = registerRace(
            new AbyssRaceDef("shadow_blade", 148.0, 140.0, 0.12, 10.0, false));
    public static final Supplier<Object> CULT_BREAKER = registerRace(
            new AbyssRaceDef("cult_breaker", 220.0, 215.0, 0.125, 13.0, false));
    public static final Supplier<Object> MASTER_OF_GARDEN = registerRace(
            new AbyssRaceDef("master_of_garden", 330.0, 320.0, 0.135, 17.0, false));
    public static final Supplier<Object> ANCIENT_KNIGHT = registerRace(
            new AbyssRaceDef("ancient_knight", 480.0, 460.0, 0.145, 21.5, false));
    public static final Supplier<Object> TRUE_HERO_OF_SHADOWS = registerRace(
            new AbyssRaceDef("true_hero_of_shadows", 700.0, 660.0, 0.155, 29.0, true));
    public static final Supplier<Object> LIGHT_SHADOW_MONARCH = registerRace(
            new AbyssRaceDef("light_shadow_monarch", 1010.0, 960.0, 0.17, 38.5, true));
    public static final Supplier<Object> SOVEREIGN_OF_MIDNIGHT = registerRace(
            new AbyssRaceDef("sovereign_of_midnight", 1650.0, 1500.0, 0.195, 54.0, true));

    // ── PFAD 4 — Progenitor Blood-Shadow ──
    public static final Supplier<Object> VAMPIRE_SPAWN = registerRace(
            new AbyssRaceDef("vampire_spawn", 57.0, 60.0, 0.108, 4.5, false));
    public static final Supplier<Object> BLOOD_SHADOW = registerRace(
            new AbyssRaceDef("blood_shadow", 95.0, 96.0, 0.113, 6.5, false));
    public static final Supplier<Object> MIST_WALKER = registerRace(
            new AbyssRaceDef("mist_walker", 152.0, 156.0, 0.118, 8.5, false));
    public static final Supplier<Object> CRIMSON_NOBLE = registerRace(
            new AbyssRaceDef("crimson_noble", 228.0, 240.0, 0.123, 11.5, false));
    public static final Supplier<Object> NIGHT_STALKER = registerRace(
            new AbyssRaceDef("night_stalker", 342.0, 360.0, 0.133, 15.0, false));
    public static final Supplier<Object> PUREBLOOD_VAMPIRE = registerRace(
            new AbyssRaceDef("pureblood_vampire", 494.0, 516.0, 0.143, 19.0, false));
    public static final Supplier<Object> AWAKENED_BLOOD_LORD = registerRace(
            new AbyssRaceDef("awakened_blood_lord", 722.0, 744.0, 0.153, 25.5, true));
    public static final Supplier<Object> MONARCH_OF_THE_RED_MOON = registerRace(
            new AbyssRaceDef("monarch_of_the_red_moon", 1045.0, 1080.0, 0.168, 34.0, true));
    public static final Supplier<Object> PROGENITOR_OF_THE_ABYSS = registerRace(
            new AbyssRaceDef("progenitor_of_the_abyss", 1710.0, 1680.0, 0.192, 48.0, true));

    // ── GEHEIME RASSE ──
    public static final Supplier<Object> STYLISH_BANDIT_SLAYER = registerRace(
            new AbyssRaceDef("stylish_bandit_slayer", 2400.0, 2000.0, 0.21, 55.0, true));
    // ═══════════════════════════════════════════════════════════════════════
    //  REGISTRIERUNGS-MECHANIK
    // ═══════════════════════════════════════════════════════════════════════

    /** Meldet eine Rasse beim DeferredRegister an und liefert ihren Supplier. */
    private static Supplier<Object> registerRace(AbyssRaceDef def) {
        count++;
        return RACES.register(def.id(), () -> abyssRace(def));
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │  API-PLATZHALTER: aus den (realen) Stat-Werten ein echtes Tensura-    │
     * │  Race-Objekt bauen. Body durch den echten Builder/Konstruktor der    │
     * │  Tensura-API ersetzen (Methodennamen verifizieren!).                 │
     * └─────────────────────────────────────────────────────────────────────┘
     *
     * Typisches Muster (Pseudocode):
     * <pre>{@code
     *   return Race.builder()
     *       .spiritualHealth(def.spiritualHealth())
     *       .health(def.physicalHealth())
     *       .movementSpeed(def.movementSpeed())
     *       .baseAttackDamage(def.baseAttack())
     *       .awakened(def.isAwakened())
     *       .build();
     * }</pre>
     */
    private static Object abyssRace(AbyssRaceDef def) {
        // TODO(API-BUILDER): durch echten Tensura-Race-Aufbau ersetzen.
        throw new UnsupportedOperationException(
                "abyssRace(): Tensura-Race-Builder noch nicht verdrahtet fuer '"
                        + def.id() + "' — siehe Kommentar oben.");
    }

    /** Am Mod-Event-Bus registrieren (aus dem Mod-Konstruktor aufgerufen). */
    public static void register(IEventBus modEventBus) {
        RACES.register(modEventBus);
    }

    /** Anzahl der angemeldeten Rassen (fuers Log). */
    public static int registeredCount() {
        return count;
    }
}
