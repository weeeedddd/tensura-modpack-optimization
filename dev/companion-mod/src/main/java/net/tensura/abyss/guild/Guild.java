package net.tensura.abyss.guild;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Eine Shadow-Garden-Gilde. Vollstaendig NBT-serialisierbar (World-Saved-Data).
 * Verwaltet Mitglieder, Raenge, eindeutige Mitgliedsnummern, Login-Zeitstempel
 * und die gemeinsame Guild-EXP (-> Abenteurer-Rang der Gilde).
 */
public class Guild {
    public String name;
    public UUID leader;
    public int memberLimit = 20;
    public int guildExp = 0;
    public int nextMemberNumber = 1;

    public final Map<UUID, GuildRank> members = new HashMap<>();
    public final Map<UUID, Integer> memberNumbers = new HashMap<>();
    public final Map<UUID, Long> lastOnline = new HashMap<>();

    public Guild(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
    }

    private Guild() {}

    public AdventurerRank rank() {
        return AdventurerRank.fromExp(guildExp);
    }

    public boolean isFull() {
        return members.size() >= memberLimit;
    }

    /** Fuegt ein Mitglied hinzu und vergibt die naechste fortlaufende Mitgliedsnummer. */
    public int addMember(UUID player, GuildRank rank) {
        members.put(player, rank);
        int number = nextMemberNumber++;
        memberNumbers.put(player, number);
        lastOnline.put(player, System.currentTimeMillis());
        return number;
    }

    public void removeMember(UUID player) {
        members.remove(player);
        memberNumbers.remove(player);
        lastOnline.remove(player);
    }

    // ── Serialisierung ──
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putUUID("leader", leader);
        tag.putInt("memberLimit", memberLimit);
        tag.putInt("guildExp", guildExp);
        tag.putInt("nextMemberNumber", nextMemberNumber);

        ListTag list = new ListTag();
        for (var e : members.entrySet()) {
            CompoundTag m = new CompoundTag();
            m.putUUID("id", e.getKey());
            m.putString("rank", e.getValue().name());
            m.putInt("number", memberNumbers.getOrDefault(e.getKey(), 0));
            m.putLong("lastOnline", lastOnline.getOrDefault(e.getKey(), 0L));
            list.add(m);
        }
        tag.put("members", list);
        return tag;
    }

    public static Guild load(CompoundTag tag) {
        Guild g = new Guild();
        g.name = tag.getString("name");
        g.leader = tag.getUUID("leader");
        g.memberLimit = tag.getInt("memberLimit");
        g.guildExp = tag.getInt("guildExp");
        g.nextMemberNumber = tag.getInt("nextMemberNumber");
        ListTag list = tag.getList("members", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag m = list.getCompound(i);
            UUID id = m.getUUID("id");
            g.members.put(id, GuildRank.valueOf(m.getString("rank")));
            g.memberNumbers.put(id, m.getInt("number"));
            g.lastOnline.put(id, m.getLong("lastOnline"));
        }
        return g;
    }
}
