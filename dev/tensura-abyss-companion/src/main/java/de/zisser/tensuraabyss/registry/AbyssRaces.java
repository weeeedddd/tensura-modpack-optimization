package de.zisser.tensuraabyss.registry;

import de.zisser.tensuraabyss.TensuraAbyssCompanion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

// ╔═══════════════════════════════════════════════════════════════════════════╗
// ║  >>> HIER DIE EINZIGE STELLE, DIE GEGEN DIE ECHTE TENSURA-API MUSS <<<     ║
// ║                                                                           ║
// ║  Die folgenden 3 Dinge muessen gegen tensura-neoforge-2.0.1.1 (Pack)      ║
// ║  verifiziert werden. Finde sie so:                                        ║
// ║    - Jar dekompilieren (IDE / Vineflower) und nach "Race" + Registry      ║
// ║      suchen, ODER                                                          ║
// ║    - die 1.21.1-Beispiel-Addons ansehen:                                   ║
// ║        github.com/BanditHelps/TensuraAddonExample                          ║
// ║        github.com/vel-mc/TensuraAddonExample4554                           ║
// ║      (dort ist die Race-Registrierung 1:1 vorgemacht)                      ║
// ║                                                                           ║
// ║  1) IMPORT der Race-Klasse:                                               ║
// ║       Annahme (dein Vorschlag):  net.tensura.api.race.Race                 ║
// ║       Wahrscheinlicher (JitPack-Gruppe com.github.manasmods):             ║
// ║                                   com.github.manasmods.tensura.race.Race   ║
// ║       -> import unten anpassen (Zeile mit TODO(API-IMPORT)).               ║
// ║                                                                           ║
// ║  2) REGISTRY-KEY:  Annahme "tensura:races"  (siehe TENSURA_RACE_REGISTRY). ║
// ║                                                                           ║
// ║  3) BUILDER/KONSTRUKTOR der Race:  siehe Helfer abyssRace(...) unten.      ║
// ╚═══════════════════════════════════════════════════════════════════════════╝

// TODO(API-IMPORT): einen der beiden Imports aktivieren, sobald verifiziert:
// import net.tensura.api.race.Race;
// import com.github.manasmods.tensura.race.Race;

/**
 * Registriert die Tensura-Abyss-Custom-Rassen in Tensuras Race-Registry.
 *
 * <p><b>Architektur-Trick:</b> Alle STAT-Werte (real & final) stehen unten als
 * saubere {@link AbyssRaceDef}-Eintraege. Nur der letzte Schritt — aus diesen
 * Werten ein echtes Tensura-{@code Race}-Objekt bauen — ist der API-Platzhalter
 * im Helfer {@link #abyssRace(AbyssRaceDef)}. So musst du bei einem Tensura-
 * Update nur eine einzige Methode anfassen.
 *
 * <p>Sobald der Race-Import gesetzt ist, ersetze das generische {@code Object}
 * in {@link #RACES} und {@link #abyssRace} durch {@code Race}.
 */
public final class AbyssRaces {
    private AbyssRaces() {}

    // ── Registry-Key von Tensura (Annahme: "tensura:races") ──
    // TODO(API-KEY): Namespace/Pfad gegen die echte Registry pruefen.
    @SuppressWarnings("unchecked")
    private static final ResourceKey<net.minecraft.core.Registry<Object>> TENSURA_RACE_REGISTRY =
            (ResourceKey<net.minecraft.core.Registry<Object>>) (Object) ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath("tensura", "races"));

    // ── DeferredRegister auf Tensuras Race-Registry ──
    // Sobald der Race-Import steht: DeferredRegister<Race> statt <Object>.
    private static final DeferredRegister<Object> RACES =
            DeferredRegister.create(TENSURA_RACE_REGISTRY, TensuraAbyssCompanion.MOD_ID);

    private static int count = 0;

    // ═══════════════════════════════════════════════════════════════════════
    //  STAT-DEFINITIONEN  (real & final — hier NICHTS raten, nur Balancing)
    //  Werte sind konsistent mit config/tensura/ascension-races.toml.
    // ═══════════════════════════════════════════════════════════════════════

    /** Kompakte, mod-unabhaengige Rassen-Definition (nur Zahlen/Flags). */
    public record AbyssRaceDef(
            String id,             // Registry-Pfad, z.B. "shadow_slime"
            double spiritualHealth,
            double physicalHealth,
            double movementSpeed,
            double baseAttack,
            boolean isAwakened     // True-Demon-Lord / Awakened-Zustand?
    ) {}

    // ── PFAD 1: Shadow Slime (Stufe 1 & Stufe 9 als Beispiel) ──
    public static final Supplier<Object> SHADOW_SLIME = registerRace(
            new AbyssRaceDef("shadow_slime", 70.0, 45.0, 0.105, 4.0, false));

    public static final Supplier<Object> EMINENCE_OF_THE_ABYSS = registerRace(
            new AbyssRaceDef("eminence_of_the_abyss", 2070.0, 1260.0, 0.19, 45.0, true));

    // ── PFAD 2: Shadow Demon (Stufe 1 als Beispiel) ──
    public static final Supplier<Object> LOW_SHADOW_DEMON = registerRace(
            new AbyssRaceDef("low_shadow_demon", 60.0, 48.0, 0.105, 4.5, false));

    // ── PFAD 3: Ancient Shadow Hero (Stufe 1 als Beispiel) ──
    public static final Supplier<Object> HUMAN_APPRENTICE = registerRace(
            new AbyssRaceDef("human_apprentice", 55.0, 55.0, 0.11, 5.0, false));

    // ── PFAD 4: Progenitor Blood-Shadow (Stufe 1 als Beispiel) ──
    public static final Supplier<Object> VAMPIRE_SPAWN = registerRace(
            new AbyssRaceDef("vampire_spawn", 57.0, 60.0, 0.108, 4.5, false));

    // ── GEHEIME RASSE: Stylish Bandit Slayer (extrem hohe Werte) ──
    public static final Supplier<Object> STYLISH_BANDIT_SLAYER = registerRace(
            new AbyssRaceDef("stylish_bandit_slayer", 2400.0, 2000.0, 0.21, 55.0, true));

    // >>> Die restlichen 38 Stufen nach genau diesem Muster ergaenzen. <<<

    // ═══════════════════════════════════════════════════════════════════════
    //  REGISTRIERUNGS-MECHANIK
    // ═══════════════════════════════════════════════════════════════════════

    /** Meldet eine Rasse beim DeferredRegister an und liefert ihren Supplier. */
    private static Supplier<Object> registerRace(AbyssRaceDef def) {
        count++;
        // DeferredRegister.register gibt einen DeferredHolder (= Supplier) zurueck.
        return RACES.register(def.id(), () -> abyssRace(def));
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │  API-PLATZHALTER: hier aus den (realen) Stat-Werten ein echtes        │
     * │  Tensura-Race-Objekt bauen. Ersetze den Body durch den echten        │
     * │  Builder/Konstruktor der Tensura-API.                                 │
     * └─────────────────────────────────────────────────────────────────────┘
     *
     * Typisches Tensura-Muster (Pseudocode — Methodennamen verifizieren!):
     * <pre>{@code
     *   return new Race(
     *           def.spiritualHealth(),
     *           def.physicalHealth(),
     *           def.movementSpeed(),
     *           def.baseAttack(),
     *           List.of(),            // Start-Skills (optional)
     *           def.isAwakened()      // ggf. eigenes Feld/Builder-Flag
     *   );
     *   // oder Builder-Stil:
     *   // return Race.builder()
     *   //     .spiritualHealth(def.spiritualHealth())
     *   //     .health(def.physicalHealth())
     *   //     .movementSpeed(def.movementSpeed())
     *   //     .baseAttackDamage(def.baseAttack())
     *   //     .awakened(def.isAwakened())
     *   //     .build();
     * }</pre>
     *
     * @return ein Tensura-{@code Race} (aktuell Object-Platzhalter)
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
