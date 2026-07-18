package net.tensura.abyss.client;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.tensura.abyss.TensuraAbyss;
import net.tensura.abyss.client.hud.AbyssSidebarHud;

/**
 * Client-only mod-bus wiring:
 *   • registers the sidebar status HUD as a GUI layer
 *   • force-enables the bundled "abyss_dark_ui" resource pack, which reskins
 *     Tensura's reincarnation menu (assets/tensura/... override) with the
 *     dark violet Shadow theme.
 */
@EventBusSubscriber(modid = TensuraAbyss.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AbyssClientEvents {
    private AbyssClientEvents() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "sidebar_status"),
                new AbyssSidebarHud());
    }

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
