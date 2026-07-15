// KubeJS 6 — Tensura Abyss: Shadow-Abyss Portal-Logik (NeoForge 1.21.1)
// server_scripts/ — hot-reloadbar mit /kubejs reload server_scripts
//
// - Rezept fuer den Portal-Rahmen (in der Ruine craftbar)
// - Rechtsklick auf den Rahmen mit "Dunklem Aether" -> Reise nach Shadow Abyss
//   (bzw. zurueck in die Overworld, wenn man bereits drin ist)
//
// Teleport laeuft ueber /execute in <dim> run tp @s ~ <y> ~  (dimensionssicher,
// versionsstabil). Keine fragilen Java-Mappings.

const ABYSS_DIM = 'tensura_abyss:shadow_abyss'
const OVERWORLD = 'minecraft:overworld'
const ABYSS_Y   = 120   // sichere Einstiegshoehe in der Abyss
const RETURN_Y  = 110   // Ruecksprunghoehe in der Overworld

// ── Rezept: Portal-Rahmen ──
ServerEvents.recipes(event => {
  event.shaped('4x kubejs:abyss_portal_frame', [
    'OCO',
    'CAC',
    'OCO'
  ], {
    O: 'minecraft:obsidian',
    C: 'minecraft:crying_obsidian',
    A: 'kubejs:dark_aether'
  }).id('tensurapack:abyss_portal_frame')
})

// ── Portal aktivieren / Reisen ──
BlockEvents.rightClicked('kubejs:abyss_portal_frame', event => {
  const { player, item, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return

  // Nur mit Dunklem Aether in der Hand
  if (!item || item.id !== 'kubejs:dark_aether') {
    player.tell(Text.gray('Der Rahmen verlangt §5Dunklen Aether§7, um zu erwachen.'))
    return
  }

  // aktuelle Dimension bestimmen (robust, mit Fallback)
  let here = ''
  try { here = String(player.level.dimension.location()) } catch (e) { here = '' }
  const inAbyss = here === ABYSS_DIM

  // 1x Dunklen Aether verbrauchen
  if (!player.isCreative()) item.shrink(1)

  const target = inAbyss ? OVERWORLD : ABYSS_DIM
  const y = inAbyss ? RETURN_Y : ABYSS_Y

  // Effekte am Ausgangspunkt
  player.runCommandSilent('playsound minecraft:block.beacon.activate master @a ~ ~ ~ 4 0.7')
  player.runCommandSilent('particle minecraft:soul_fire_flame ~ ~1 ~ 0.6 1.2 0.6 0.03 200 force')
  player.runCommandSilent('particle minecraft:reverse_portal ~ ~1 ~ 0.5 1.0 0.5 0.1 120 force')

  // dimensionssicherer Teleport (behaelt X/Z, setzt sichere Y-Hoehe)
  player.runCommandSilent(`execute in ${target} run tp @s ~ ${y} ~`)

  if (inAbyss) {
    player.tell(Text.aqua('Du kehrst aus dem §3Shadow Abyss§b in die Overworld zurueck.'))
  } else {
    player.tell(Text.aqua('Das Portal erwacht — willkommen im §5§lShadow Abyss§r§b.'))
    player.tell(Text.gray('Ein sicherer Landeplatz wurde gewaehlt. Vorsicht vor den Bewohnern.'))
  }
})
