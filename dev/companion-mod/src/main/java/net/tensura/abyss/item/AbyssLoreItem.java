package net.tensura.abyss.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Plain item with data-driven lore tooltips.
 *
 * Appends up to five translatable lines per item —
 * {@code tooltip.tensura_abyss.<path>.l1 … l5} — but only those that actually
 * exist in the loaded language file, so items opt in purely via
 * {@code en_us.json}. Formatting codes (§5, §6, §7, §8, …) are embedded in the
 * lang entries themselves, keeping all styling in one place.
 */
public class AbyssLoreItem extends Item {

    public AbyssLoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(this);
        Language lang = Language.getInstance();
        for (int i = 1; i <= 5; i++) {
            String key = "tooltip.tensura_abyss." + id.getPath() + ".l" + i;
            if (lang.has(key)) {
                tooltip.add(Component.translatable(key));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
