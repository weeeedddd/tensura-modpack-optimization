// KubeJS 6 — Tensura Abyss: Server-Side Balancing
// Ziel: Mod-Progressionen harmonisieren, Loot skalieren, Rezepte gaten.
//
// EINRICHTUNG: Diese Datei in den "kubejs/server_scripts/" Ordner deiner
// Modpack-Instanz legen. KubeJS muss installiert sein.
//
// Item-IDs prüfen: Im Spiel /kubejs hand eingeben, um die ID des
// gehaltenen Items zu sehen.

// ─────────────────────────────────────────────────────────────────────────────
// LOOT TABLE ANPASSUNGEN
// ─────────────────────────────────────────────────────────────────────────────
// HINWEIS: 'ServerEvents.lootTables' existiert in KubeJS 1.21.1 (2101.x) nicht mehr.
// Die Loot-API wurde aufgeteilt in ServerEvents.entityLootTables / .blockLootTables /
// .chestLootTables. Der folgende Block war reines Platzhalter-Scaffolding
// (Dummy-Drops) und ist deaktiviert, damit das Skript fehlerfrei laedt.
// Zum Reaktivieren mit echten Tensura-Items auf die neue API portieren:
//
// ServerEvents.entityLootTables(event => {
//   event.modifyEntity('iceandfire:ice_dragon', table => {
//     table.addPool(pool => {
//       pool.rolls(1);
//       pool.addItem('tensura_magic_growth:slime_core').weight(3);
//     });
//   });
// });

// ─────────────────────────────────────────────────────────────────────────────
// REZEPT-ANPASSUNGEN (Progression-Gating)
// ─────────────────────────────────────────────────────────────────────────────
ServerEvents.recipes(event => {

  // ── Create: Hochrangige Rezepte an Tensura-Fortschritt koppeln ──
  // Deaktiviere die Kommentare sobald du die korrekten Item-IDs kennst.
  // Item-IDs findest du mit: /kubejs hand (hält das Item in der Hand)

  // Beispiel: Mächtiger Create-Gegenstand braucht Tensura-Kernmaterial
  // event.remove({ id: 'create:brass_ingot' });
  // event.shapeless('create:brass_ingot', [
  //   'create:zinc_ingot',
  //   'create:copper_ingot',
  //   'tensura_magic_growth:slime_core'   // <-- Tensura-Progression-Gate
  // ]).id('tensurapack:brass_ingot_gated');

  // Beispiel: MineColonies-Gebäude brauchen Tensura-Ressource ab Tier 3+
  // event.remove({ id: 'minecolonies:supply_camp' });
  // event.shaped('minecolonies:supply_camp', [
  //   'AAA',
  //   'ABA',
  //   'AAA'
  // ], {
  //   A: '#forge:planks',
  //   B: 'tensura_magic_growth:slime_core'   // Tensura-Gate
  // }).id('tensurapack:supply_camp_gated');

  // ── Einfache Balance-Tweaks ──

  // Gateways to Eternity: Catalyst billiger machen für Early-Game-Boss-Fights
  // event.remove({ id: 'gateways:gateway_pearl' });
  // event.shaped('gateways:gateway_pearl', [' A ', 'ABA', ' A '], {
  //   A: 'minecraft:ender_pearl',
  //   B: 'minecraft:blaze_rod'
  // }).id('tensurapack:gateway_pearl_cheaper');
});

// ─────────────────────────────────────────────────────────────────────────────
// ENTITY ATTRIBUTE ANPASSUNGEN (Schaden / HP Balancing)
// ─────────────────────────────────────────────────────────────────────────────
// HINWEIS: 'ServerEvents.entitySpawned' existiert in KubeJS 1.21.1 nicht.
// Dieser Block war leeres Scaffolding (nur Kommentare, keine Wirkung) und ist
// deaktiviert. EP Scaling uebernimmt ohnehin die Grundskalierung der Mobs.
// Fuer Spawn-basiertes Feintuning bei Bedarf die NeoForge-Events der jeweiligen
// Mod nutzen (z. B. ueber die Companion-Mod), nicht KubeJS.
