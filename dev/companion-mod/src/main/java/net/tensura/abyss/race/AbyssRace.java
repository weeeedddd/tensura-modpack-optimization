package net.tensura.abyss.race;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// ── ECHTE Tensura/ManasCore-API (1.21.1), verifiziert per javap gegen die
//    heruntergeladenen Jars (io.github.manasmods — NICHT com.github!). ──
import io.github.manasmods.tensura.race.TensuraRace;
import io.github.manasmods.tensura.race.template.EvolutionRequirement;
import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.RaceAPI;
import net.minecraft.world.entity.LivingEntity;
import net.tensura.abyss.registry.AbyssRaces;

import java.util.List;
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
