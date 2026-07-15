package net.tensura.abyss.guild;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Verwaltet Partys und den Party-Chat (Session-Zustand, statisch). */
public final class PartyManager {
    private PartyManager() {}

    private static final Map<UUID, Party> PARTY_OF = new HashMap<>();

    public static Party partyOf(UUID player) {
        return PARTY_OF.get(player);
    }

    public static Party create(ServerPlayer leader) {
        if (PARTY_OF.containsKey(leader.getUUID())) return PARTY_OF.get(leader.getUUID());
        Party p = new Party(leader.getUUID());
        PARTY_OF.put(leader.getUUID(), p);
        return p;
    }

    public static boolean join(ServerPlayer player, Party party) {
        if (PARTY_OF.containsKey(player.getUUID())) return false;
        party.members.add(player.getUUID());
        PARTY_OF.put(player.getUUID(), party);
        return true;
    }

    public static void leave(ServerPlayer player) {
        Party p = PARTY_OF.remove(player.getUUID());
        if (p == null) return;
        p.members.remove(player.getUUID());
        if (p.leader.equals(player.getUUID()) || p.members.isEmpty()) {
            // Party aufloesen
            for (UUID m : p.members) PARTY_OF.remove(m);
            p.members.clear();
        }
    }

    /** Sendet eine Nachricht farbig an alle Online-Mitglieder der Party. */
    public static void chat(MinecraftServer server, ServerPlayer sender, String message) {
        Party p = PARTY_OF.get(sender.getUUID());
        if (p == null) {
            sender.sendSystemMessage(Component.literal("§7Du bist in keiner Party. /shadow party create"));
            return;
        }
        Component line = Component.literal("§d[Party] §f" + sender.getName().getString() + "§7: §d" + message);
        for (UUID m : p.members) {
            ServerPlayer mp = server.getPlayerList().getPlayer(m);
            if (mp != null) mp.sendSystemMessage(line);
        }
    }
}
