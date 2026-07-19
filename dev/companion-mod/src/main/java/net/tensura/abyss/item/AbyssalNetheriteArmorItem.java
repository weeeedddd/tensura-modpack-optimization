package net.tensura.abyss.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tensura.abyss.registry.ModArmorMaterials;

import java.util.List;

/** Endgame armor forged from resources that only form inside the Shadow Abyss. */
public final class AbyssalNetheriteArmorItem extends ArmorItem {
    public AbyssalNetheriteArmorItem(Type type, Item.Properties properties) {
        super(ModArmorMaterials.ABYSSAL_NETHERITE, type, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tensura_abyss.abyssal_netherite.scaling")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.tensura_abyss.abyssal_netherite.armor")
                .withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
