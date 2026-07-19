package net.tensura.abyss.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tensura.abyss.TensuraAbyss;

import java.util.EnumMap;
import java.util.List;

/**
 * Eigenes Armor-Material fuer den Slime Suit.
 *
 * WICHTIG (der "echte Armor-Renderer"): Das Material verweist ueber seine
 * {@link ArmorMaterial.Layer} auf die Textur-ID {@code tensura_abyss:slime_suit}.
 * Der Vanilla-{@code HumanoidArmorLayer} rendert dadurch automatisch:
 *   assets/tensura_abyss/textures/models/armor/slime_suit_layer_1.png  (Helm/Brust/Stiefel)
 *   assets/tensura_abyss/textures/models/armor/slime_suit_layer_2.png  (Hose)
 * -> KEIN Netherit-Fallback, kein eigener Renderer noetig.
 */
public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, TensuraAbyss.MOD_ID);

    // Schutzwerte pro Slot (Netherit-Niveau)
    private static final EnumMap<ArmorItem.Type, Integer> SLIME_DEFENSE = new EnumMap<>(ArmorItem.Type.class);
    private static final EnumMap<ArmorItem.Type, Integer> ABYSSAL_DEFENSE = new EnumMap<>(ArmorItem.Type.class);
    static {
        SLIME_DEFENSE.put(ArmorItem.Type.BOOTS, 3);
        SLIME_DEFENSE.put(ArmorItem.Type.LEGGINGS, 6);
        SLIME_DEFENSE.put(ArmorItem.Type.CHESTPLATE, 8);
        SLIME_DEFENSE.put(ArmorItem.Type.HELMET, 3);
        SLIME_DEFENSE.put(ArmorItem.Type.BODY, 11);
        ABYSSAL_DEFENSE.put(ArmorItem.Type.BOOTS, 4);
        ABYSSAL_DEFENSE.put(ArmorItem.Type.LEGGINGS, 7);
        ABYSSAL_DEFENSE.put(ArmorItem.Type.CHESTPLATE, 9);
        ABYSSAL_DEFENSE.put(ArmorItem.Type.HELMET, 4);
        ABYSSAL_DEFENSE.put(ArmorItem.Type.BODY, 12);
    }

    public static final Holder<ArmorMaterial> SLIME_SUIT = ARMOR_MATERIALS.register(
            "slime_suit",
            () -> new ArmorMaterial(
                    SLIME_DEFENSE,
                    16,                                   // enchantmentValue
                    SoundEvents.ARMOR_EQUIP_NETHERITE,    // equipSound
                    () -> Ingredient.of(ModItems.DARK_SLIME.get()),  // repairIngredient
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "slime_suit"))),
                    3.0F,                                 // toughness
                    0.1F                                  // knockbackResistance
            )
    );

    public static final Holder<ArmorMaterial> ABYSSAL_NETHERITE = ARMOR_MATERIALS.register(
            "abyssal_netherite",
            () -> new ArmorMaterial(
                    ABYSSAL_DEFENSE,
                    18,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(ModItems.ABYSSAL_NETHERITE_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(
                            TensuraAbyss.MOD_ID, "abyssal_netherite"))),
                    4.0F,
                    0.15F
            )
    );
}
