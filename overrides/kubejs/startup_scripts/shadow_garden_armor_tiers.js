// KubeJS 6 — Tensura Abyss: Slime-Suit Armor-Tier (NeoForge 1.21.1)
// Muss in startup_scripts/ liegen. Laedt vor shadow_garden_items.js
// (alphabetisch: "armor_tiers" < "items").
//
// ┌──────────────────────────────────────────────────────────────────────┐
// │ ZWEI VARIANTEN — nutze GENAU EINE. Wenn die Slime-Optik am Koerper    │
// │ nicht erscheint, kommentiere Variante A aus und Variante B ein.       │
// └──────────────────────────────────────────────────────────────────────┘
//
// Beide erwarten die Layer-Texturen (liegen bereits im Repo):
//   kubejs/assets/kubejs/textures/models/armor/slime_suit_layer_1.png  (Helm/Brust/Stiefel)
//   kubejs/assets/kubejs/textures/models/armor/slime_suit_layer_2.png  (Hose)
//
// Stats = Netherit-Niveau; der eigentliche Mehrwert ist der Stealth-Set-Bonus
// in shadow_garden.js.

// gemeinsame Stat-Funktion (DRY)
function applySlimeStats(tier) {
  tier.durabilityMultiplier = 37
  tier.slotProtections = [3, 6, 8, 3]   // [FEET, LEGS, BODY/CHEST, HEAD] (Netherit)
  tier.toughness = 3.0
  tier.knockbackResistance = 0.1
  tier.enchantmentValue = 16
  tier.equipSound = 'minecraft:item.armor.equip_netherite'
  tier.repairIngredient = 'kubejs:dark_slime'
}

// ═══════════════════════════════════════════════════════════════════════
// VARIANTE A — STANDARD (Tier-Name-Aufloesung)  ◀ AKTIV
// KubeJS loest die Layer automatisch ueber Namespace + Tier-Name auf:
//   kubejs:textures/models/armor/slime_suit_layer_1.png (+ _2)
// Funktioniert auf den meisten KubeJS-6-Builds fuer 1.21.1.
// ═══════════════════════════════════════════════════════════════════════
ItemEvents.armorTierRegistry(event => {
  event.add('slime_suit', tier => {
    applySlimeStats(tier)
    // (kein Textur-Setter noetig — Layer via Tier-Name)
  })
})

// ═══════════════════════════════════════════════════════════════════════
// VARIANTE B — EXPLIZITER LAYER-/ASSET-SETTER (neuere 1.21.1-Builds)
// Falls Variante A die Layer NICHT rendert: Variante A oben auskommentieren
// und diesen Block einkommentieren. Nutzt den expliziten Equipment-Asset,
// wie ihn neuere KubeJS-/NeoForge-1.21.1-Builds erwarten.
// ═══════════════════════════════════════════════════════════════════════
/*
ItemEvents.armorTierRegistry(event => {
  event.add('slime_suit', tier => {
    applySlimeStats(tier)
    // Verweist explizit auf den Equipment-Asset "kubejs:slime_suit".
    // Minecraft 1.21.1 sucht die Layer dann unter:
    //   kubejs:textures/models/armor/slime_suit_layer_1.png (+ _2)
    tier.assetId('kubejs:slime_suit')
  })
})
*/

// ── Hinweis: Item-Builder-Route (dritte Moeglichkeit) ──
// Manche Builds bieten den Setter direkt am Ruestungs-ITEM statt am Tier an.
// Dann in shadow_garden_items.js an jedes Teil anhaengen, z.B.:
//   event.create('slime_suit_helmet', 'helmet').tier('slime_suit')
//       .assetId('kubejs:slime_suit')      // ODER: .client(c => c.armorTexture('kubejs:slime_suit'))
// Nur EINE der drei Routen (A, B oder Item-Builder) gleichzeitig verwenden.
