package net.tensura.abyss.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.tensura.abyss.registry.ModArmorMaterials;

import java.util.List;

/**
 * Slime-Suit-Ruestungsteil. Nutzt das eigene {@link ModArmorMaterials#SLIME_SUIT}
 * Material, dessen Layer auf die Slime-Texturen zeigt -> der Vanilla-Armor-Layer
 * rendert die Slime-Optik am Spielerkoerper (kein Netherit-Fallback).
 *
 * Der Stealth-Set-Bonus (Speed/Resistance/Invisibility) wird server-seitig im
 * KubeJS-Skript shadow_garden.js ueber ein volles Set gesteuert.
 */
public class SlimeSuitArmorItem extends ArmorItem {

    public SlimeSuitArmorItem(Type type, Item.Properties properties) {
        super(ModArmorMaterials.SLIME_SUIT, type, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tensura_abyss.slime_suit.set")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.tensura_abyss.slime_suit.stealth")
                .withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
