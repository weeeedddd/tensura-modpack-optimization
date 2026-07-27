package net.tensura.abyss.race;

/**
 * Kompakte, mod-unabhaengige Rassen-Definition — nur Zahlen, gemappt auf die
 * echten Tensura-Race-Felder:
 * <ul>
 *   <li>{@code baseHealth}    – physische HP  -&gt; Race#getBaseHealth()</li>
 *   <li>{@code attackDamage}  -&gt; Race#getBaseAttackDamage()</li>
 *   <li>{@code movementSpeed} -&gt; Race#getMovementSpeed() (+Sprint *1.45)</li>
 *   <li>{@code aura}          – Basis der Aura-Range (0.8..1.0)</li>
 *   <li>{@code magiculeBase}  – Basis der Magicule-Range (0.75..1.0);
 *       entspricht dem alten "spiritualHealth"</li>
 * </ul>
 */
public record AbyssRaceDef(
        String id,
        String path,
        int stage,
        double baseHealth,
        double attackDamage,
        double movementSpeed,
        double aura,
        double magiculeBase,
        double armorToughness
) {}
