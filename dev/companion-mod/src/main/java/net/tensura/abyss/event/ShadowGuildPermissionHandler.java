package net.tensura.abyss.event;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

// ══════════════════════════════════════════════════════════════════════════
//  CORE-MOD-API (ManasCore / Architectury) — an deine dekompilierten Jars
//  anpassen. Verifiziert per javap gegen manascore-race-neoforge-4.0.0.2:
//    RaceEvents           -> io.github.manasmods.manascore.race.api.RaceEvents
//    ManasRaceInstance    -> io.github.manasmods.manascore.race.api.ManasRaceInstance
//                            (Typ von oldRace/newRace; hier nur via Lambda-
//                             Inferenz genutzt -> Import optional)
//    EventResult          -> dev.architectury.event.EventResult
// ══════════════════════════════════════════════════════════════════════════
import io.github.manasmods.manascore.race.api.RaceEvents;
import dev.architectury.event.EventResult;

import java.util.Set;

/**
 * Koppelt exklusive „Shadow"-Rassen an die Argonauts-Gilden-Erstellungs-
 * Berechtigung (via LuckPerms). Reagiert auf das ManasCore-Rassenwechsel-Event
 * im Architectury-Stil ({@link RaceEvents#SET_RACE}).
 */
public final class ShadowGuildPermissionHandler {
    private ShadowGuildPermissionHandler() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Persistenz-Flag im NBT (unter PERSISTED_NBT_TAG -> ueberlebt den Tod). */
    private static final String FLAG = "hasUnlockedShadowGuild";

    /** LuckPerms-Node fuer die Gilden-Erstellung. */
    private static final String PERMISSION = "argonauts.guild.create";

    /** Ziel-Rassen, die die Gilden-Erstellung freischalten: voller Slime-Pfad + geheime Rasse. */
    private static final Set<ResourceLocation> SHADOW_RACES = Set.of(
            ResourceLocation.parse("tensura_abyss:shadow_slime"),          // Stufe 1
            ResourceLocation.parse("tensura_abyss:magicule_slime"),        // Stufe 2
            ResourceLocation.parse("tensura_abyss:abyss_slime"),           // Stufe 3
            ResourceLocation.parse("tensura_abyss:shadow_garden_guard"),   // Stufe 4
            ResourceLocation.parse("tensura_abyss:dark_slime_sovereign"),  // Stufe 5
            ResourceLocation.parse("tensura_abyss:shadow_lord"),           // Stufe 6
            ResourceLocation.parse("tensura_abyss:awakened_shadow_lord"),  // Stufe 7
            ResourceLocation.parse("tensura_abyss:abyss_monarch"),         // Stufe 8
            ResourceLocation.parse("tensura_abyss:eminence_of_the_abyss"), // Stufe 9
            ResourceLocation.parse("tensura_abyss:stylish_bandit_slayer")  // geheime Rasse
    );

    /** Einmalig beim Mod-Start aufrufen (aus dem Mod-Konstruktor). */
    public static void init() {
        // ┌──────────────────────────────────────────────────────────────────┐
        // │ WICHTIG: SetRaceEvent#set hat gegen die ECHTE Jar SECHS Parameter: │
        // │   (oldRace, entity, newRace, resetSkills, cancelled, message)      │
        // │ Ein 3-Parameter-Lambda kompiliert NICHT. Wir brauchen nur die      │
        // │ ersten drei — die restlichen bleiben ungenutzt.                    │
        // └──────────────────────────────────────────────────────────────────┘
        RaceEvents.SET_RACE.register((oldRace, entity, newRace, resetSkills, cancelled, message) -> {
            // Parameter 3 is the new race; parameter 5 is Changeable<Boolean>.
            if (cancelled.isPresent() && Boolean.TRUE.equals(cancelled.get())) return EventResult.pass();
            // 1) Nur echte Spieler auf der LOGISCHEN SERVER-Seite.
            if (entity instanceof Player player && !player.level().isClientSide()) {

                ResourceLocation oldId = (oldRace != null) ? oldRace.getRaceId() : null;
                ResourceLocation newId = (newRace != null) ? newRace.getRaceId() : null;

                // 4) Debug-Log zur Verifikation der Parameter-Reihenfolge.
                LOGGER.info("[Tensura Abyss] SET_RACE: player={} old={} new={}",
                        player.getGameProfile().getName(), oldId, newId);

                MinecraftServer server = player.getServer();
                if (server != null && newId != null && !newId.equals(oldId)) {
                    updateGuildPermission(server, player, newId);
                }
            }
            // 2) Nur beobachten, den Rassenwechsel NICHT blockieren.
            return EventResult.pass();
        });
    }

    private static void updateGuildPermission(MinecraftServer server, Player player, ResourceLocation raceId) {
        boolean isShadowRace = SHADOW_RACES.contains(raceId);
        boolean hasFlag = getFlag(player);
        String name = player.getGameProfile().getName();

        if (isShadowRace && !hasFlag) {
            // Freischalten
            setFlag(player, true);
            runSilent(server, "lp user " + name + " permission set " + PERMISSION + " true");
        } else if (!isShadowRace && hasFlag) {
            // Entziehen
            setFlag(player, false);
            runSilent(server, "lp user " + name + " permission set " + PERMISSION + " false");
        }
    }

    /**
     * Fuehrt einen Befehl STILL (kein Chat-/Log-Spam) mit OP-Level 4 aus —
     * garantiert auf dem Server-Haupt-Thread (verhindert Race-Conditions/Crashes).
     */
    private static void runSilent(MinecraftServer server, String command) {
        server.execute(() -> {
            CommandSourceStack source = server.createCommandSourceStack()  // Konsole = Level 4
                    .withPermission(4)
                    .withSuppressedOutput();
            server.getCommands().performPrefixedCommand(source, command);
        });
    }

    // ── Persistenz-Flag unter PERSISTED_NBT_TAG (survives death) ──
    private static boolean getFlag(Player player) {
        return player.getPersistentData()
                .getCompound(Player.PERSISTED_NBT_TAG)
                .getBoolean(FLAG);
    }

    private static void setFlag(Player player, boolean value) {
        var root = player.getPersistentData();
        var persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putBoolean(FLAG, value);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
