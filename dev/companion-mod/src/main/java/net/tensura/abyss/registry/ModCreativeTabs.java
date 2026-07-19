package net.tensura.abyss.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tensura.abyss.TensuraAbyss;

/** Kreativ-Tab, das alle Companion-Items buendelt. */
public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TensuraAbyss.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHADOW_TAB =
            CREATIVE_TABS.register("shadow_garden", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tensura_abyss.shadow_garden"))
                    .icon(() -> new ItemStack(ModItems.DARK_AETHER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.DARK_SLIME.get());
                        output.accept(ModItems.DARK_AETHER.get());
                        output.accept(ModItems.SLIME_SWORD.get());
                        output.accept(ModItems.FALSE_EMINENCE_INSIGNIA.get());
                        output.accept(ModItems.MAGICULE_SPIRE_CRYSTAL.get());
                        output.accept(ModItems.CONDENSED_DARK_MATTER.get());
                        output.accept(ModItems.ABYSSAL_NETHERITE_INGOT.get());
                        output.accept(ModItems.ABYSSAL_NETHERITE_SWORD.get());
                        output.accept(ModItems.ABYSSAL_NETHERITE_HELMET.get());
                        output.accept(ModItems.ABYSSAL_NETHERITE_CHESTPLATE.get());
                        output.accept(ModItems.ABYSSAL_NETHERITE_LEGGINGS.get());
                        output.accept(ModItems.ABYSSAL_NETHERITE_BOOTS.get());
                        output.accept(ModItems.SIGNATURE_RECORD.get());
                        output.accept(ModItems.CULT_INSIGNIA.get());
                        output.accept(ModItems.MITSUGOSHI_LEDGER.get());
                        output.accept(ModItems.SHADOW_PLEDGE_NOTE.get());
                        output.accept(ModItems.ABYSS_PORTAL_FRAME.get());
                        output.accept(ModItems.SLIME_SUIT_HELMET.get());
                        output.accept(ModItems.SLIME_SUIT_CHESTPLATE.get());
                        output.accept(ModItems.SLIME_SUIT_LEGGINGS.get());
                        output.accept(ModItems.SLIME_SUIT_BOOTS.get());
                    })
                    .build());
}
