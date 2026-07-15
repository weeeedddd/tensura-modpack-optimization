// KubeJS 6 — Tensura Abyss: "Eminence in Shadow" ⇄ Tensura Evolutions-Kopplung
// server_scripts/ — hot-reloadbar mit /kubejs reload server_scripts
//
// HYBRID: Ruft die Java-Companion-Mod (net.tensura.abyss.bridge.TensuraBridge)
// fuer echten Tensura-Zugriff (Magicules/EP/Rasse). Ist die Mod nicht gebaut,
// faellt ALLES automatisch auf den Scoreboard-Proxy zurueck -> laeuft trotzdem.
//
// Ausloeser:
//   Dunkler Schleim (Rechtsklick)           -> +10.000 Magicules (Tensura)
//   Dunkler Aether  (Sneak + Rechtsklick)   -> Evolution zur naechsten Stufe
//   Dunkler Aether  (Rechtsklick, kein Sneak)-> Breath (shadow_garden.js)

// ── Item-Namespace: 'kubejs' (KubeJS-Items) ODER 'tensura_abyss' (Companion-Mod).
//    Bei installierter Companion-Mod hier auf 'tensura_abyss' umstellen.
const NS = 'kubejs'
const MAGICULE_OBJ = 'sg_magicule'

// Rang-Leiter mit Tensura-Rassenpfad + Magicule-Schwelle + Katalysator.
const RANKS = [
  { id: 'possessed', name: 'Possessed',            race: 'tensura_abyss:possessed',            magicule: 0,     aether: 0, slime: 0 },
  { id: 'member',    name: 'Shadow Garden Member', race: 'tensura_abyss:shadow_garden_member', magicule: 5000,  aether: 1, slime: 0 },
  { id: 'delta',     name: 'Delta',                race: 'tensura_abyss:seven_shadows_delta',  magicule: 15000, aether: 2, slime: 2 },
  { id: 'gamma',     name: 'Gamma',                race: 'tensura_abyss:seven_shadows_gamma',  magicule: 40000, aether: 3, slime: 4 },
  { id: 'beta',      name: 'Beta',                 race: 'tensura_abyss:seven_shadows_beta',   magicule: 90000, aether: 4, slime: 6 },
  { id: 'alpha',     name: 'Alpha',                race: 'tensura_abyss:seven_shadows_alpha',  magicule: 200000, aether: 5, slime: 8 },
  { id: 'shadow',    name: 'Shadow',               race: 'tensura_abyss:shadow',               magicule: 500000, aether: 8, slime: 12 }
]

// ═══════════════════════ Java-Bridge mit Fallback ═══════════════════════
let BRIDGE = null
try { BRIDGE = Java.loadClass('net.tensura.abyss.bridge.TensuraBridge') } catch (e) { BRIDGE = null }

function getMagicules(player) {
  if (BRIDGE) { try { return BRIDGE.getMagicules(player) } catch (e) {} }
  return player.runCommandSilent(`scoreboard players get @s ${MAGICULE_OBJ}`)
}
function addMagicules(player, delta) {
  if (BRIDGE) { try { BRIDGE.addMagicules(player, delta); return } catch (e) {} }
  player.runCommandSilent(`scoreboard players add @s ${MAGICULE_OBJ} ${Math.round(delta)}`)
}
function setTensuraRace(player, racePath) {
  if (BRIDGE) { try { return BRIDGE.setTensuraRace(player, racePath) } catch (e) {} }
  return false // ohne Mod: nur unser Rang/Praefix (siehe unten)
}
function isSneaking(p) {
  try { return p.isCrouching() } catch (e) {
    try { return p.isShiftKeyDown() } catch (e2) { return false }
  }
}
function countItem(player, id) { return player.runCommandSilent(`clear @s ${id} 0`) }
function rankIndex(player) { return player.persistentData.getInt('sgEvoRank') }

ServerEvents.loaded(event => {
  event.server.runCommandSilent(`scoreboard objectives add ${MAGICULE_OBJ} dummy "Magicule"`)
})

// ═══════════════════════ EVOLUTION: Sneak + Dunkler Aether ═══════════════════════
ItemEvents.rightClicked(`${NS}:dark_aether`, event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  if (!isSneaking(player)) return   // normaler Rechtsklick = Breath (shadow_garden.js)

  const idx = rankIndex(player)
  if (idx >= RANKS.length - 1) {
    player.tell(Text.gold(`Du hast die hoechste Form erreicht: [${RANKS[idx].name}].`))
    return
  }
  const next = RANKS[idx + 1]

  // 1) Magicule-Gate (echt via Bridge, sonst Scoreboard)
  const mag = getMagicules(player)
  if (mag < next.magicule) {
    player.tell(Text.gray(`Zu wenig Magicule: ${Math.round(mag)} / ${next.magicule} fuer [${next.name}].`))
    return
  }

  // 2) Katalysator-Check
  const haveAether = countItem(player, `${NS}:dark_aether`)
  const haveSlime  = next.slime > 0 ? countItem(player, `${NS}:dark_slime`) : 0
  if (haveAether < next.aether || haveSlime < next.slime) {
    player.tell(Text.gray(`Katalysator fehlt: ${next.aether}x Dunkler Aether` +
      (next.slime > 0 ? ` + ${next.slime}x Dunkler Schleim` : '') + `.`))
    return
  }

  // 3) Katalysator verbrauchen
  if (!player.isCreative()) {
    if (next.aether > 0) player.runCommandSilent(`clear @s ${NS}:dark_aether ${next.aether}`)
    if (next.slime > 0)  player.runCommandSilent(`clear @s ${NS}:dark_slime ${next.slime}`)
  }

  // 4) Rang setzen + Praefix
  player.persistentData.putInt('sgEvoRank', idx + 1)
  const teams = ['sg_shadow','sg_shadow','sg_numbers','sg_numbers','sg_seven','sg_seven','sg_lord']
  player.runCommandSilent(`team join ${teams[idx + 1]} @s`)

  // 5) ECHTE Tensura-Evolution ueber die Java-Bridge
  const evolved = setTensuraRace(player, next.race)

  // 6) Epische Animation (purpur/schwarz)
  player.runCommandSilent('playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 4 0.6')
  player.runCommandSilent('particle minecraft:dragon_breath ~ ~1 ~ 0.7 1.2 0.7 0.02 200 force')
  player.runCommandSilent('particle minecraft:witch ~ ~1 ~ 0.7 1.2 0.7 0.1 120 force')
  player.runCommandSilent('particle minecraft:smoke ~ ~1 ~ 0.8 1.2 0.8 0.02 120 force')
  player.runCommandSilent('effect give @s minecraft:strength 30 1 true')
  player.runCommandSilent('effect give @s minecraft:regeneration 15 1 true')
  player.runCommandSilent('effect give @s minecraft:resistance 30 0 true')

  player.tell(Text.aqua(`§lEVOLUTION§r§b — [${next.name}] erreicht!`))
  if (!evolved) player.tell(Text.gray('(In-Mod-Rasse folgt, sobald die Companion-Mod aktiv ist — Rang/Buffs sind gesetzt.)'))
})

// ═══════════════════════ DUNKLER SCHLEIM: +10.000 Magicules ═══════════════════════
ItemEvents.rightClicked(`${NS}:dark_slime`, event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  if (isSneaking(player)) return

  if (!player.isCreative()) event.item.shrink(1)
  addMagicules(player, 10000)

  const tier = player.persistentData.getInt('sgSlimeMastery') + 1
  player.persistentData.putInt('sgSlimeMastery', tier)

  player.runCommandSilent('playsound minecraft:entity.slime.squish master @s ~ ~ ~ 1 0.8')
  player.runCommandSilent('particle minecraft:item_slime ~ ~1 ~ 0.4 0.6 0.4 0.1 50 force')
  player.tell(Text.aqua(`Dunkler Schleim absorbiert — +10.000 Magicules (Schleim-Meisterschaft ${tier}).`))
})
