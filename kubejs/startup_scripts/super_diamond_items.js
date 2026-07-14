// KubeJS 6 — Tensura Abyss: Custom Items für das Super-Diamond-System
// Muss in startup_scripts liegen (Item-Registry ist nur beim Spielstart möglich).
//
// Erzeugt:
//   kubejs:super_diamond              — Kern-Ressource für Rework/Unlock
//   kubejs:breath_attunement_crystal  — Verbrauchsgegenstand für Skill-Unlocks

StartupEvents.registry('item', event => {

  event.create('super_diamond')
    .displayName('Super Diamond')
    .maxStackSize(64)
    .rarity('epic')
    .glow(true);

  event.create('breath_attunement_crystal')
    .displayName('Breath Attunement Crystal')
    .maxStackSize(16)
    .rarity('epic')
    .glow(true);

});
