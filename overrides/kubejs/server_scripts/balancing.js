// KubeJS 6 — Tensura Abyss: Super Diamond Progressions-System
//
// Ziel: Eine zentrale Endgame-Ressource ("Super Diamond") schaffen, die
// High-Tier-Ausrüstung reworkt/aufwertet und mächtige Fähigkeiten
// (Breath-System, fortgeschrittene Sprüche) freischaltet.
//
// VORAUSSETZUNG: kubejs/startup_scripts/super_diamond_items.js muss
// geladen sein (registriert kubejs:super_diamond + kubejs:breath_attunement_crystal).
//
// EINRICHTUNG: Ordner in "kubejs/server_scripts/" der Modpack-Instanz legen.
// Item-IDs prüfen: /kubejs hand im Spiel.

// ─────────────────────────────────────────────────────────────────────────────
// STUFE 1 — SUPER DIAMOND CRAFTEN
// ─────────────────────────────────────────────────────────────────────────────
// Kosten: 8 Diamanten + 1 Netherstern. Der Netherstern (Wither-Kill) sorgt
// dafür, dass Super Diamonds erst im Endgame verfügbar sind — kein Skip
// der Early-/Mid-Game-Progression möglich.
ServerEvents.recipes(event => {

  event.shaped('8x kubejs:super_diamond', [
    'DDD',
    'DND',
    'DDD'
  ], {
    D: 'minecraft:diamond',
    N: 'minecraft:nether_star'
  }).id('tensurapack:super_diamond_craft');

  // Optionaler zweiter Pfad über Tensura-Bossmaterial (günstiger, aber
  // Tensura-Progression gebunden). Item-ID mit /kubejs hand verifizieren,
  // dann Kommentar entfernen:
  //
  // event.shaped('4x kubejs:super_diamond', [
  //   'DDD',
  //   'DMD',
  //   'DDD'
  // ], {
  //   D: 'minecraft:diamond',
  //   M: 'tensura_magic_growth:magicule_crystal'   // <-- Tensura-Gate, ID prüfen
  // }).id('tensurapack:super_diamond_craft_tensura');

  // ── STUFE 2 — WAFFEN-REWORK ──
  // Verzaubert bestehende High-Tier-Waffen mit einem garantierten,
  // sonst nicht erreichbaren Verzauberungs-Paket. Verbraucht die Basis-Waffe.
  event.shapeless('minecraft:netherite_sword', [
    'minecraft:netherite_sword',
    '2x kubejs:super_diamond',
    'minecraft:nether_star'
  ])
    .enchant('minecraft:sharpness', 5)
    .enchant('minecraft:mending', 1)
    .enchant('minecraft:unbreaking', 3)
    .id('tensurapack:netherite_sword_rework');

  event.shapeless('minecraft:netherite_axe', [
    'minecraft:netherite_axe',
    '2x kubejs:super_diamond',
    'minecraft:nether_star'
  ])
    .enchant('minecraft:sharpness', 5)
    .enchant('minecraft:mending', 1)
    .id('tensurapack:netherite_axe_rework');

  // ── STUFE 2 — RÜSTUNGS-REWORK ──
  // Jedes Teil einzeln reworkbar. Protection IV + Unbreaking III + Mending
  // sind sonst im Pack durch Enchanting-Limits nicht kombinierbar.
  ['helmet', 'chestplate', 'leggings', 'boots'].forEach(piece => {
    event.shapeless(`minecraft:netherite_${piece}`, [
      `minecraft:netherite_${piece}`,
      '2x kubejs:super_diamond'
    ])
      .enchant('minecraft:protection', 4)
      .enchant('minecraft:unbreaking', 3)
      .enchant('minecraft:mending', 1)
      .id(`tensurapack:netherite_${piece}_rework`);
  });

  // ── STUFE 3 — BREATH-SYSTEM / SKILL-UNLOCK-TOKEN ──
  // Craftet den Verbrauchsgegenstand, der (via Right-Click, siehe unten)
  // fortgeschrittene Fähigkeiten freischaltet.
  event.shaped('kubejs:breath_attunement_crystal', [
    ' D ',
    'DND',
    ' D '
  ], {
    D: 'kubejs:super_diamond',
    N: 'minecraft:totem_of_undying'
  }).id('tensurapack:breath_attunement_crystal_craft');
});

// ─────────────────────────────────────────────────────────────────────────────
// BREATH-SYSTEM UNLOCK — RECHTSKLICK-VERBRAUCH
// ─────────────────────────────────────────────────────────────────────────────
// WICHTIG: Der auskommentierte runCommandSilent-Aufruf ist ein PLATZHALTER.
// Tensura: Magic Growth hat eigene Skillpoint-/Unlock-Commands — die exakte
// Syntax MUSS im Spiel geprüft werden (/tensura help oder Tensura-Wiki),
// bevor dieser Block aktiviert wird. Ohne Anpassung tut der Crystal aktuell
// nur: Item verbrauchen + Partikel/Sound-Feedback geben.
ItemEvents.rightClicked('kubejs:breath_attunement_crystal', event => {
  const { player, level, hand } = event;
  if (level.isClientSide) return;

  player.getInventory().removeItem(player.getItemInHand(hand), 1);
  level.playSound(null, player.blockPosition(), 'minecraft:entity.totem.use', 'players', 1.0, 1.0);

  // Platzhalter — Tensura-Command-Syntax vor Aktivierung verifizieren:
  // player.runCommandSilent('tensura skillpoint add @s 1');
  // player.runCommandSilent('tensura ultimateskill unlock @s breath_of_the_abyss');

  player.tell('§b§lBreath Attunement Crystal §rverbraucht §7(Skill-Unlock: siehe Kommentar in balancing.js)');
});
