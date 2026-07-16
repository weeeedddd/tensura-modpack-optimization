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
ServerEvents.lootTables(event => {

  // Ice and Fire CE: Drachen droppen Tensura-relevante Items
  // Macht Drachen-Kämpfe für die gesamte Spielphase relevant
  event.modifyEntity('iceandfire:ice_dragon', table => {
    table.addPool(pool => {
      pool.rolls(1);
      pool.addItem('minecraft:diamond').weight(3);             // Platzhalter — ersetze mit Tensura-Item
      // pool.addItem('tensura_magic_growth:slime_core').weight(3);
    });
  });

  event.modifyEntity('iceandfire:fire_dragon', table => {
    table.addPool(pool => {
      pool.rolls(1);
      pool.addItem('minecraft:blaze_powder').weight(5);        // Platzhalter
      // pool.addItem('tensura_magic_growth:flame_essence').weight(5);
    });
  });

  // Cataclysm-Bosse: Garantierter Tensura-Progression-Drop
  // Stelle sicher, dass Tensura-Fortschritt aus Boss-Kills kommt
  event.modifyEntity('iceandfire:death_worm', table => {
    table.addPool(pool => {
      pool.rolls(1);
      pool.addItem('minecraft:bone').weight(8);               // Platzhalter
    });
  });
});

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
ServerEvents.entitySpawned(event => {
  const e = event.entity;

  // EP Scaling übernimmt die Grundskalierung.
  // Hier nur Feintuning für spezifische Mobs die zu stark/schwach sind.

  // Ice and Fire Drachen: Leicht stärker für Mid-/Late-Game Challenge
  if (e.type === 'iceandfire:ice_dragon' || e.type === 'iceandfire:fire_dragon') {
    // Nur in Welten mit hohem Spieler-Level anwenden
    // Basiswerte werden von EP Scaling gesetzt
  }
});
