package net.tensura.abyss.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * SLIME SWORD — the pitch-black, fluid-shifting blade of the shadow path.
 *
 * Balance: deliberately early-to-mid game. 6.5 total attack damage (between
 * iron 6 and diamond 7), normal sword speed, 384 durability. Its uniqueness
 * comes from utility, not raw power:
 *   • +0.75 entity interaction range (the blade flows further than it looks)
 *   • fluid durability — the slime slowly reknits itself (1 durability / 10 s)
 *   • Form Shift: Aegis  (sneak + right-click, 45 s cooldown): 6 s of
 *     Absorption II + Resistance I at the cost of Slowness II
 *   • Shadow Step        (right-click, 20 s cooldown): short blink (max 8
 *     blocks) castable ONLY in light level below 7
 *   • draw cue: a wet squelch + void particles whenever the blade is drawn
 */
public class SlimeSwordItem extends SwordItem {

    private static final int AEGIS_COOLDOWN_TICKS = 900;  // 45 s
    private static final int STEP_COOLDOWN_TICKS  = 400;  // 20 s
    private static final int EFFECT_TICKS         = 120;  // 6 s
    private static final int MAX_STEP_BLOCKS      = 8;
    private static final int MAX_LIGHT_FOR_STEP   = 7;    // exclusive

    private static final String AEGIS_READY_AT = "sgAegisReadyAt";
    private static final String STEP_READY_AT  = "sgStepReadyAt";
    private static final String DRAWN_FLAG     = "sgSlimeBladeDrawn";

    public SlimeSwordItem(Properties properties) {
        super(Tiers.IRON, properties
                .durability(384)
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 5.5, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ENTITY_INTERACTION_RANGE,
                                new AttributeModifier(ResourceLocation.fromNamespaceAndPath("tensura_abyss", "slime_sword_reach"),
                                        0.75, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build()));
    }

    // ═══════════════ ABILITIES ═══════════════

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        return player.isCrouching()
                ? formShiftAegis(level, player, stack)
                : shadowStep(level, player, stack);
    }

    /** Sneak + right-click: defensive coat — absorption + resistance, slowed. */
    private InteractionResultHolder<ItemStack> formShiftAegis(Level level, Player player, ItemStack stack) {
        long now = level.getGameTime();
        long readyAt = player.getPersistentData().getLong(AEGIS_READY_AT);
        if (now < readyAt) {
            cooldownBar(player, "Form Shift: Aegis", readyAt - now);
            return InteractionResultHolder.fail(stack);
        }
        player.getPersistentData().putLong(AEGIS_READY_AT, now + AEGIS_COOLDOWN_TICKS);

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, EFFECT_TICKS, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_TICKS, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_TICKS, 1));

        abilityCue(level, player);
        player.displayClientMessage(Component.literal("Form Shift: Aegis — the slime hardens around you.")
                .withStyle(ChatFormatting.DARK_PURPLE), true);
        return InteractionResultHolder.success(stack);
    }

    /** Right-click: short blink, only from darkness (light level below 7). */
    private InteractionResultHolder<ItemStack> shadowStep(Level level, Player player, ItemStack stack) {
        long now = level.getGameTime();
        long readyAt = player.getPersistentData().getLong(STEP_READY_AT);
        if (now < readyAt) {
            cooldownBar(player, "Shadow Step", readyAt - now);
            return InteractionResultHolder.fail(stack);
        }
        if (level.getMaxLocalRawBrightness(player.blockPosition()) >= MAX_LIGHT_FOR_STEP) {
            player.displayClientMessage(Component.literal("The light betrays you — Shadow Step needs darkness (light < 7).")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        // Walk the look vector back from max range until the body fits.
        Vec3 look = player.getLookAngle();
        Vec3 origin = player.position();
        Vec3 target = null;
        for (double d = MAX_STEP_BLOCKS; d >= 1.5; d -= 0.5) {
            Vec3 candidate = origin.add(look.scale(d));
            if (level.noCollision(player, player.getBoundingBox().move(candidate.subtract(origin)))) {
                target = candidate;
                break;
            }
        }
        if (target == null) {
            player.displayClientMessage(Component.literal("No room in the shadows to step into.")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        player.getPersistentData().putLong(STEP_READY_AT, now + STEP_COOLDOWN_TICKS);

        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SQUID_INK, origin.x, origin.y + 1.0, origin.z, 24, 0.3, 0.6, 0.3, 0.02);
        }
        player.teleportTo(target.x, target.y, target.z);
        player.fallDistance = 0.0F;
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 1.0, target.z, 32, 0.3, 0.6, 0.3, 0.05);
        }
        abilityCue(level, player);
        stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        return InteractionResultHolder.success(stack);
    }

    private static void cooldownBar(Player player, String ability, long ticksLeft) {
        player.displayClientMessage(Component.literal(
                ability + " recharging... (" + (ticksLeft / 20 + 1) + "s)")
                .withStyle(ChatFormatting.DARK_GRAY), true);
    }

    /** The signature squelch/void cue for shadow gear and abilities. */
    public static void abilityCue(Level level, Player player) {
        level.playSound(null, player.blockPosition(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 0.55F);
        level.playSound(null, player.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 0.6F, 0.8F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SQUID_INK,
                    player.getX(), player.getY() + 1.1, player.getZ(), 14, 0.25, 0.45, 0.25, 0.015);
        }
    }

    // ═══════════════ FLUID DURABILITY + DRAW CUE ═══════════════

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;

        // Fluid durability: the slime reknits itself — 1 point every 10 s.
        if (level.getGameTime() % 200 == 0 && stack.isDamaged()) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }

        // Draw cue: squelch + void burst when the blade is newly drawn.
        boolean drawn = player.getPersistentData().getBoolean(DRAWN_FLAG);
        boolean holding = isSelected || player.getOffhandItem() == stack;
        if (holding && !drawn) {
            player.getPersistentData().putBoolean(DRAWN_FLAG, true);
            abilityCue(level, player);
        } else if (!holding && drawn && player.getMainHandItem().getItem() != this) {
            player.getPersistentData().putBoolean(DRAWN_FLAG, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        Language lang = Language.getInstance();
        for (int i = 1; i <= 5; i++) {
            String key = "tooltip.tensura_abyss.slime_sword.l" + i;
            if (lang.has(key)) tooltip.add(Component.translatable(key));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
