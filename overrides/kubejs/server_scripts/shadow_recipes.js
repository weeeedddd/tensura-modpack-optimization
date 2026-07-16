// KubeJS 6 — Tensura Abyss: Shadow-Garden Rezepte (Create + Crafting)
// server_scripts/ — /kubejs reload server_scripts
//
// Item-Namespace: bei installierter Companion-Mod auf 'tensura_abyss' stellen.
const NS = 'kubejs'

// >>> ZU VERIFIZIEREN: exakte Item-IDs aus den installierten Mods <<<
const SOUL_ECHO    = 'tensura_ascensions:soul_echo' // Drop von Ascension-Bossen
const TENSURA_SLIME = 'tensura:slime'               // normaler Tensura-Schleim
const ABYSS_ESSENCE = 'minecraft:wither_rose'       // "Abyss-Essenz" (Platzhalter-Zutat)

ServerEvents.recipes(event => {

  // ── Dunkler Aether: 4x Soul Echo + 4x Dunkler Schleim + 1x Netherstern ──
  // (Shaped, damit die Anordnung eindeutig ist.)
  event.shaped(`2x ${NS}:dark_aether`, [
    'SES',
    'DND',
    'SES'
  ], {
    S: SOUL_ECHO,
    E: `${NS}:dark_slime`,
    D: `${NS}:dark_slime`,
    N: 'minecraft:nether_star'
  }).id('tensurapack:dark_aether_soulecho')

  // ── Dunkler Schleim via Create Mixing: Tensura-Schleim + Abyss-Essenz ──
  // Benoetigt Create + KubeJS-Create-Integration. Falls dein Build die
  // Methode anders benennt, siehe KubeJS-Create-Doku (createMixing).
  event.recipes.createMixing(`2x ${NS}:dark_slime`, [
    TENSURA_SLIME,
    TENSURA_SLIME,
    ABYSS_ESSENCE
  ]).heated()
    .id('tensurapack:dark_slime_mixing')

  // Fallback-Craft, falls Create/Tensura-IDs (noch) nicht vorhanden sind:
  // stellt Dunklen Schleim aus Vanilla-Zutaten her, damit die Kette nie blockt.
  event.shapeless(`2x ${NS}:dark_slime`, [
    'minecraft:slime_ball', 'minecraft:slime_ball', 'minecraft:slime_ball',
    'minecraft:wither_rose', 'minecraft:ink_sac'
  ]).id('tensurapack:dark_slime_fallback')
})
