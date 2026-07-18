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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tensura.abyss.bridge.TensuraBridge;

import java.util.List;

/**
 * The ultimate skill "I Am Atomic" as an item.
 *
 * Requirements (checked server-side):
 *   • Active race {@code tensura_abyss:eminence_of_the_abyss} — read live from
 *     the native ManasCore race system via {@link TensuraBridge#hasRace}.
 *   • >= 5,000,000 EP (via {@link TensuraBridge#getMaxEP} — scoreboard fallback)
 * Effect:
 *   • giant neon-blue particle thorn field (expanding rings)
 *   • deep bass sound
 *   • massive damage to ALL hostile mobs within 30 blocks
 * Server safety:
 *   • damage goes through entity.hurt(...) — NO explosion, NO block damage.
 *
 * CUSTOM VISUAL HOOK PLAN (structural, for later polish):
 *   1. Particles: register a custom ParticleType in a new
 *      {@code net.tensura.abyss.registry.ModParticles} (DeferredRegister on
 *      BuiltInRegistries.PARTICLE_TYPE), texture under
 *      {@code assets/tensura_abyss/textures/particle/atomic_thorn.png} +
 *      {@code assets/tensura_abyss/particles/atomic_thorn.json}, then swap it
 *      in for SOUL_FIRE_FLAME/ELECTRIC_SPARK inside {@link #spawnThornField}.
 *   2. Sound: custom event in {@code assets/tensura_abyss/sounds.json}
 *      ("skill.i_am_atomic") + ogg under sounds/skill/, registered via a
 *      {@code ModSounds} DeferredRegister, replacing WARDEN_SONIC_BOOM.
 *   3. Item texture: assets/tensura_abyss/textures/item/i_am_atomic_catalyst.png
 *      (already wired through the standard item model).
 */
public class IAmAtomicItem extends Item {

    /** Only the final slime-tree form may unleash the skill. */
    private static final ResourceLocation REQUIRED_RACE =
            ResourceLocation.parse("tensura_abyss:eminence_of_the_abyss");
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

        // ── Gate 1: active race must be [Eminence of the Abyss] ──
        // Read live from the native race system — no stale script flags.
        if (!TensuraBridge.hasRace(player, REQUIRED_RACE)) {
            player.displayClientMessage(Component.literal(
                    "Only the Eminence of the Abyss can unleash I Am Atomic.")
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
            return InteractionResultHolder.fail(stack);
        }

        // ── Gate 2: >= 5,000,000 EP ──
        double maxEp = TensuraBridge.getMaxEP(player);
        if (maxEp < REQUIRED_MAX_EP) {
            player.displayClientMessage(Component.literal(
                    "Not enough Existence Points: " + (long) maxEp + " / " + (long) REQUIRED_MAX_EP)
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
