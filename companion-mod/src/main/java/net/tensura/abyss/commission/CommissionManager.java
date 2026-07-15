package net.tensura.abyss.commission;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generiert und verwaltet die aktuell ausgeschriebenen Auftraege ("Commission
 * Board"). Bewusst datengetrieben — die konkrete Fortschritts-/Abschluss-Pruefung
 * laeuft ueber KubeJS (Item-/Kill-Zaehlung), das {@link #complete} aufruft.
 */
public final class CommissionManager {
    private CommissionManager() {}

    private static final List<Commission> BOARD = new ArrayList<>();
    private static final long DURATION = 1000L * 60 * 60 * 24; // 24h

    // Pools fuer die Zufallsgenerierung
    private static final String[] GATHER_ITEMS = {
            "tensura_abyss:dark_slime", "minecraft:echo_shard", "minecraft:amethyst_shard"
    };
    private static final String[] CRAFT_ITEMS = {
            "tensura_abyss:slime_suit_chestplate", "tensura_abyss:dark_aether"
    };
    private static final String[] SUBJUGATION_MOBS = {
            "minecraft:pillager", "minecraft:vindicator", "minecraft:evoker"
    };

    public static List<Commission> board() {
        BOARD.removeIf(Commission::isExpired);
        return BOARD;
    }

    public static Commission byId(UUID id) {
        for (Commission c : BOARD) if (c.id.equals(id)) return c;
        return null;
    }

    /** Fuellt das Board bis zur Zielgroesse mit frischen Auftraegen auf. */
    public static void refill(RandomSource rng, int target) {
        board(); // abgelaufene entfernen
        while (BOARD.size() < target) {
            BOARD.add(generate(rng));
        }
    }

    public static Commission generate(RandomSource rng) {
        CommissionType[] types = CommissionType.values();
        CommissionType type = types[rng.nextInt(types.length)];
        Commission c = new Commission();
        c.id = UUID.randomUUID();
        c.type = type;
        c.expiresAt = System.currentTimeMillis() + DURATION;

        switch (type) {
            case GATHERING -> {
                c.targetId = pick(rng, GATHER_ITEMS);
                c.amount = 8 + rng.nextInt(25);
                c.rewardExp = 200 + c.amount * 10;
                c.title = "Gathering: " + c.amount + "x " + shortName(c.targetId);
            }
            case CRAFTING -> {
                c.targetId = pick(rng, CRAFT_ITEMS);
                c.amount = 1 + rng.nextInt(3);
                c.rewardExp = 400 + c.amount * 150;
                c.title = "Crafting: " + c.amount + "x " + shortName(c.targetId);
            }
            case SUBJUGATION -> {
                c.targetId = pick(rng, SUBJUGATION_MOBS);
                c.amount = 5 + rng.nextInt(15);
                c.rewardExp = 350 + c.amount * 20;
                c.title = "Subjugation: " + c.amount + "x " + shortName(c.targetId);
            }
            case COORDINATE_SUBJUGATION -> {
                c.coordX = (rng.nextInt(16000) - 8000);
                c.coordZ = (rng.nextInt(16000) - 8000);
                c.targetId = "minecraft:ravager";
                c.amount = 1;
                c.rewardExp = 1500;
                c.title = "Coordinate Subjugation @ " + c.coordX + ", " + c.coordZ;
            }
            case PARTY -> {
                c.targetId = pick(rng, SUBJUGATION_MOBS);
                c.amount = 30 + rng.nextInt(40);
                c.rewardExp = 3000;
                c.partyOnly = true;
                c.title = "Party Raid: " + c.amount + "x " + shortName(c.targetId);
            }
            case DWARF -> {
                c.targetId = pick(rng, new String[]{
                        "minecraft:iron_ingot", "minecraft:leather", "tensura_abyss:dark_slime"});
                c.amount = 16 + rng.nextInt(48);
                c.rewardExp = 500;
                c.rewardItem = "minecraft:emerald";
                c.rewardCount = 4 + rng.nextInt(8);
                c.title = "Dwarf Commission: liefere " + c.amount + "x " + shortName(c.targetId);
            }
        }
        return c;
    }

    /** Entfernt den Auftrag vom Board (nach erfolgreichem Abschluss via KubeJS). */
    public static Commission complete(UUID id) {
        Commission c = byId(id);
        if (c != null) BOARD.remove(c);
        return c;
    }

    private static String pick(RandomSource rng, String[] pool) {
        return pool[rng.nextInt(pool.length)];
    }

    private static String shortName(String id) {
        int i = id.indexOf(':');
        return i >= 0 ? id.substring(i + 1) : id;
    }
}
