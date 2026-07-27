package net.tensura.abyss.race;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// ── ECHTE Tensura/ManasCore-API (1.21.1), verifiziert per javap gegen die
//    heruntergeladenen Jars (io.github.manasmods — NICHT com.github!). ──
import io.github.manasmods.tensura.race.TensuraRace;
import io.github.manasmods.tensura.race.template.EvolutionRequirement;
import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.RaceAPI;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.tensura.util.EnergyHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.tensura.abyss.bridge.TensuraBridge;
import net.tensura.abyss.registry.AbyssRaces;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Eine parametrisierte Tensura-Rasse (1.21.1). Erbt von {@link TensuraRace}
 * (das wiederum von ManasCores {@code ManasRace} erbt) und liest ihre Werte aus
 * einer {@link AbyssRaceDef}.
 *
 * <p><b>Wichtig (1.21.1-Design):</b> Stats werden NICHT ueber getBaseHealth()-
 * Overrides gesetzt (die gibt es nicht mehr), sondern ueber
 * {@link ManasRace#addAttributeModifier} im Konstruktor. Nur die beiden
 * abstrakten Methoden {@link #getBaseAuraRange()} und
 * {@link #getBaseMagiculeRange()} muessen implementiert werden.
 */
public class AbyssRace extends TensuraRace {

    private static final String ATOMIC_CHARGE = "abyssAtomicCharge";
    private static final double ATOMIC_COST_FRACTION = 0.35;
    private static final int ATOMIC_CHARGE_TICKS = 40;
    private static final int ATOMIC_COOLDOWN_TICKS = 2400;
    private static final double ATOMIC_RADIUS = 30.0;

    private final AbyssRaceDef def;

    public AbyssRace(AbyssRaceDef def) {
        // Difficulty ist eine Enum von ManasRace: EASY / INTERMEDIATE / HARD / EXTREME.
        super(ManasRace.Difficulty.INTERMEDIATE);
        this.def = def;

        // Eindeutige Modifier-IDs pro Attribut+Rasse.
        ResourceLocation hpId  = rl(def.id() + "_hp");
        ResourceLocation atkId = rl(def.id() + "_atk");
        ResourceLocation spdId = rl(def.id() + "_spd");

        // baseHealth/attackDamage als additive Boni auf die Vanilla-Basiswerte.
        addAttributeModifier(Attributes.MAX_HEALTH, hpId, def.baseHealth(),
                AttributeModifier.Operation.ADD_VALUE);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, atkId, def.attackDamage(),
                AttributeModifier.Operation.ADD_VALUE);
        // Bewegung: Delta auf die Vanilla-Basis (0.1), damit die Ziel-Endgeschwindigkeit
        // dem TOML-Wert entspricht (statt ihn oben drauf zu addieren). Bei Bedarf tunen.
        addAttributeModifier(Attributes.MOVEMENT_SPEED, spdId, def.movementSpeed() - 0.1,
                AttributeModifier.Operation.ADD_VALUE);
        if (def.armorToughness() > 0) {
            addAttributeModifier(Attributes.ARMOR_TOUGHNESS, rl(def.id() + "_toughness"),
                    def.armorToughness(), AttributeModifier.Operation.ADD_VALUE);
        }
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("tensura_abyss", path);
    }

    /** Aura-Range (min..max) — abstrakt in TensuraRace. */
    @Override
    public Pair<Double, Double> getBaseAuraRange() {
        return Pair.of(def.aura() * 0.8, def.aura());
    }

    /** Magicule-Range (min..max) — abstrakt in TensuraRace. */
    @Override
    public Pair<Double, Double> getBaseMagiculeRange() {
        return Pair.of(def.magiculeBase() * 0.75, def.magiculeBase());
    }

    /** Anzeigename -> nutzt unseren Lang-Key tensura_abyss.race.&lt;id&gt;. */
    @Override
    public MutableComponent getName() {
        return Component.translatable("tensura_abyss.race." + def.id());
    }

    /**
     * The native reincarnation screen gives this component a compact text box,
     * so every entry leads with unique lore and follows with scannable facts.
     */
    @Override
    public MutableComponent getRaceDescription() {
        MutableComponent description = Component.translatable(
                "tensura_abyss.race." + def.id() + ".lore").withStyle(ChatFormatting.GRAY);
        description.append(Component.literal("\n"));
        description.append(Component.literal("Stage " + def.stage() + "/9  ")
                .withStyle(ChatFormatting.DARK_GRAY));
        description.append(Component.literal(String.format(Locale.US, "+%.0f HP  +%.1f ATK  %.3f SPD  +%.1f Toughness",
                        def.baseHealth(), def.attackDamage(), def.movementSpeed(), def.armorToughness()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        description.append(Component.literal("\nLow light: Night Vision + Speed. ")
                .withStyle(ChatFormatting.DARK_AQUA));
        if (def.stage() >= 5) {
            description.append(Component.literal("Shadow veil: capped absorption/evasion. ")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        String next = AbyssRaces.NEXT.get(def.id());
        description.append(Component.literal("\n"));
        if (next == null) {
            description.append(Component.literal("Final evolution.").withStyle(ChatFormatting.GOLD));
        } else {
            description.append(Component.literal("Evolves into ").withStyle(ChatFormatting.GRAY));
            description.append(Component.translatable("tensura_abyss.race." + next)
                    .withStyle(ChatFormatting.WHITE));
            description.append(Component.literal(" at " + String.format(Locale.US, "%,.0f", AbyssRaces.EVOLUTION_EP.get(next)) + " EP.")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (isAtomicRace()) {
            description.append(Component.literal("\nR: I Am Atomic (35% max Magicules, 120s cooldown).")
                    .withStyle(ChatFormatting.AQUA));
        }
        return description;
    }

    @Override
    public boolean canTick(ManasRaceInstance instance, LivingEntity entity) {
        return true;
    }

    /** Shared shadow traits. Effects are refreshed cheaply once every second. */
    @Override
    public void onTick(ManasRaceInstance instance, LivingEntity entity) {
        if (entity.level().isClientSide()) return;

        if (entity.tickCount % 20 == 0 && entity.level().getMaxLocalRawBrightness(entity.blockPosition()) < 7) {
            entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60,
                    def.stage() >= 7 ? 1 : 0, true, false));
        }

        if (isAtomicRace() && entity instanceof ServerPlayer player) {
            int charge = instance.getOrCreateTag().getInt(ATOMIC_CHARGE);
            if (charge > 0) {
                charge--;
                instance.getOrCreateTag().putInt(ATOMIC_CHARGE, charge);
                atomicChargeParticles(player.serverLevel(), player, charge);
                if (charge == 0) detonateAtomic(player);
            }
        }
    }

    /** Higher stages blunt some damage and occasionally dissolve completely into shadow. */
    @Override
    public boolean onHurt(ManasRaceInstance instance, LivingEntity entity,
                          DamageSource source, Changeable<Float> amount) {
        if (def.stage() < 5 || amount.isEmpty() || amount.get() <= 0) return true;
        float evadeChance = Math.min(0.10F, (def.stage() - 4) * 0.02F);
        if (!entity.level().isClientSide() && entity.getRandom().nextFloat() < evadeChance) {
            amount.set(0.0F);
            if (entity instanceof ServerPlayer player) {
                player.displayClientMessage(Component.literal("Shadow Evasion")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            return true;
        }
        float reduction = Math.min(0.12F, 0.025F * (def.stage() - 4));
        amount.set(amount.get() * (1.0F - reduction));
        return true;
    }

    @Override
    public boolean canActivateAbility(ManasRaceInstance instance, LivingEntity entity) {
        if (!isAtomicRace() || !(entity instanceof Player player) || instance.getOrCreateTag().getInt(ATOMIC_CHARGE) > 0) {
            return false;
        }
        double cost = atomicCost(player);
        boolean enough = player.getAbilities().instabuild || TensuraBridge.getMagicules(player) >= cost;
        if (!enough && player instanceof ServerPlayer) {
            player.displayClientMessage(Component.literal(
                    "I Am Atomic requires 35% of your maximum Magicule capacity.")
                    .withStyle(ChatFormatting.GRAY), true);
        }
        return enough;
    }

    /** Uses Tensura's own Race/Mount Ability key and packet (default R). */
    @Override
    public void onActivateAbility(ManasRaceInstance instance, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !isAtomicRace()) return;
        double cost = atomicCost(player);
        if (!player.getAbilities().instabuild) {
            TensuraBridge.setMagicules(player, Math.max(0, TensuraBridge.getMagicules(player) - cost));
        }
        instance.setCooldown(ATOMIC_COOLDOWN_TICKS);
        instance.getOrCreateTag().putInt(ATOMIC_CHARGE, ATOMIC_CHARGE_TICKS);
        player.displayClientMessage(Component.literal("I... AM...")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT,
                SoundSource.PLAYERS, 2.0F, 0.45F);
    }

    private boolean isAtomicRace() {
        return "eminence_of_the_abyss".equals(def.id());
    }

    private static double atomicCost(Player player) {
        return Math.max(0, EnergyHelper.getMaxMagicule(player) * ATOMIC_COST_FRACTION);
    }

    private static void atomicChargeParticles(ServerLevel level, ServerPlayer player, int charge) {
        if (charge % 4 != 0) return;
        double progress = 1.0 - charge / (double) ATOMIC_CHARGE_TICKS;
        double radius = 2.0 + progress * 10.0;
        int points = 24;
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0 * i / points;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX() + Math.cos(angle) * radius,
                    player.getY() + 0.25,
                    player.getZ() + Math.sin(angle) * radius,
                    1, 0, 0.12, 0, 0.01);
        }
    }

    private static void detonateAtomic(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 center = player.position();
        double capacity = EnergyHelper.getMaxMagicule(player);
        float damage = (float) Math.min(180.0, 60.0 + Math.sqrt(Math.max(0, capacity) / 1000.0) * 2.5);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(ATOMIC_RADIUS), target ->
                        target != player && target.isAlive() && !player.isAlliedTo(target)
                                && (target instanceof Enemy || target instanceof Player))) {
            double distance = Math.sqrt(target.distanceToSqr(center));
            float falloff = (float) Math.max(0.55, 1.0 - (distance / ATOMIC_RADIUS) * 0.45);
            target.hurt(level.damageSources().indirectMagic(player, player), damage * falloff);
        }

        for (double radius : new double[] {8, 16, 24, 30}) {
            int points = Math.max(32, (int) (radius * 4));
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2.0 * i / points;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        center.x + Math.cos(angle) * radius, center.y + 0.35,
                        center.z + Math.sin(angle) * radius, 1, 0.04, 0.25, 0.04, 0.08);
            }
        }
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1, center.z, 3, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1, center.z,
                140, 6, 1.5, 6, 0.06);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS, 6.0F, 0.5F);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 5.0F, 0.4F);
        player.displayClientMessage(Component.literal("ATOMIC.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);
    }

    // ═══════════════ NATIVE EVOLUTION TREE (dead-end fix) ═══════════════
    // Without these overrides ManasRace returns empty evolution lists and the
    // Tensura menu shows no further stages after picking a starter race.
    // Chains live in AbyssRaces.NEXT/PREV; EP gates in AbyssRaces.EVOLUTION_EP.

    /** Resolves a race id from our namespace against the live registry. */
    private static ManasRace resolve(String path) {
        if (path == null) return null;
        return RaceAPI.getRaceRegistry()
                .get(ResourceLocation.fromNamespaceAndPath("tensura_abyss", path));
    }

    @Override
    public List<ManasRace> getNextEvolutions(ManasRaceInstance instance, LivingEntity entity) {
        ManasRace next = resolve(AbyssRaces.NEXT.get(def.id()));
        return next == null ? List.of() : List.of(next);
    }

    @Override
    public List<ManasRace> getPreviousEvolutions(ManasRaceInstance instance, LivingEntity entity) {
        ManasRace prev = resolve(AbyssRaces.PREV.get(def.id()));
        return prev == null ? List.of() : List.of(prev);
    }

    @Override
    public ManasRace getDefaultEvolution(ManasRaceInstance instance, LivingEntity entity) {
        return resolve(AbyssRaces.NEXT.get(def.id()));
    }

    /**
     * Requirements to evolve INTO this race — rendered natively by the Tensura
     * menu (EP bar + requirement line). Uses Tensura's own
     * {@link EvolutionRequirement.EPRequirement}.
     */
    @Override
    public Map<EvolutionRequirement, Float> getEvolutionRequirements(ManasRaceInstance instance, LivingEntity entity) {
        double ep = AbyssRaces.EVOLUTION_EP.getOrDefault(def.id(), 0.0);
        if (ep <= 0) return Map.of();
        return Map.of(new EvolutionRequirement.EPRequirement(ep), 1.0F);
    }
}
