package net.tensura.abyss.client;

import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.RaceAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.tensura.abyss.TensuraAbyss;
import net.tensura.abyss.registry.ModItems;

import java.util.Map;
import java.util.Optional;

/**
 * SHADOW SIGHT — faction rank visibility &amp; deception (client render logic).
 *
 * The pledge teams no longer carry nameplate prefixes (cleared server-side in
 * shadow_garden.js), so BY DEFAULT nobody sees faction ranks over heads.
 * This handler re-adds the rank line — but only for viewers who themselves
 * hold a tensura_abyss shadow race ("shadow seers").
 *
 * INSIGNIA OF FALSE EMINENCE (held in either hand, both hands sync to all
 * clients):
 *   • non-shadow bearer → shadow seers are TRICKED into reading a forged
 *     top rank ("Shadow Lord") over the impostor's head
 *   • shadow bearer     → total presence masking: the nametag is suppressed
 *     entirely, for every viewer
 */
@EventBusSubscriber(modid = TensuraAbyss.MOD_ID, value = Dist.CLIENT)
public final class ShadowSightHandler {
    private ShadowSightHandler() {}

    private static final Map<String, ChatFormatting> RANK_COLORS = Map.of(
            "sg_lord", ChatFormatting.DARK_PURPLE,
            "sg_seven", ChatFormatting.LIGHT_PURPLE,
            "sg_numbers", ChatFormatting.AQUA,
            "sg_shadow", ChatFormatting.GRAY
    );
    private static final Map<String, String> RANK_LABELS = Map.of(
            "sg_lord", "Shadow Lord", "sg_seven", "Seven Shadows",
            "sg_numbers", "Numbers", "sg_shadow", "Shadow");

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer == null || target == viewer) return;

        boolean targetShadow = hasShadowRace(target);
        boolean bearingInsignia = holdsInsignia(target);

        // A shadow bearer of the insignia vanishes from every nameplate.
        if (bearingInsignia && targetShadow) {
            event.setCanRender(TriState.FALSE);
            return;
        }

        // Only shadow seers may perceive faction ranks at all.
        if (!hasShadowRace(viewer)) return;

        String teamName = target.getTeam() == null ? null : target.getTeam().getName();
        String label;
        ChatFormatting color;
        if (bearingInsignia) {
            label = "Shadow Lord";
            color = ChatFormatting.DARK_PURPLE;
        } else {
            label = RANK_LABELS.get(teamName);
            color = RANK_COLORS.get(teamName);
        }
        if (label == null || color == null) return;

        event.setContent(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(label).withStyle(color))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY))
                .append(event.getContent().copy().withStyle(ChatFormatting.WHITE)));
    }

    /** Best-effort race check (ManasCore race storage syncs to trackers). */
    private static boolean hasShadowRace(Player player) {
        try {
            Optional<ManasRaceInstance> race = RaceAPI.getRaceFrom(player).getRace();
            return race.map(r -> {
                ResourceLocation id = r.getRaceId();
                return id != null && TensuraAbyss.MOD_ID.equals(id.getNamespace());
            }).orElse(false);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean holdsInsignia(Player player) {
        var insignia = ModItems.FALSE_EMINENCE_INSIGNIA.get();
        return player.getMainHandItem().is(insignia) || player.getOffhandItem().is(insignia);
    }
}
