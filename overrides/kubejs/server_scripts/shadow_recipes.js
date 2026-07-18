// KubeJS 6 — Tensura Abyss: Shadow-Garden Rezepte (Create + Crafting)
// server_scripts/ — /kubejs reload server_scripts
//
// Item-Namespace: bei installierter Companion-Mod auf 'tensura_abyss' stellen.
const NS_REC = 'tensura_abyss'

// >>> ZU VERIFIZIEREN: exakte Item-IDs aus den installierten Mods <<<
const SOUL_ECHO    = 'minecraft:echo_shard'         // deep-dark echo (real item)
const TENSURA_SLIME = 'tensura:slime_chunk'         // Tensura slime material (verified id)
const ABYSS_ESSENCE = 'minecraft:wither_rose'       // "Abyss-Essenz" (Platzhalter-Zutat)

ServerEvents.recipes(event => {

  // ── Dunkler Aether: 4x Soul Echo + 4x Dunkler Schleim + 1x Netherstern ──
  // (Shaped, damit die Anordnung eindeutig ist.)
  event.shaped(`2x ${NS_REC}:dark_aether`, [
    'SES',
    'DND',
    'SES'
  ], {
    S: SOUL_ECHO,
    E: `${NS_REC}:dark_slime`,
    D: `${NS_REC}:dark_slime`,
    N: 'minecraft:nether_star'
  }).id('tensurapack:dark_aether_soulecho')

  // ── Dark Slime via Create Mixing (heated) ──
  // Written as a raw JSON recipe: the KubeJS-Create wrapper (createMixing /
  // .heated()) changed its API in 1.21 — event.custom is version-proof.
  event.custom({
    type: 'create:mixing',
    ingredients: [
      { item: TENSURA_SLIME },
      { item: TENSURA_SLIME },
      { item: ABYSS_ESSENCE }
    ],
    results: [{ id: `${NS_REC}:dark_slime`, count: 2 }],
    heat_requirement: 'heated'
  }).id('tensurapack:dark_slime_mixing')

  // Fallback-Craft, falls Create/Tensura-IDs (noch) nicht vorhanden sind:
  // stellt Dunklen Schleim aus Vanilla-Zutaten her, damit die Kette nie blockt.
  event.shapeless(`2x ${NS_REC}:dark_slime`, [
    'minecraft:slime_ball', 'minecraft:slime_ball', 'minecraft:slime_ball',
    'minecraft:wither_rose', 'minecraft:ink_sac'
  ]).id('tensurapack:dark_slime_fallback')
})
