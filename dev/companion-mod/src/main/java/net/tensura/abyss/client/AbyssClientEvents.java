package net.tensura.abyss.client;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.tensura.abyss.TensuraAbyss;

/**
 * Client-only mod-bus wiring: force-enables the bundled "abyss_dark_ui"
 * resource pack, which reskins Tensura's reincarnation menu
 * (assets/tensura/... override) with the dark violet Shadow theme.
 * (The former sidebar HUD was removed on request — the right edge stays clean.)
 */
@EventBusSubscriber(modid = TensuraAbyss.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AbyssClientEvents {
    private AbyssClientEvents() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "resourcepacks/abyss_dark_ui"),
                PackType.CLIENT_RESOURCES,
                Component.literal("Tensura Abyss — Dark UI"),
                PackSource.BUILT_IN,
                true,                    // always active — the reskin is part of the pack
                Pack.Position.TOP);      // wins over tensura's own asset
    }
}
