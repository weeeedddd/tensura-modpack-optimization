// KubeJS 6 — Tensura Abyss: Slime-Suit Armor-Tier (NeoForge 1.21.1)
// Muss in startup_scripts/ liegen. Laedt vor shadow_garden_items.js
// (alphabetisch: "armor_tiers" < "items"), sodass die Items den Tier kennen.
//
// WICHTIG — WORN-TEXTUR:
// KubeJS loest die getragenen Armor-Layer automatisch ueber den Tier-Namen +
// Namespace auf:
//   kubejs:textures/models/armor/slime_suit_layer_1.png   (Helm, Brust, Stiefel)
//   kubejs:textures/models/armor/slime_suit_layer_2.png   (Hose)
// Genau diese Dateien liegen bereits im Repo -> die Slime-Optik wird am
// Spielerkoerper gerendert (ersetzt die Netherit-Optik).
//
// Stats = Netherit-Niveau (Slime Suit ist Endgame-Ruestung); der eigentliche
// Mehrwert ist der Stealth-Set-Bonus in shadow_garden.js.

ItemEvents.armorTierRegistry(event => {
  event.add('slime_suit', tier => {
    // Haltbarkeit: Basis-Faktor pro Slot (Netherit = 37)
    tier.durabilityMultiplier = 37

    // Schutzwerte je Slot: [FEET, LEGS, BODY/CHEST, HEAD] (Netherit = 3,6,8,3)
    tier.slotProtections = [3, 6, 8, 3]

    // Zaehigkeit & Knockback-Resistenz (Netherit-Werte)
    tier.toughness = 3.0
    tier.knockbackResistance = 0.1

    // Verzauberbarkeit (etwas hoeher als Netherit=15 fuer RPG-Flavor)
    tier.enchantmentValue = 16

    // Equip-Sound & Reparatur mit der Kern-Ressource
    tier.equipSound = 'minecraft:item.armor.equip_netherite'
    tier.repairIngredient = 'kubejs:dark_slime'
  })
})
