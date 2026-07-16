package net.tensura.abyss.event;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tensura.abyss.TensuraAbyss;
import net.tensura.abyss.registry.ModItems;

/**
 * Server-Game-Events der Companion-Mod:
 *  1) ABYSS-KORRUPTION (Void Corruption): Wer die Shadow-Abyss ohne vollstaendigen
 *     Slime Suit betritt, baut alle 20 s eine Korruptionsstufe mit immer
 *     schlimmeren Debuffs auf. Volles Set = komplette Immunitaet.
 *  2) KULT-ANFUEHRER-KILL: markiert den Bezwinger eines "Diablos-Ritters"
 *     (Evolutions-Bedingung fuer den Rang [Numbers]).
 */
@EventBusSubscriber(modid = TensuraAbyss.MOD_ID)
public final class ShadowGameEvents {
    private ShadowGameEvents() {}

    private static final ResourceLocation ABYSS =
            ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "shadow_abyss");

    private static final int TICK_INTERVAL = 400; // 20 Sekunden
    private static final int MAX_CORRUPTION = 8;
    private static final String KEY_CORRUPTION = "sgVoidCorruption";
    private static final String KEY_ENTERED = "sgEnteredAbyss";

    // ─────────────────────────── VOID CORRUPTION ───────────────────────────
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % TICK_INTERVAL != 0) return;

        boolean inAbyss = player.level().dimension().location().equals(ABYSS);
        CompoundTag data = player.getPersistentData();
        int corruption = data.getInt(KEY_CORRUPTION);

        if (!inAbyss) {
            // Ausserhalb der Abyss klingt die Korruption langsam ab.
            if (corruption > 0) {
                setCorruption(player, corruption - 1);
            }
            return;
        }

        data.putBoolean(KEY_ENTERED, true); // Evolutions-Bedingung [Shadow]

        if (fullSlimeSuit(player)) {
            if (corruption != 0) {
                setCorruption(player, 0);
                player.displayClientMessage(Component.translatable(
                        "message.tensura_abyss.corruption.immune")
                        .withStyle(ChatFormatting.AQUA), true);
            }
            return; // Slime Suit = immun
        }

        int level = Math.min(corruption + 1, MAX_CORRUPTION);
        setCorruption(player, level);
        applyDebuffs(player, level);
        player.displayClientMessage(Component.translatable(
                "message.tensura_abyss.corruption.rising", level, MAX_CORRUPTION)
                .withStyle(ChatFormatting.DARK_PURPLE), true);
    }

    private static void applyDebuffs(Player player, int level) {
        int dur = TICK_INTERVAL + 60; // etwas laenger als das Intervall -> nahtlos
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur,
                Math.min(level - 1, 4), false, true));
        if (level >= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, dur,
                    Math.min(level - 2, 3), false, true));
        }
        if (level >= 4) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, true));
        }
        if (level >= 6) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, dur,
                    Math.min(level - 5, 2), false, true));
        }
        if (level >= MAX_CORRUPTION) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, true));
        }
    }

    private static void setCorruption(Player player, int value) {
        player.getPersistentData().putInt(KEY_CORRUPTION, Math.max(0, value));
    }

    private static boolean fullSlimeSuit(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SLIME_SUIT_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SLIME_SUIT_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SLIME_SUIT_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SLIME_SUIT_BOOTS.get());
    }

    // ─────────────────────────── KULT-ANFUEHRER-KILL ───────────────────────────
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!event.getEntity().getTags().contains("cult_knight")) return;
        if (event.getSource().getEntity() instanceof Player killer) {
            killer.getPersistentData().putBoolean("sgKilledCultLeader", true);
            killer.displayClientMessage(Component.translatable(
                    "message.tensura_abyss.cult_leader_slain")
                    .withStyle(ChatFormatting.DARK_RED), true);
        }
    }
}
