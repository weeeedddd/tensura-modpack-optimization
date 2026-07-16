package de.zisser.tensuraabyss;

import de.zisser.tensuraabyss.registry.AbyssRaces;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Tensura Abyss Companion — Einstiegspunkt (NeoForge 1.21.1, Java 21).
 *
 * <p>Zweck (Weg B): registriert die Shadow-Garden-Custom-Rassen NATIV in der
 * Race-Registry von Tensura: Reincarnated. Dadurch tauchen sie im originalen
 * Rassenauswahl-/Evolutionsmenue auf und werden von Tensura: Ascension erkannt.
 *
 * <p>Das eigentliche Gating (Boss-Kills, Dimension, Items, Coins) bleibt in
 * KubeJS (overrides/kubejs/server_scripts/); diese Mod liefert nur die
 * Rassen-Definitionen + Anzeige-Namen.
 */
@Mod(TensuraAbyssCompanion.MOD_ID)
public final class TensuraAbyssCompanion {

    /** Muss mit gradle.properties (mod_id) und der neoforge.mods.toml uebereinstimmen. */
    public static final String MOD_ID = "tensura_abyss";

    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * NeoForge injiziert den Mod-Event-Bus in den Konstruktor.
     * Hier haengen wir unsere DeferredRegister ein.
     */
    public TensuraAbyssCompanion(IEventBus modEventBus) {
        LOGGER.info("[Tensura Abyss] Companion startet — registriere Custom-Rassen...");

        // Rassen-Registry an den Mod-Bus haengen (siehe AbyssRaces).
        AbyssRaces.register(modEventBus);

        LOGGER.info("[Tensura Abyss] {} Rassen zur Registrierung angemeldet.",
                AbyssRaces.registeredCount());
    }
}
