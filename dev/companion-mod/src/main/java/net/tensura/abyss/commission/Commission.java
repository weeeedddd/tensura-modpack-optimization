package net.tensura.abyss.commission;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Ein einzelner Auftrag. NBT-serialisierbar. */
public class Commission {
    public UUID id;
    public CommissionType type;
    public String title;
    public String targetId;   // Item- oder Entity-ID (je nach Typ)
    public int amount;        // benoetigte Menge / Kills
    public int rewardExp;     // Guild-EXP-Belohnung
    public String rewardItem; // optional: Item-ID der Belohnung
    public int rewardCount;
    public long expiresAt;     // epoch millis
    public int coordX, coordZ; // fuer COORDINATE_SUBJUGATION
    public boolean partyOnly;

    public Commission() {}

    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putUUID("id", id);
        t.putString("type", type.name());
        t.putString("title", title);
        t.putString("target", targetId == null ? "" : targetId);
        t.putInt("amount", amount);
        t.putInt("rewardExp", rewardExp);
        t.putString("rewardItem", rewardItem == null ? "" : rewardItem);
        t.putInt("rewardCount", rewardCount);
        t.putLong("expiresAt", expiresAt);
        t.putInt("coordX", coordX);
        t.putInt("coordZ", coordZ);
        t.putBoolean("partyOnly", partyOnly);
        return t;
    }

    public static Commission load(CompoundTag t) {
        Commission c = new Commission();
        c.id = t.getUUID("id");
        c.type = CommissionType.valueOf(t.getString("type"));
        c.title = t.getString("title");
        c.targetId = t.getString("target");
        c.amount = t.getInt("amount");
        c.rewardExp = t.getInt("rewardExp");
        c.rewardItem = t.getString("rewardItem");
        c.rewardCount = t.getInt("rewardCount");
        c.expiresAt = t.getLong("expiresAt");
        c.coordX = t.getInt("coordX");
        c.coordZ = t.getInt("coordZ");
        c.partyOnly = t.getBoolean("partyOnly");
        return c;
    }
}
