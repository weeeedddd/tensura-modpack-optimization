package net.tensura.abyss.registry;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tensura.abyss.TensuraAbyss;
import net.tensura.abyss.item.IAmAtomicItem;
import net.tensura.abyss.item.SignatureRecordItem;
import net.tensura.abyss.item.SlimeSuitArmorItem;

/**
 * Alle Items der Companion-Mod. Item-Texturen (Inventar) liegen unter
 * assets/tensura_abyss/textures/item/&lt;id&gt;.png mit passendem Modell.
 */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TensuraAbyss.MOD_ID);

    // ── Kern-Ressourcen ──
    public static final DeferredItem<Item> DARK_SLIME = ITEMS.registerItem(
            "dark_slime",
            props -> new Item(props.rarity(Rarity.RARE).stacksTo(64))
    );

    public static final DeferredItem<Item> DARK_AETHER = ITEMS.registerItem(
            "dark_aether",
            props -> new Item(props.rarity(Rarity.EPIC).stacksTo(64).fireResistant())
    );

    // ── Kult von Diablos: Drop + Fraktions-Werkzeuge (aus KubeJS uebernommen) ──
    public static final DeferredItem<Item> CULT_INSIGNIA = ITEMS.registerItem(
            "cult_insignia",
            props -> new Item(props.rarity(Rarity.UNCOMMON).stacksTo(64))
    );

    public static final DeferredItem<Item> MITSUGOSHI_LEDGER = ITEMS.registerItem(
            "mitsugoshi_ledger",
            props -> new Item(props.rarity(Rarity.RARE).stacksTo(1))
    );

    public static final DeferredItem<Item> SHADOW_PLEDGE_NOTE = ITEMS.registerItem(
            "shadow_pledge_note",
            props -> new Item(props.rarity(Rarity.UNCOMMON).stacksTo(16))
    );

    // ── BlockItem fuer den Abyss-Portal-Rahmen ──
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ABYSS_PORTAL_FRAME =
            ITEMS.registerSimpleBlockItem("abyss_portal_frame", ModBlocks.ABYSS_PORTAL_FRAME);

    // ── Gilden-Item: signiertes Mitglieds-Dokument ──
    public static final DeferredItem<Item> SIGNATURE_RECORD = ITEMS.registerItem(
            "signature_record",
            props -> new SignatureRecordItem(props.rarity(Rarity.EPIC).stacksTo(1))
    );

    // ── Ultimate-Skill-Katalysator "I Am Atomic" ──
    public static final DeferredItem<Item> I_AM_ATOMIC_CATALYST = ITEMS.registerItem(
            "i_am_atomic_catalyst",
            props -> new IAmAtomicItem(props.rarity(Rarity.EPIC).stacksTo(1).fireResistant())
    );

    // ── Slime Suit (eigenes Armor-Material -> Slime-Layer am Koerper) ──
    public static final DeferredItem<ArmorItem> SLIME_SUIT_HELMET = ITEMS.registerItem(
            "slime_suit_helmet",
            props -> new SlimeSuitArmorItem(ArmorItem.Type.HELMET,
                    props.durability(ArmorItem.Type.HELMET.getDurability(37)))
    );
    public static final DeferredItem<ArmorItem> SLIME_SUIT_CHESTPLATE = ITEMS.registerItem(
            "slime_suit_chestplate",
            props -> new SlimeSuitArmorItem(ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(37)))
    );
    public static final DeferredItem<ArmorItem> SLIME_SUIT_LEGGINGS = ITEMS.registerItem(
            "slime_suit_leggings",
            props -> new SlimeSuitArmorItem(ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(37)))
    );
    public static final DeferredItem<ArmorItem> SLIME_SUIT_BOOTS = ITEMS.registerItem(
            "slime_suit_boots",
            props -> new SlimeSuitArmorItem(ArmorItem.Type.BOOTS,
                    props.durability(ArmorItem.Type.BOOTS.getDurability(37)))
    );
}
