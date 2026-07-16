package net.tensura.abyss.guild;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Aktualisiert Online-/Offline-Status und lastOnline-Zeitstempel der Mitglieder. */
public class GuildEventHandler {

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        touch(event.getEntity() instanceof ServerPlayer sp ? sp : null);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        touch(event.getEntity() instanceof ServerPlayer sp ? sp : null);
    }

    private void touch(ServerPlayer player) {
        if (player == null) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        GuildSavedData data = GuildSavedData.get(server);
        Guild g = data.guildOf(player.getUUID());
        if (g != null) {
            g.lastOnline.put(player.getUUID(), System.currentTimeMillis());
            data.setDirty();
        }
    }
}
