// KubeJS 6 — Tensura Abyss: Shadow-Abyss Portal-Logik (NeoForge 1.21.1)
// server_scripts/ — hot-reloadbar mit /kubejs reload server_scripts
//
// - Rezept fuer den Portal-Rahmen (in der Ruine craftbar)
// - Rechtsklick auf den Rahmen mit "Dunklem Aether" -> Reise nach Shadow Abyss
//   (bzw. zurueck in die Overworld, wenn man bereits drin ist)
//
// Teleport laeuft ueber /execute in <dim> run tp @s ~ <y> ~  (dimensionssicher,
// versionsstabil). Keine fragilen Java-Mappings.

const ABYSS_DIM_PORTAL = 'tensura_abyss:shadow_abyss'
const OVERWORLD = 'minecraft:overworld'
const ABYSS_Y   = 120   // sichere Einstiegshoehe in der Abyss
const RETURN_Y  = 110   // Ruecksprunghoehe in der Overworld

// ── Rezept: Portal-Rahmen ──
ServerEvents.recipes(event => {
  event.shaped('4x tensura_abyss:abyss_portal_frame', [
    'OCO',
    'CAC',
    'OCO'
  ], {
    O: 'minecraft:obsidian',
    C: 'minecraft:crying_obsidian',
    A: 'tensura_abyss:dark_aether'
  }).id('tensurapack:abyss_portal_frame')
})

// ── Portal aktivieren / Reisen ──
BlockEvents.rightClicked('tensura_abyss:abyss_portal_frame', event => {
  const { player, item, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return

  // Nur mit Dunklem Aether in der Hand
  if (!item || item.id !== 'tensura_abyss:dark_aether') {
    player.tell(Text.gray('The frame demands §5Dark Aether§7 to awaken.'))
    return
  }

  // aktuelle Dimension bestimmen (robust, mit Fallback)
  let here = ''
  try { here = String(player.level.dimension.location()) } catch (e) { here = '' }
  const inAbyss = here === ABYSS_DIM_PORTAL

  // 1x Dunklen Aether verbrauchen
  if (!player.isCreative()) item.shrink(1)

  const target = inAbyss ? OVERWORLD : ABYSS_DIM_PORTAL
  const y = inAbyss ? RETURN_Y : ABYSS_Y

  // Effekte am Ausgangspunkt
  player.runCommandSilent('playsound minecraft:block.beacon.activate master @a ~ ~ ~ 4 0.7')
  player.runCommandSilent('particle minecraft:soul_fire_flame ~ ~1 ~ 0.6 1.2 0.6 0.03 200 force')
  player.runCommandSilent('particle minecraft:reverse_portal ~ ~1 ~ 0.5 1.0 0.5 0.1 120 force')

  // dimensionssicherer Teleport (behaelt X/Z, setzt sichere Y-Hoehe)
  player.runCommandSilent(`execute in ${target} run tp @s ~ ${y} ~`)

  if (inAbyss) {
    player.tell(Text.aqua('You return from the §3Shadow Abyss§b to the Overworld.'))
  } else {
    player.tell(Text.aqua('The portal awakens — welcome to the §5§lShadow Abyss§r§b.'))
    player.tell(Text.gray('A safe landing spot was chosen. Beware of the inhabitants.'))
  }
})
