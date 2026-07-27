package net.tensura.abyss.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tensura.abyss.TensuraAbyss;
import net.tensura.abyss.bridge.TensuraBridge;
import net.tensura.abyss.registry.ModItems;

/** Applies capped, server-authoritative bonuses derived from live maximum Magicules. */
@EventBusSubscriber(modid = TensuraAbyss.MOD_ID)
public final class AbyssalGearHandler {
    private AbyssalGearHandler() {}

    private static final ResourceLocation ATTACK_ID = id("abyssal_capacity_attack");
    private static final ResourceLocation ARMOR_ID = id("abyssal_capacity_armor");
    private static final ResourceLocation TOUGHNESS_ID = id("abyssal_capacity_toughness");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0) return;

        remove(player.getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_ID);
        remove(player.getAttribute(Attributes.ARMOR), ARMOR_ID);
        remove(player.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGHNESS_ID);

        double capacity = TensuraBridge.getMaxMagicules(player);
        if (player.getMainHandItem().is(ModItems.ABYSSAL_NETHERITE_SWORD.get())) {
            double attack = Math.min(8.0, Math.log10(1.0 + capacity / 5_000.0) * 3.0);
            add(player.getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_ID, attack);
        }
        if (hasFullSet(player)) {
            double armor = Math.min(6.0, Math.log10(1.0 + capacity / 10_000.0) * 2.0);
            double toughness = Math.min(4.0, armor * 0.65);
            add(player.getAttribute(Attributes.ARMOR), ARMOR_ID, armor);
            add(player.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGHNESS_ID, toughness);
        }
    }

    private static boolean hasFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ABYSSAL_NETHERITE_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ABYSSAL_NETHERITE_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ABYSSAL_NETHERITE_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ABYSSAL_NETHERITE_BOOTS.get());
    }

    private static void add(AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute == null || amount <= 0) return;
        attribute.addTransientModifier(new AttributeModifier(id, amount,
                AttributeModifier.Operation.ADD_VALUE));
    }

    private static void remove(AttributeInstance attribute, ResourceLocation id) {
        if (attribute != null) attribute.removeModifier(id);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, path);
    }
}
