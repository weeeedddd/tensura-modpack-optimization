// KubeJS 6 — Tensura Abyss: "The Eminence in Shadow" Item-Registry
// MC 1.21.1 / NeoForge. Muss in startup_scripts/ liegen (Item-Registry
// ist nur beim Spielstart moeglich, NICHT hot-reloadbar).
//
// Registriert die Endgame-Ressourcen, den Ultimate-Skill-Katalysator,
// Kult-Drops, das Mitsugoshi-Werkzeug und die Slime-Suit-Ruestung.
//
// TEXTUREN: Basis-Item-Icons sind BEREITS enthalten unter
//   kubejs/assets/kubejs/textures/item/<id>.png
// (KubeJS nutzt diese automatisch, kein Model-JSON noetig.)
//
// Getragene Ruestung: .tier('slime_suit') (siehe shadow_garden_armor_tiers.js)
// rendert am Koerper die Slime-Layer unter
//   kubejs/assets/kubejs/textures/models/armor/slime_suit_layer_1.png (+ _2)
// Alle Texturen neu erzeugen mit: python3 tools/gen_textures.py

StartupEvents.registry('item', event => {

  // ── Endgame-Ressourcen / Endgame resources ──
  event.create('dark_slime')
    .displayName('Refined Dark Slime')
    .rarity('rare').glow(true).maxStackSize(64)

  event.create('dark_aether')
    .displayName('Dark Aether')
    .rarity('epic').glow(true).maxStackSize(64)

  // ── Ultimate Skill "I Am Atomic" (Verbrauchs-Katalysator) ──
  event.create('i_am_atomic_catalyst')
    .displayName('I Am Atomic')
    .rarity('epic').glow(true).maxStackSize(1)

  // ── Kult von Diablos: Drop + Fraktions-Werkzeuge ──
  event.create('cult_insignia')
    .displayName('Cult of Diablos Insignia')
    .rarity('uncommon').maxStackSize(64)

  event.create('mitsugoshi_ledger')
    .displayName('Mitsugoshi Trade Ledger')
    .rarity('rare').glow(true).maxStackSize(1)

  event.create('shadow_pledge_note')
    .displayName('Shadow Garden Pledge')
    .rarity('uncommon').maxStackSize(16)

  // ── Slime Suit (Stealth-Ruestung, Set-Bonus via shadow_garden.js) ──
  // .tier('slime_suit') = eigener Armor-Tier (siehe shadow_garden_armor_tiers.js),
  // rendert die Slime-Layer am Koerper statt der Netherit-Optik.
  event.create('slime_suit_helmet', 'helmet').tier('slime_suit')
    .displayName('Slime Suit Mask').glow(true)
  event.create('slime_suit_chestplate', 'chestplate').tier('slime_suit')
    .displayName('Slime Suit Coat').glow(true)
  event.create('slime_suit_leggings', 'leggings').tier('slime_suit')
    .displayName('Slime Suit Leggings').glow(true)
  event.create('slime_suit_boots', 'boots').tier('slime_suit')
    .displayName('Slime Suit Boots').glow(true)

})
