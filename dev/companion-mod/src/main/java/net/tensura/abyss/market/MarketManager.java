package net.tensura.abyss.market;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Mitsugoshi-Schwarzmarkt: eine eigene Waehrung ("Mitsugoshi-Muenzen"), die aus
 * MineColonies-/Handels-Erzeugnissen generiert und gegen seltene Materialien
 * eingetauscht wird. Common-Code (Server-Wahrheit); die GUI liest nur
 * {@link #OFFERS} zum Anzeigen.
 *
 * Muenzen liegen in den PersistentData des Spielers ("mitsugoshiCoins") und sind
 * damit auch fuer KubeJS sichtbar.
 */
public final class MarketManager {
    private MarketManager() {}

    public static final String COIN_KEY = "mitsugoshiCoins";

    /** Ein Kauf-Angebot: Kosten in Muenzen -> Item x Menge. */
    public record Offer(int cost, String itemId, int count, String label) {}

    /** Angebote (Server = Wahrheit, Client rendert dieselbe Liste). */
    public static final List<Offer> OFFERS = List.of(
            new Offer(500, "tensura_abyss:dark_aether", 1, "Dunkler Aether"),
            new Offer(150, "tensura_abyss:dark_slime", 4, "Dunkler Schleim"),
            new Offer(120, "minecraft:amethyst_shard", 8, "Magische Essenz"),
            new Offer(220, "minecraft:echo_shard", 2, "Abyss-Fragment"),
            new Offer(100, "minecraft:diamond", 4, "Diamanten")
    );

    /** Was MineColonies-/Handels-Erzeugnisse beim Umwandeln wert sind. */
    private record Conversion(String itemId, int perUnit) {}
    private static final List<Conversion> CONVERSIONS = List.of(
            new Conversion("minecraft:emerald", 25),
            new Conversion("minecraft:gold_ingot", 8),
            new Conversion("minecraft:diamond", 20),
            new Conversion("minecraft:bread", 1),
            new Conversion("minecraft:leather", 2),
            new Conversion("minecraft:iron_ingot", 4)
    );

    // ── Muenzen ──
    public static long getCoins(ServerPlayer player) {
        return player.getPersistentData().getLong(COIN_KEY);
    }

    public static void addCoins(ServerPlayer player, long delta) {
        player.getPersistentData().putLong(COIN_KEY, Math.max(0, getCoins(player) + delta));
    }

    public static boolean trySpend(ServerPlayer player, long amount) {
        long c = getCoins(player);
        if (c < amount) return false;
        player.getPersistentData().putLong(COIN_KEY, c - amount);
        return true;
    }

    // ── Umwandlung: Erzeugnisse -> Muenzen ──
    public static long convertProducts(ServerPlayer player) {
        long gained = 0;
        var inv = player.getInventory();
        for (Conversion conv : CONVERSIONS) {
            Item item = resolve(conv.itemId());
            if (item == null) continue;
            for (int slot = 0; slot < inv.getContainerSize(); slot++) {
                ItemStack stack = inv.getItem(slot);
                if (!stack.isEmpty() && stack.is(item)) {
                    gained += (long) stack.getCount() * conv.perUnit();
                    inv.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
        if (gained > 0) {
            addCoins(player, gained);
            player.sendSystemMessage(Component.literal(
                    "§6Mitsugoshi: +" + gained + " Muenzen aus Handels-Erzeugnissen."));
        } else {
            player.sendSystemMessage(Component.literal(
                    "§7Keine umwandelbaren Erzeugnisse im Inventar."));
        }
        return gained;
    }

    // ── Kauf ──
    public static boolean buy(ServerPlayer player, int offerIndex) {
        if (offerIndex < 0 || offerIndex >= OFFERS.size()) return false;
        Offer offer = OFFERS.get(offerIndex);
        Item item = resolve(offer.itemId());
        if (item == null) {
            player.sendSystemMessage(Component.literal("§cAngebot nicht verfuegbar: " + offer.itemId()));
            return false;
        }
        if (!trySpend(player, offer.cost())) {
            player.sendSystemMessage(Component.literal("§cNicht genug Mitsugoshi-Muenzen (" +
                    getCoins(player) + "/" + offer.cost() + ")."));
            return false;
        }
        ItemStack result = new ItemStack(item, offer.count());
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
        player.sendSystemMessage(Component.literal("§dGekauft: " + offer.count() + "x " + offer.label() +
                " (−" + offer.cost() + " Muenzen)."));
        return true;
    }

    private static Item resolve(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        // BuiltInRegistries.ITEM.get gibt bei unbekannter ID AIR zurueck
        return item == net.minecraft.world.item.Items.AIR ? null : item;
    }
}
