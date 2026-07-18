package net.tensura.abyss.guild;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.tensura.abyss.bridge.TensuraBridge;
import net.tensura.abyss.registry.ModItems;

import java.util.UUID;

/**
 * High-Level-Operationen fuer Gilden. Erzwingt das GEHEIME Zugangs-Gate:
 * nur Spieler mit einer Tensura-Rasse (Possessed oder hoeher) koennen das
 * Gildensystem ueberhaupt nutzen.
 */
public final class GuildManager {
    private GuildManager() {}

    /** Zentrales Gate: ohne Tensura-Rasse ist das gesamte System unsichtbar/gesperrt. */
    public static boolean canUseGuildSystem(ServerPlayer player) {
        return TensuraBridge.hasTensuraRace(player);
    }

    public static boolean createGuild(ServerPlayer leader, String name) {
        if (!canUseGuildSystem(leader)) return false;
        MinecraftServer server = leader.getServer();
        GuildSavedData data = GuildSavedData.get(server);
        String key = name.toLowerCase();
        if (data.guilds.containsKey(key)) return false;
        if (data.playerToGuild.containsKey(leader.getUUID())) return false;

        Guild g = new Guild(name, leader.getUUID());
        int number = g.addMember(leader.getUUID(), GuildRank.LEADER);
        data.guilds.put(key, g);
        data.playerToGuild.put(leader.getUUID(), key);
        data.setDirty();

        giveSignatureRecord(leader, g, number);
        leader.sendSystemMessage(Component.literal("§5Shadow Garden: guild \"" + name +
                "\" founded. You are §lMember #" + number + "§r§5 (Leader)."));
        return true;
    }

    public static boolean joinGuild(ServerPlayer player, String name) {
        if (!canUseGuildSystem(player)) return false;
        MinecraftServer server = player.getServer();
        GuildSavedData data = GuildSavedData.get(server);
        Guild g = data.guilds.get(name.toLowerCase());
        if (g == null || g.isFull()) return false;
        if (data.playerToGuild.containsKey(player.getUUID())) return false;

        int number = g.addMember(player.getUUID(), GuildRank.MEMBER);
        data.playerToGuild.put(player.getUUID(), g.name.toLowerCase());
        data.setDirty();

        giveSignatureRecord(player, g, number);
        player.sendSystemMessage(Component.literal("§5You joined the guild \"" + g.name +
                "\" — §lMember #" + number + "§r§5, rank " + g.rank().name() + "."));
        return true;
    }

    public static boolean leaveGuild(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        GuildSavedData data = GuildSavedData.get(server);
        Guild g = data.guildOf(player.getUUID());
        if (g == null) return false;
        g.removeMember(player.getUUID());
        data.playerToGuild.remove(player.getUUID());
        if (g.members.isEmpty()) data.guilds.remove(g.name.toLowerCase());
        data.setDirty();
        player.sendSystemMessage(Component.literal("§7You left the guild."));
        return true;
    }

    /** Erstellt das signierte Gilden-Dokument mit Nummer + Abenteurer-Rang. */
    private static void giveSignatureRecord(ServerPlayer player, Guild g, int number) {
        ItemStack record = new ItemStack(ModItems.SIGNATURE_RECORD.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("guild", g.name);
        tag.putInt("member_number", number);
        tag.putString("rank", g.rank().name());
        record.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        record.set(DataComponents.CUSTOM_NAME,
                Component.literal("Signature Record — " + g.name + " #" + number));
        if (!player.getInventory().add(record)) {
            player.drop(record, false);
        }
    }

    /** Guild-EXP vergeben (z.B. bei erledigten Commissions) und Rang-Aufstieg melden. */
    public static void addGuildExp(MinecraftServer server, UUID player, int exp) {
        GuildSavedData data = GuildSavedData.get(server);
        Guild g = data.guildOf(player);
        if (g == null) return;
        AdventurerRank before = g.rank();
        g.guildExp += exp;
        data.setDirty();
        AdventurerRank after = g.rank();
        if (after != before) {
            ServerPlayer p = server.getPlayerList().getPlayer(player);
            if (p != null) {
                p.sendSystemMessage(Component.literal("§6Guild \"" + g.name +
                        "\" has advanced to adventurer rank §l" + after.name() + "§r§6!"));
            }
        }
    }
}
