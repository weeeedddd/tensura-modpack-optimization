package net.tensura.abyss.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.tensura.abyss.guild.Guild;
import net.tensura.abyss.guild.GuildSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Minimap-/Radar-Schnittstelle für Gildenmitglieder.
 *
 * ┌───────────────────────────────────────────────────────────────────────────┐
 * │ EHRLICH: Ein echtes, farbiges Icon auf Xaero's Minimap / JourneyMap braucht │
 * │ deren jeweilige API (Waypoint-/Radar-Provider). Diese Klasse liefert die    │
 * │ SERVER-SEITIGEN Daten (Positionen der Online-Gildenmitglieder) sauber auf;  │
 * │ die Anbindung an das konkrete Minimap-Mod ist der dokumentierte Endpunkt.   │
 * └───────────────────────────────────────────────────────────────────────────┘
 *
 * Integrations-Optionen (eine waehlen, gegen die installierte Minimap-Mod bauen):
 *  - JourneyMap: net.jankdev... bzw. journeymap.api.v2 ClientAPI -> addWaypoint()
 *    pro Mitglied, Farbe = Gildenfarbe, Update im ClientTick.
 *  - Xaero's Minimap: xaero.common.minimap ... bzw. das offizielle "Xaero Minimap
 *    API"-Addon; Radar-Provider registrieren, der {@link #onlineMembers} abfragt.
 *  - Ohne Zusatz-Mod: /shadow radar (in ShadowCommands) listet Positionen im Chat.
 */
public final class MinimapIntegration {
    private MinimapIntegration() {}

    public record MemberBlip(UUID id, String name, String dimension, int x, int y, int z) {}

    /** Alle Online-Mitglieder der Gilde des Spielers (fuer den Client-Renderer). */
    public static List<MemberBlip> onlineMembers(ServerPlayer viewer) {
        List<MemberBlip> blips = new ArrayList<>();
        MinecraftServer server = viewer.getServer();
        if (server == null) return blips;
        GuildSavedData data = GuildSavedData.get(server);
        Guild g = data.guildOf(viewer.getUUID());
        if (g == null) return blips;
        for (UUID m : g.members.keySet()) {
            ServerPlayer mp = server.getPlayerList().getPlayer(m);
            if (mp != null && mp != viewer) {
                blips.add(new MemberBlip(m, mp.getName().getString(),
                        mp.level().dimension().location().toString(),
                        mp.getBlockX(), mp.getBlockY(), mp.getBlockZ()));
            }
        }
        return blips;
    }

    // Naechster Schritt (mit gewaehlter Minimap-API):
    //  1) Custom-Payload registrieren (PayloadRegistrar in einem
    //     RegisterPayloadHandlersEvent), die eine List<MemberBlip> zum Client sendet.
    //  2) Server sendet die Liste periodisch (z.B. alle 20 Ticks) an jeden Viewer.
    //  3) Client uebergibt die Blips an die Waypoint-/Radar-API der Minimap-Mod.
}
