// KubeJS 6 — Tensura Abyss: HARDCORE Evolutions-Gating (8-Stufen-System)
// server_scripts/ — /kubejs reload server_scripts
//
// Koppelt an Tensura via net.tensura.abyss.bridge.TensuraBridge (mit lautlosem
// Scoreboard-Fallback). Erzwingt 8 eigenstaendige Evolutionsstufen (9 Zustaende
// inkl. Startform) mit EXPONENTIELL steigenden Magicule-Kosten und harten
// Bedingungen — verhindert, dass Spieler zu schnell brutal stark werden.
//
// Ausloeser: SNEAK + Rechtsklick mit Dunklem Aether.
//   (Normaler Rechtsklick mit Dunklem Aether = Breath, shadow_garden.js)
//   (Rechtsklick mit Dunklem Schleim = +Magicules & Schleim-Meisterschaft)

const NS = 'kubejs'                 // bei Companion-Mod: 'tensura_abyss'
const ABYSS_DIM = 'tensura_abyss:shadow_abyss'

// ── Java-Bridge (optional) mit Fallback ──
let BRIDGE = null
try { BRIDGE = Java.loadClass('net.tensura.abyss.bridge.TensuraBridge') } catch (e) { BRIDGE = null }
function getMagicules(p) {
  if (BRIDGE) { try { return BRIDGE.getMagicules(p) } catch (e) {} }
  return p.runCommandSilent('scoreboard players get @s sg_magicule')
}
function setRace(p, path) {
  if (BRIDGE) { try { return BRIDGE.setTensuraRace(p, path) } catch (e) {} }
  return false
}
function isSneaking(p) {
  try { return p.isCrouching() } catch (e) { try { return p.isShiftKeyDown() } catch (e2) { return false } }
}
function count(p, id) { return p.runCommandSilent(`clear @s ${id} 0`) }

// ── Bedingungs-Prueffunktionen (persistentData-Flags + Fallbacks) ──
function flag(p, key) { return p.persistentData.getBoolean(key) }
function condCultLeader(p) {
  return flag(p, 'sgKilledCultLeader') ||
         p.runCommandSilent('execute if score @s sg_cult_leader matches 1..') >= 1
}
function condDarkSlime(p) {
  return p.persistentData.getInt('sgSlimeMastery') >= 1
}
function condAbyss(p) {
  if (flag(p, 'sgEnteredAbyss')) return true
  // Fallback: aktuell in der Abyss?
  let here = ''
  try { here = String(p.level.dimension.location()) } catch (e) {}
  if (here === ABYSS_DIM) { p.persistentData.putBoolean('sgEnteredAbyss', true); return true }
  return false
}

// ── Rang-Leiter: 9 Zustaende, exponentielle Magicule-Kosten ──
// cond: optionale harte Bedingung (fn) + condMsg fuer Chat-Feedback.
const RANKS = [
  { name: 'Possessed',            race: 'tensura_abyss:possessed',            magicule: 0,       aether: 0, slime: 0 },
  { name: 'Awakened',             race: 'tensura_abyss:awakened',             magicule: 3000,    aether: 1, slime: 0 },
  { name: 'Shadow Garden Member', race: 'tensura_abyss:shadow_garden_member', magicule: 8000,    aether: 2, slime: 1 },
  { name: 'Numbers',              race: 'tensura_abyss:numbers',              magicule: 20000,   aether: 3, slime: 2,
    cond: condCultLeader, condMsg: 'Besiege zuerst einen §5Diablos-Kult-Anfuehrer§7 (Diablos-Ritter).' },
  { name: 'Rogue',                race: 'tensura_abyss:rogue',                magicule: 50000,   aether: 4, slime: 3 },
  { name: 'Elite Shadow',         race: 'tensura_abyss:elite_shadow',         magicule: 120000,  aether: 5, slime: 4 },
  { name: 'Seven Shadows Aspirant', race: 'tensura_abyss:seven_shadows_aspirant', magicule: 300000, aether: 6, slime: 6,
    cond: condDarkSlime, condMsg: 'Stelle zuerst §3Dunklen Schleim§7 her und nutze ihn (Rechtsklick).' },
  { name: 'Seven Shadows',        race: 'tensura_abyss:seven_shadows',        magicule: 750000,  aether: 8, slime: 8 },
  { name: 'Shadow',               race: 'tensura_abyss:shadow',               magicule: 2000000, aether: 12, slime: 12,
    cond: condAbyss, condMsg: 'Betrete zuerst den §5§lShadow Abyss§r§7 (Aether-Portal).' }
]
const TEAMS = ['sg_shadow','sg_shadow','sg_numbers','sg_numbers','sg_numbers','sg_seven','sg_seven','sg_seven','sg_lord']

ServerEvents.loaded(event => {
  event.server.runCommandSilent('scoreboard objectives add sg_magicule dummy "Magicule"')
  event.server.runCommandSilent('scoreboard objectives add sg_cult_leader dummy "CultLeader"')
})

// ═══════════════════════ EVOLUTION ═══════════════════════
ItemEvents.rightClicked(`${NS}:dark_aether`, event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  if (!isSneaking(player)) return

  const idx = player.persistentData.getInt('sgEvoRank')
  if (idx >= RANKS.length - 1) {
    player.tell(Text.gold(`§lMaximale Form erreicht: [${RANKS[idx].name}].`))
    return
  }
  const next = RANKS[idx + 1]

  // 1) exponentielles Magicule-Gate
  const mag = getMagicules(player)
  if (mag < next.magicule) {
    player.tell(Text.red(`✖ Evolution [${next.name}] gesperrt.`))
    player.tell(Text.gray(`Magicule: ${Math.round(mag)} / ${next.magicule} (${percent(mag, next.magicule)}%).`))
    return
  }

  // 2) harte Bedingung
  if (next.cond && !next.cond(player)) {
    player.tell(Text.red(`✖ Bedingung nicht erfuellt fuer [${next.name}].`))
    player.tell(Text.gray('» ' + next.condMsg))
    return
  }

  // 3) Katalysator-Gate
  const haveA = count(player, `${NS}:dark_aether`)
  const haveS = next.slime > 0 ? count(player, `${NS}:dark_slime`) : 0
  if (haveA < next.aether || haveS < next.slime) {
    player.tell(Text.red('✖ Katalysator fehlt.'))
    player.tell(Text.gray(`Benoetigt: ${next.aether}x Dunkler Aether` +
      (next.slime > 0 ? ` + ${next.slime}x Dunkler Schleim` : '') +
      ` (hast ${haveA}/${haveS}).`))
    return
  }

  // ── alles erfuellt ──
  if (!player.isCreative()) {
    if (next.aether > 0) player.runCommandSilent(`clear @s ${NS}:dark_aether ${next.aether}`)
    if (next.slime > 0)  player.runCommandSilent(`clear @s ${NS}:dark_slime ${next.slime}`)
  }
  player.persistentData.putInt('sgEvoRank', idx + 1)
  player.runCommandSilent(`team join ${TEAMS[idx + 1]} @s`)
  const evolved = setRace(player, next.race)

  // Evolutions-Inszenierung (purpur/schwarz)
  player.runCommandSilent('playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 4 0.6')
  player.runCommandSilent('particle minecraft:dragon_breath ~ ~1 ~ 0.7 1.2 0.7 0.02 220 force')
  player.runCommandSilent('particle minecraft:witch ~ ~1 ~ 0.7 1.2 0.7 0.1 140 force')
  player.runCommandSilent('effect give @s minecraft:strength 30 1 true')
  player.runCommandSilent('effect give @s minecraft:resistance 30 0 true')

  player.tell(Text.aqua(`§l✦ EVOLUTION ✦ §r§b Stufe ${idx + 1}/8 — [${next.name}]!`))
  if (!evolved) player.tell(Text.gray('(In-Mod-Rasse folgt via Companion-Mod; Rang/Buffs gesetzt.)'))
})

function percent(cur, req) {
  if (req <= 0) return 100
  return Math.max(0, Math.min(99, Math.floor(cur * 100 / req)))
}

// ═══════════════════════ DUNKLER SCHLEIM: +10.000 Magicules ═══════════════════════
ItemEvents.rightClicked(`${NS}:dark_slime`, event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  if (isSneaking(player)) return

  if (!player.isCreative()) event.item.shrink(1)
  if (BRIDGE) { try { BRIDGE.addMagicules(player, 10000) } catch (e) { player.runCommandSilent('scoreboard players add @s sg_magicule 10000') } }
  else player.runCommandSilent('scoreboard players add @s sg_magicule 10000')

  const tier = player.persistentData.getInt('sgSlimeMastery') + 1
  player.persistentData.putInt('sgSlimeMastery', tier)
  player.runCommandSilent('playsound minecraft:entity.slime.squish master @s ~ ~ ~ 1 0.8')
  player.runCommandSilent('particle minecraft:item_slime ~ ~1 ~ 0.4 0.6 0.4 0.1 50 force')
  player.tell(Text.aqua(`Dunkler Schleim absorbiert — +10.000 Magicules (Meisterschaft ${tier}).`))
})
