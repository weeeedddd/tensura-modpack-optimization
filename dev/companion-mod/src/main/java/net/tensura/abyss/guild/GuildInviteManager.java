package net.tensura.abyss.guild;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Verwaltet offene Gilden-Einladungen (Session), inkl. der Geheimbotschaft. */
public final class GuildInviteManager {
    private GuildInviteManager() {}

    public record Pending(String guildName, String note, String fromName) {}

    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    public static void invite(UUID target, String guildName, String note, String fromName) {
        PENDING.put(target, new Pending(guildName, note, fromName));
    }

    public static Pending pending(UUID target) {
        return PENDING.get(target);
    }

    /** Nimmt die offene Einladung an -> tritt der Gilde bei. */
    public static boolean accept(ServerPlayer player) {
        Pending p = PENDING.remove(player.getUUID());
        if (p == null) return false;
        return GuildManager.joinGuild(player, p.guildName());
    }

    public static void clear(UUID target) {
        PENDING.remove(target);
    }
}
