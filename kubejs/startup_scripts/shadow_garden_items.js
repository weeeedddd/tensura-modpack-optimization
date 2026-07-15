// KubeJS 6 — Tensura Abyss: "The Eminence in Shadow" Item-Registry
// MC 1.21.1 / NeoForge. Muss in startup_scripts/ liegen (Item-Registry
// ist nur beim Spielstart moeglich, NICHT hot-reloadbar).
//
// Registriert die Endgame-Ressourcen, den Ultimate-Skill-Katalysator,
// Kult-Drops, das Mitsugoshi-Werkzeug und die Slime-Suit-Ruestung.
//
// HINWEIS ZU TEXTUREN: Custom Items erscheinen ohne PNG als schwarz/pink.
// Optional Texturen ablegen unter:
//   kubejs/assets/kubejs/textures/item/<id>.png
// Die Funktionalitaet (Werte, Set-Bonus, Skill) ist davon unabhaengig.

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
  // .tier('netherite') = Netherit-Grundwerte (Schutz/Haltbarkeit).
  event.create('slime_suit_helmet', 'helmet').tier('netherite')
    .displayName('Slime Suit Mask').glow(true)
  event.create('slime_suit_chestplate', 'chestplate').tier('netherite')
    .displayName('Slime Suit Coat').glow(true)
  event.create('slime_suit_leggings', 'leggings').tier('netherite')
    .displayName('Slime Suit Leggings').glow(true)
  event.create('slime_suit_boots', 'boots').tier('netherite')
    .displayName('Slime Suit Boots').glow(true)

})
