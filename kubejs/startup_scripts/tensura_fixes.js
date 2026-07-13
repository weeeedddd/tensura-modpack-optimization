// KubeJS 6 — Tensura Abyss: Startup Fixes & Tag Assignments
// Läuft einmal beim Spiel-Start (vor dem Laden der Welt).
// Hier: Cross-Mod-Tags setzen, damit Mods sich gegenseitig erkennen.

// ─────────────────────────────────────────────────────────────────────────────
// ITEM TAG ASSIGNMENTS
// Sorgt dafür, dass Tensura-Items in Create/anderen Mods als korrekte
// Ressourcen erkannt werden (z.B. als "Slimeball" für Rezepte).
// ─────────────────────────────────────────────────────────────────────────────
StartupEvents.tags('item', event => {

  // Beispiele — deaktiviere Kommentare nach ID-Verifikation mit /kubejs hand:

  // Tensura Slime-Materialien als forge:slimeballs registrieren
  // (damit Create-Rezepte sie akzeptieren)
  // event.add('forge:slimeballs', 'tensura_magic_growth:slime_ball');
  // event.add('forge:slimeballs', 'tensura_magic_growth:dark_slime_ball');

  // Tensura-Edelsteine als forge:gems/* registrieren
  // event.add('forge:gems', 'tensura_magic_growth:magic_crystal');

  // Tensura-Ingots für Create-Rezepte
  // event.add('forge:ingots', 'tensura_magic_growth:magicule_ingot');
});

// ─────────────────────────────────────────────────────────────────────────────
// BLOCK TAG ASSIGNMENTS
// ─────────────────────────────────────────────────────────────────────────────
StartupEvents.tags('block', event => {
  // Beispiel: Tensura-Blöcke als mineable/pickaxe registrieren
  // event.add('minecraft:mineable/pickaxe', 'tensura_magic_growth:magicule_ore');
});
