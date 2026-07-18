package net.tensura.abyss.client;

import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.RaceAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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

    /** team name -> [colored rank badge]. */
    private static final Map<String, String> RANK_BADGES = Map.of(
            "sg_lord",    "§8[§5Shadow Lord§8]",
            "sg_seven",   "§8[§dSeven Shadows§8]",
            "sg_numbers", "§8[§bNumbers§8]",
            "sg_shadow",  "§8[§7Shadow§8]"
    );

    private static final String FORGED_BADGE = "§8[§5Shadow Lord§8]";

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

        String badge;
        if (bearingInsignia) {
            badge = FORGED_BADGE; // the impostor's forged aura
        } else {
            var team = target.getTeam();
            badge = team == null ? null : RANK_BADGES.get(team.getName());
        }
        if (badge == null) return;

        event.setContent(Component.literal(badge + " ").append(event.getContent()));
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
