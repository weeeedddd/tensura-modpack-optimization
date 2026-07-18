package net.tensura.abyss.event;

import com.mojang.logging.LogUtils;
import dev.architectury.event.EventResult;
import io.github.manasmods.manascore.race.api.RaceEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tensura.abyss.item.SlimeSwordItem;
import net.tensura.abyss.registry.ModItems;
import org.slf4j.Logger;

import java.util.Set;

/**
 * Grants the SLIME SWORD once when a player first enters (or evolves within)
 * the Shadow Slime race path. One-time via a persistent flag that survives
 * death; creative-mode players are included on purpose.
 */
public final class ShadowGearHandler {
    private ShadowGearHandler() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String GIVEN_FLAG = "sgGivenSlimeSword";

    /** Every stage of the Shadow Slime path plus the secret race. */
    private static final Set<ResourceLocation> SHADOW_PATH = Set.of(
            ResourceLocation.parse("tensura_abyss:shadow_slime"),
            ResourceLocation.parse("tensura_abyss:magicule_slime"),
            ResourceLocation.parse("tensura_abyss:abyss_slime"),
            ResourceLocation.parse("tensura_abyss:shadow_garden_guard"),
            ResourceLocation.parse("tensura_abyss:dark_slime_sovereign"),
            ResourceLocation.parse("tensura_abyss:shadow_lord"),
            ResourceLocation.parse("tensura_abyss:awakened_shadow_lord"),
            ResourceLocation.parse("tensura_abyss:abyss_monarch"),
            ResourceLocation.parse("tensura_abyss:eminence_of_the_abyss"),
            ResourceLocation.parse("tensura_abyss:stylish_bandit_slayer")
    );

    /** Call once from the mod constructor. */
    public static void init() {
        RaceEvents.SET_RACE.register((oldRace, entity, newRace, resetSkills, cancelled, message) -> {
            if (entity instanceof Player player && !player.level().isClientSide() && newRace != null) {
                ResourceLocation raceId = newRace.getRaceId();
                if (SHADOW_PATH.contains(raceId) && !hasFlag(player)) {
                    setFlag(player);
                    ItemStack sword = new ItemStack(ModItems.SLIME_SWORD.get());
                    if (!player.getInventory().add(sword)) {
                        player.drop(sword, false);
                    }
                    SlimeSwordItem.abilityCue(player.level(), player);
                    player.sendSystemMessage(Component.literal(
                            "§8── §5§lShadow Garden§r §8──"));
                    player.sendSystemMessage(Component.literal(
                            "§7A blade of living shadow seeps from your body — the §5Slime Sword§7 is yours."));
                    LOGGER.info("[Tensura Abyss] Granted Slime Sword to {} (race {}).",
                            player.getGameProfile().getName(), raceId);
                }
            }
            return EventResult.pass();
        });
    }

    private static boolean hasFlag(Player player) {
        return player.getPersistentData()
                .getCompound(Player.PERSISTED_NBT_TAG)
                .getBoolean(GIVEN_FLAG);
    }

    private static void setFlag(Player player) {
        var root = player.getPersistentData();
        var persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putBoolean(GIVEN_FLAG, true);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
