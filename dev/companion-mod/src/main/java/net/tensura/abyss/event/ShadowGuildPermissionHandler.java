package net.tensura.abyss.event;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

// ── ECHTE ManasCore/Architectury-API (per javap gegen die Pack-Jars verifiziert) ──
import io.github.manasmods.manascore.race.api.RaceEvents;
import dev.architectury.event.EventResult;

import java.util.List;
import java.util.Locale;

/**
 * Koppelt exklusive „Shadow"-Rassen an die Gilden-Erstellungs-Berechtigung
 * (Argonauts) via LuckPerms.
 *
 * <p><b>Mechanik (verifiziert):</b> Der Rassenwechsel ist KEIN NeoForge-
 * {@code @SubscribeEvent}, sondern das <em>Architectury</em>-Event
 * {@link RaceEvents#SET_RACE}. Es wird einmalig beim Mod-Start registriert
 * (siehe {@link #init()}, aufgerufen aus dem Mod-Konstruktor).
 *
 * <p>Der Handler {@code SetRaceEvent.set(...)} liefert:
 * <pre>{@code set(oldRace, entity, newRace, resetSkills, cancelled, message)}</pre>
 * Wir lesen die NEUE Rasse ({@code newRace.getRaceId()}) und die Entity.
 */
public final class ShadowGuildPermissionHandler {
    private ShadowGuildPermissionHandler() {}

    /** Persistenz-Flag — unter PERSISTED_NBT_TAG gespeichert, ueberlebt so den Tod. */
    private static final String FLAG = "hasUnlockedShadowGuild";

    /** Exklusive Shadow-Rassen, die die Gilden-Erstellung freischalten. */
    private static final List<String> SHADOW_RACES = List.of(
            "tensura_abyss:shadow_tier_1",
            "tensura_abyss:stylish_bandit_slayer"
            // ... weitere Shadow-Rassen hier ergaenzen
    );

    private static final String PERM = "argonauts.guild.create";

    /**
     * Registriert den SET_RACE-Listener. Einmalig aus dem Mod-Konstruktor
     * aufrufen: {@code ShadowGuildPermissionHandler.init();}
     */
    public static void init() {
        // 6 Parameter gemaess SetRaceEvent#set(...). Rueckgabe: EventResult.pass()
        // = wir beobachten nur, blockieren den Rassenwechsel nicht.
        RaceEvents.SET_RACE.register((oldRace, entity, newRace, resetSkills, cancelled, message) -> {
            // Nur serverseitig + nur fuer echte Spieler handeln.
            if (entity instanceof ServerPlayer player) {
                // >>> Falls die Freischaltung auf der FALSCHEN Rasse triggert:
                //     hier 'oldRace' statt 'newRace' verwenden (Parameter-Reihenfolge
                //     einmal im Spiel mit einem Log verifizieren). <<<
                ResourceLocation raceId = newRace.getRaceId();
                handleRaceChange(player, raceId);
            }
            return EventResult.pass();
        });
    }

    private static void handleRaceChange(ServerPlayer player, ResourceLocation raceId) {
        MinecraftServer server = player.getServer();
        if (server == null) return;   // sollte serverseitig nie null sein

        String id = raceId.toString().toLowerCase(Locale.ROOT);
        boolean isShadowRace = SHADOW_RACES.contains(id);
        boolean hasFlag = getFlag(player);

        if (isShadowRace && !hasFlag) {
            setFlag(player, true);
            runSilent(server, "lp user " + player.getGameProfile().getName()
                    + " permission set " + PERM + " true");
        } else if (!isShadowRace && hasFlag) {
            setFlag(player, false);
            runSilent(server, "lp user " + player.getGameProfile().getName()
                    + " permission set " + PERM + " false");
        }
    }

    /**
     * Fuehrt einen Befehl STILL (kein Chat-/Log-Feedback) mit Server-/OP-Rechten
     * aus — garantiert auf dem Server-Haupt-Thread, um Race-Conditions/Crashes
     * zu vermeiden.
     */
    private static void runSilent(MinecraftServer server, String command) {
        server.execute(() -> {
            CommandSourceStack source = server.createCommandSourceStack()  // Konsole = Level 4
                    .withPermission(4)
                    .withSuppressedOutput();                               // still
            server.getCommands().performPrefixedCommand(source, command);
        });
    }

    // ── Persistenz-Flag (survives death via PERSISTED_NBT_TAG) ──
    private static boolean getFlag(ServerPlayer player) {
        return player.getPersistentData()
                .getCompound(Player.PERSISTED_NBT_TAG)
                .getBoolean(FLAG);
    }

    private static void setFlag(ServerPlayer player, boolean value) {
        var root = player.getPersistentData();
        var persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putBoolean(FLAG, value);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
