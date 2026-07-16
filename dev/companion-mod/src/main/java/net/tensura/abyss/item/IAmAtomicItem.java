package net.tensura.abyss.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tensura.abyss.bridge.TensuraBridge;

import java.util.List;

/**
 * Der ultimative Skill "I Am Atomic" als Item.
 *
 * Voraussetzungen (server-seitig geprueft):
 *   • Rang "Shadow" (persistentData sgEvoRank >= 6 — von shadow_evos.js gesetzt)
 *   • >= 5.000.000 Max EP (via {@link TensuraBridge#getMaxEP} — Scoreboard-Fallback)
 * Effekt:
 *   • gigantisches neon-blaues Partikel-Dornenfeld (kreisfoermig ausbreitend)
 *   • tiefer Bass-Sound
 *   • massiver Schaden an ALLEN feindlichen Mobs im Umkreis von 30 Bloecken
 * Server-Schutz:
 *   • Schaden direkt ueber entity.hurt(...) — KEINE Explosion, KEINE Blockschaeden.
 */
public class IAmAtomicItem extends Item {

    private static final int    REQUIRED_RANK   = 6;        // "Shadow"
    private static final double REQUIRED_MAX_EP = 5_000_000.0;
    private static final double RADIUS          = 30.0;
    private static final float  DAMAGE          = 120.0F;
    private static final int    COOLDOWN_TICKS  = 2400;     // 120 s

    public IAmAtomicItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        // ── Gate 1: Rang "Shadow" ──
        if (player.getPersistentData().getInt("sgEvoRank") < REQUIRED_RANK) {
            player.displayClientMessage(Component.literal(
                    "Nur der Rang [Shadow] kann \"I Am Atomic\" entfesseln.")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        // ── Gate 2: >= 5.000.000 Max EP ──
        double maxEp = TensuraBridge.getMaxEP(player);
        if (maxEp < REQUIRED_MAX_EP) {
            player.displayClientMessage(Component.literal(
                    "Zu wenig Max EP: " + (long) maxEp + " / " + (long) REQUIRED_MAX_EP)
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel server = (ServerLevel) level;
        Vec3 center = player.position();

        // ── Effekt: neon-blaues Dornenfeld (mehrere Ringe) ──
        spawnThornField(server, center);

        // ── Tiefer Bass-Sound ──
        level.playSound(null, player.blockPosition(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 6.0F, 0.5F);
        level.playSound(null, player.blockPosition(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 6.0F, 0.4F);

        // ── AoE-Schaden an feindlichen Mobs (KEINE Blockschaeden) ──
        AABB box = player.getBoundingBox().inflate(RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && (e instanceof Enemy));
        for (LivingEntity target : targets) {
            if (target.distanceToSqr(center) <= RADIUS * RADIUS) {
                target.hurt(level.damageSources().indirectMagic(player, player), DAMAGE);
            }
        }

        // ── Kosten & Cooldown ──
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        player.displayClientMessage(Component.literal("I... AM... ATOMIC.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);

        return InteractionResultHolder.success(stack);
    }

    /** Zeichnet mehrere expandierende, neon-blaue Partikelringe (server-seitig). */
    private void spawnThornField(ServerLevel level, Vec3 center) {
        double[] radii = { RADIUS * 0.35, RADIUS * 0.6, RADIUS * 0.85 };
        for (double r : radii) {
            int points = (int) Math.max(24, r * 3);
            for (int i = 0; i < points; i++) {
                double ang = (Math.PI * 2.0 * i) / points;
                double x = center.x + Math.cos(ang) * r;
                double z = center.z + Math.sin(ang) * r;
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, center.y + 0.4, z,
                        2, 0.05, 0.35, 0.05, 0.01);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, center.y + 0.3, z,
                        1, 0.05, 0.2, 0.05, 0.06);
            }
        }
        // heller Kern-Blitz
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0, center.z,
                2, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0, center.z,
                120, RADIUS * 0.2, 1.0, RADIUS * 0.2, 0.04);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tensura_abyss.i_am_atomic.req")
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("tooltip.tensura_abyss.i_am_atomic.effect")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
