// KubeJS — Tensura Abyss: Startup-Hinweise
// Laeuft einmal beim Spiel-Start.
//
// HINWEIS ZU TAGS: In KubeJS 2101 (1.21.1) gibt es KEIN 'StartupEvents.tags'.
// Item-/Block-Tags werden in SERVER-SCRIPTS gesetzt, ueber:
//     ServerEvents.tags('item',  event => { event.add('c:slimeballs', 'id') })
//     ServerEvents.tags('block', event => { event.add('minecraft:mineable/pickaxe', 'id') })
// (In 1.21 heisst der Sammel-Namespace 'c:' statt 'forge:', z.B. 'c:slimeballs'.)
//
// Beispiele — bei Bedarf in eine server_scripts/-Datei uebernehmen und die
// realen IDs mit /kubejs hand verifizieren:
//   event.add('c:slimeballs', 'tensura:slime')
//   event.add('c:gems',       'tensura_magic_growth:magic_crystal')
//   event.add('minecraft:mineable/pickaxe', 'tensura_magic_growth:magicule_ore')

// (Aktuell keine Startup-Aktionen noetig — Items/Bloecke registriert die
//  Companion-Mod 'tensura_abyss' in Java.)
