package net.tensura.abyss.guild;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World-Saved-Data der Shadow-Garden-Gilden. Wird auf der Overworld gespeichert
 * (data/tensura_abyss_guilds.dat) und gilt server-weit.
 */
public class GuildSavedData extends SavedData {
    private static final String NAME = "tensura_abyss_guilds";

    public final Map<String, Guild> guilds = new HashMap<>();      // name (lowercase) -> Guild
    public final Map<UUID, String> playerToGuild = new HashMap<>(); // player -> guild name

    public static GuildSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) throw new IllegalStateException("Overworld not loaded");
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(GuildSavedData::new, GuildSavedData::load),
                NAME);
    }

    public Guild guildOf(UUID player) {
        String name = playerToGuild.get(player);
        return name == null ? null : guilds.get(name);
    }

    public void indexMembers(Guild g) {
        String key = g.name.toLowerCase();
        for (UUID member : g.members.keySet()) {
            playerToGuild.put(member, key);
        }
    }

    // ── Serialisierung ──
    public static GuildSavedData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        GuildSavedData data = new GuildSavedData();
        ListTag list = tag.getList("guilds", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            Guild g = Guild.load(list.getCompound(i));
            data.guilds.put(g.name.toLowerCase(), g);
            data.indexMembers(g);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Guild g : guilds.values()) {
            list.add(g.save());
        }
        tag.put("guilds", list);
        return tag;
    }
}
