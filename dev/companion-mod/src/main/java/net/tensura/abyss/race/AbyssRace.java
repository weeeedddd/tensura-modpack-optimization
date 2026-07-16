package net.tensura.abyss.race;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;

// ══════════════════════════════════════════════════════════════════════════
//  ECHTE Tensura-API (com.github.manasmods:tensura — via CurseMaven).
//  Verifiziert gegen die offiziellen Beispiel-Addons (BanditHelps/vel-mc/
//  ShinNoShinigami). ACHTUNG: Diese Beispiele sind 1.19.2/Forge. Die Race-
//  Basisklasse + Methodensignaturen sind das stabile Kern-Design und mit hoher
//  Wahrscheinlichkeit auf 1.21.1 identisch. Sollte 1.21.1 den Package-Root
//  geaendert haben, hier die drei Imports anpassen (sonst nichts).
// ══════════════════════════════════════════════════════════════════════════
import com.github.manasmods.tensura.race.Race;
import com.github.manasmods.tensura.ability.TensuraSkill;
import com.github.manasmods.tensura.util.JumpPowerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Eine parametrisierte Tensura-Rasse: EIN Subklassen-Typ, der seine Werte aus
 * einer {@link AbyssRaceDef} liest — so brauchen wir statt 37 Klassen nur
 * {@code new AbyssRace(def)}.
 *
 * <p>Basiert 1:1 auf dem echten Muster {@code ExampleRace extends Race} aus den
 * Tensura-Beispiel-Addons.
 *
 * <p><b>Wert-Mapping</b> (unsere TOML -> Tensura-API):
 * physicalHealth -> {@link #getBaseHealth()}, attackDamage ->
 * {@link #getBaseAttackDamage()}, movementSpeed -> {@link #getMovementSpeed()}
 * (+Sprint *1.45), aura -> {@link #getBaseAuraRange()} (0.8..1.0),
 * spiritualHealth -> {@link #getBaseMagiculeRange()} (0.75..1.0).
 * Tensura hat keinen eigenen "Spiritual Health"-Rassenwert; die Magicule-Range
 * ist der naechste Aequivalent-Pool.
 */
public class AbyssRace extends Race {

    private final AbyssRaceDef def;

    public AbyssRace(AbyssRaceDef def) {
        // 'Difficulty' ist eine verschachtelte Enum von Race (INTERMEDIATE ist
        // aus dem Beispiel bestaetigt). Weitere Konstanten erst nach Blick in
        // die 1.21.1-Jar nutzen.
        super(Difficulty.INTERMEDIATE);
        this.def = def;
    }

    @Override public double getBaseHealth()         { return def.baseHealth(); }
    @Override public double getBaseAttackDamage()   { return def.attackDamage(); }
    @Override public double getBaseAttackSpeed()    { return 3.0; }
    @Override public double getKnockbackResistance(){ return 0.0; }
    @Override public double getMovementSpeed()      { return def.movementSpeed(); }
    @Override public double getSprintSpeed()        { return def.movementSpeed() * 1.45; }
    @Override public float  getPlayerSize()         { return 2.0f; }

    @Override
    public double getJumpHeight() {
        // Sprunghoehe ist in Tensura ueber diesen Helfer gekapselt (wie im Beispiel).
        return JumpPowerHelper.defaultPlayer(1.0);
    }

    @Override
    public Pair<Double, Double> getBaseAuraRange() {
        return Pair.of(def.aura() * 0.8, def.aura());
    }

    @Override
    public Pair<Double, Double> getBaseMagiculeRange() {
        return Pair.of(def.magiculeBase() * 0.75, def.magiculeBase());
    }

    /**
     * Intrinsic-Skills der Rasse. Aktuell leer — hier spaeter eigene oder
     * vorhandene Tensura-Skills eintragen, z.B.
     * {@code list.add(IntrinsicSkills.BODY_ARMOR.get());}
     */
    @Override
    public List<TensuraSkill> getIntrinsicSkills(Player player) {
        return new ArrayList<>();
    }

    // ── Typ-Flags (Semantik gegen Tensura-Doku pruefen; Default = false) ──
    public boolean isMajin()     { return false; }
    public boolean isSpiritual() { return false; }
    public boolean isDivine()    { return false; }
}
