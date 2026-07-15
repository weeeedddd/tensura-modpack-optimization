package net.tensura.abyss.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * "Signature Record" — signiertes Gilden-Dokument, das ein Spieler beim
 * Beitritt zu einer Shadow-Garden-Gilde erhaelt. Traegt Gildenname,
 * Mitgliedsnummer und Abenteurer-Rang in den Item-Komponenten (custom_data),
 * gesetzt beim Ausstellen durch {@code GuildManager}.
 */
public class SignatureRecordItem extends Item {

    public SignatureRecordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        var data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data != null) {
            var tag = data.copyTag();
            if (tag.contains("guild")) {
                tooltip.add(Component.literal("Guild: " + tag.getString("guild"))
                        .withStyle(ChatFormatting.AQUA));
            }
            if (tag.contains("member_number")) {
                tooltip.add(Component.literal("Member #" + tag.getInt("member_number"))
                        .withStyle(ChatFormatting.GRAY));
            }
            if (tag.contains("rank")) {
                tooltip.add(Component.literal("Adventurer Rank: " + tag.getString("rank"))
                        .withStyle(ChatFormatting.GOLD));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.tensura_abyss.signature_record.blank")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
