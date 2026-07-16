// KubeJS 6 — Tensura Abyss: Shadow-Abyss Portal-Block (NeoForge 1.21.1)
// startup_scripts/ — Block-Registry ist nur beim Spielstart moeglich.
//
// Registriert den Portal-Rahmen-Block, der in den Ruinen craftbar ist und
// per Rechtsklick mit Dunklem Aether die Dimension "Shadow Abyss" oeffnet.
//
// Textur liegt bereit: kubejs/assets/kubejs/textures/block/abyss_portal_frame.png
// (KubeJS erzeugt Model + Blockstate automatisch, cube_all).

StartupEvents.registry('block', event => {
  event.create('abyss_portal_frame')
    .displayName('Abyss Portal Frame')
    .material('stone')
    .hardness(25.0)
    .resistance(1200.0)
    .requiresTool(true)
    .tagBlock('minecraft:mineable/pickaxe')
    .tagBlock('minecraft:needs_diamond_tool')
    .lightLevel(0.4)
    .renderType('solid')
})
