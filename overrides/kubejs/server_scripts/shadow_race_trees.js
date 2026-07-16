// KubeJS — Tensura Abyss: 4 RASSEN-BAEUME x 9 STUFEN (Gating-Hintergrund)
// server_scripts/ — /kubejs reload server_scripts
//
// ERSETZT shadow_evos.js (alte 8-Stufen-Leiter).
//
// ARCHITEKTUR:
//   - Die Rassen + EP-Gates stehen in config/tensura/ascension-races.toml und
//     erscheinen im ORIGINALEN Tensura-Menue (via Tensura: Ascension).
//   - Dieses Skript erzwingt die ZUSATZ-Gates, die das Menue nicht kennt:
//       Stufe 3: Diablos-Kult-Anfuehrer besiegt (sgKilledCultLeader)
//       Stufe 5: Baum-Katalysator (Slime-Baum: 4x Dunkler Schleim,
//                andere Baeume: 2x Dunkler Aether)
//       Stufe 8: Shadow-Abyss-Dimension betreten (sgEnteredAbyss)
//   - Es vergibt die Skript-PERKS: Fallschaden-Immunitaet (Slime ab Stufe 1),
//     Void-Immunitaet (alle Baeume ab Stufe 8), "I Am Atomic"-Unlock (Slime
//     Stufe 9), Vampir-Nachtregeneration/Lifesteal/Nebelgestalt.
//
// TRIGGER der Evolution: SNEAK + Rechtsklick mit Dunklem Aether
//   (identisch zum alten System — Spieler kennen den Handgriff schon).
// Baum-Wahl: /shadowtree <slime|demon|hero|vampire>  (einmalig)

const NS = 'kubejs'
const ABYSS_DIM = 'tensura_abyss:shadow_abyss'

// ── Java-Bridge (optional) mit lautlosem Fallback ──
let BRIDGE = null
try { BRIDGE = Java.loadClass('net.tensura.abyss.bridge.TensuraBridge') } catch (e) { BRIDGE = null }

function getMagicules(p) {
  if (BRIDGE) { try { return BRIDGE.getMagicules(p) } catch (e) {} }
  return p.runCommandSilent('scoreboard players get @s sg_magicule')
}
function setRace(p, raceId) {
  if (BRIDGE) { try { return BRIDGE.setTensuraRace(p, raceId) } catch (e) {} }
  return false
}
function isSneaking(p) {
  try { return p.isCrouching() } catch (e) { try { return p.isShiftKeyDown() } catch (e2) { return false } }
}
function countItem(p, id) { return p.runCommandSilent(`clear @s ${id} 0`) }
function flag(p, key) { return p.persistentData.getBoolean(key) }

// ── Zusatz-Gates ──
function condCultLeader(p) {
  return flag(p, 'sgKilledCultLeader') ||
         p.runCommandSilent('execute if score @s sg_cult_leader matches 1..') >= 1
}
function condAbyss(p) {
  if (flag(p, 'sgEnteredAbyss')) return true
  let here = ''
  try { here = String(p.level.dimension.location()) } catch (e) {}
  if (here === ABYSS_DIM) { p.persistentData.putBoolean('sgEnteredAbyss', true); return true }
  return false
}

// ── Die 4 Baeume (Stufen-Index 0..8 = Stufe 1..9) ──
// Magicule-Kurve identisch fuer alle Baeume; Katalysator-Kosten steigen mit.
const MAGICULE = [0, 5000, 15000, 40000, 100000, 250000, 600000, 1400000, 3000000]
const AETHER_COST = [0, 1, 2, 3, 4, 5, 7, 9, 12]

const TREES = {
  slime: {
    label: 'Schatten-Schleim',
    catalyst: { id: `${NS}:dark_slime`, n: 4, label: '4x Dunkler Schleim' },
    stages: [
      'tensura_abyss:shadow_slime',        'tensura_abyss:magicule_slime',
      'tensura_abyss:abyss_slime',         'tensura_abyss:shadow_garden_guard',
      'tensura_abyss:dark_slime_sovereign','tensura_abyss:shadow_lord',
      'tensura_abyss:awakened_shadow_lord','tensura_abyss:abyss_monarch',
      'tensura_abyss:eminence_of_the_abyss'
    ],
    names: ['Shadow Slime','Magicule Slime','Abyss Slime','Shadow Garden Guard',
      'Dark Slime Sovereign','Shadow Lord','Awakened Shadow Lord','Abyss Monarch',
      'Eminence of the Abyss']
  },
  demon: {
    label: 'Schatten-Daemon',
    catalyst: { id: `${NS}:dark_aether`, n: 2, label: '2x Dunkler Aether (extra)' },
    stages: [
      'tensura_abyss:low_shadow_demon',    'tensura_abyss:shadow_demon_peer',
      'tensura_abyss:blood_shadow_demon',  'tensura_abyss:arcane_demon_guard',
      'tensura_abyss:arch_demon_of_shadows','tensura_abyss:shadow_duke',
      'tensura_abyss:awakened_demon_king', 'tensura_abyss:void_overlord',
      'tensura_abyss:diablos_eminence'
    ],
    names: ['Low Shadow Demon','Shadow Demon Peer','Blood-Shadow Demon','Arcane Demon Guard',
      'Arch-Demon of Shadows','Shadow Duke','Awakened Demon King','Void Overlord',
      'Diablos Eminence']
  },
  hero: {
    label: 'Antiker Schatten-Held',
    catalyst: { id: `${NS}:dark_aether`, n: 2, label: '2x Dunkler Aether (extra)' },
    stages: [
      'tensura_abyss:human_apprentice',    'tensura_abyss:shadow_spellsword',
      'tensura_abyss:shadow_blade',        'tensura_abyss:cult_breaker',
      'tensura_abyss:master_of_garden',    'tensura_abyss:ancient_knight',
      'tensura_abyss:true_hero_of_shadows','tensura_abyss:light_shadow_monarch',
      'tensura_abyss:sovereign_of_midnight'
    ],
    names: ['Human Apprentice','Shadow Spellsword','Shadow Blade','Cult Breaker',
      'Master of Garden','Ancient Knight','True Hero of Shadows','Light-Shadow Monarch',
      'Sovereign of Midnight']
  },
  vampire: {
    label: 'Urvampir',
    catalyst: { id: `${NS}:dark_aether`, n: 2, label: '2x Dunkler Aether (extra)' },
    stages: [
      'tensura_abyss:vampire_spawn',       'tensura_abyss:blood_shadow',
      'tensura_abyss:mist_walker',         'tensura_abyss:crimson_noble',
      'tensura_abyss:night_stalker',       'tensura_abyss:pureblood_vampire',
      'tensura_abyss:awakened_blood_lord', 'tensura_abyss:monarch_of_the_red_moon',
      'tensura_abyss:progenitor_of_the_abyss'
    ],
    names: ['Vampire Spawn','Blood Shadow','Mist Walker','Crimson Noble',
      'Night Stalker','Pureblood Vampire','Awakened Blood Lord','Monarch of the Red Moon',
      'Progenitor of the Abyss']
  }
}

// Gate-Definition pro Stufen-Index (0-basiert): Ziel-Stufe 3 = Index 2, usw.
function extraGate(treeKey, targetIdx, p) {
  if (targetIdx === 2 && !condCultLeader(p))
    return 'Besiege zuerst einen §5Diablos-Kult-Anfuehrer§7 (Diablos-Ritter).'
  if (targetIdx === 4) {
    const c = TREES[treeKey].catalyst
    if (countItem(p, c.id) < c.n) return `Benoetigt zusaetzlich: §3${c.label}§7.`
  }
  if (targetIdx === 7 && !condAbyss(p))
    return 'Betrete zuerst den §5§lShadow Abyss§r§7 (Aether-Portal).'
  return null
}

// ── Scoreboards ──
ServerEvents.loaded(event => {
  event.server.runCommandSilent('scoreboard objectives add sg_magicule dummy "Magicule"')
  event.server.runCommandSilent('scoreboard objectives add sg_cult_leader dummy "CultLeader"')
})

// ═══════════════ BAUM-WAHL: /shadowtree <slime|demon|hero|vampire> ═══════════════
ServerEvents.commandRegistry(event => {
  const { commands: Commands, arguments: Args } = event

  event.register(Commands.literal('shadowtree')
    .then(Commands.argument('baum', Args.STRING.create(event))
      .executes(ctx => {
        const p = ctx.source.player
        if (!p) return 0
        const key = String(Args.STRING.getResult(ctx, 'baum')).toLowerCase()
        if (!TREES[key]) {
          p.tell(Text.red('Unbekannter Baum. Waehle: slime | demon | hero | vampire'))
          return 0
        }
        if (p.persistentData.getString('sgTree')) {
          p.tell(Text.red('Du hast deinen Pfad bereits gewaehlt — er ist endgueltig.'))
          return 0
        }
        const t = TREES[key]
        p.persistentData.putString('sgTree', key)
        p.persistentData.putInt('sgTreeStage', 0)
        setRace(p, t.stages[0])
        applyPerks(p, key, 0)
        p.runCommandSilent('playsound minecraft:block.beacon.activate master @s ~ ~ ~ 1 0.7')
        p.tell(Text.aqua(`§lPfad gewaehlt: [${t.label}]§r§b — Startform: ${t.names[0]}.`))
        p.tell(Text.gray('Evolution: SNEAK + Rechtsklick mit Dunklem Aether.'))
        return 1
      })))
})

// ═══════════════ EVOLUTION: Sneak + Rechtsklick Dunkler Aether ═══════════════
ItemEvents.rightClicked(`${NS}:dark_aether`, event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  if (!isSneaking(player)) return

  const treeKey = player.persistentData.getString('sgTree')
  if (!treeKey || !TREES[treeKey]) {
    player.tell(Text.gray('Waehle zuerst deinen Pfad: /shadowtree <slime|demon|hero|vampire>'))
    return
  }
  const tree = TREES[treeKey]
  const idx = player.persistentData.getInt('sgTreeStage')
  if (idx >= tree.stages.length - 1) {
    player.tell(Text.gold(`§lMaximale Form erreicht: [${tree.names[idx]}].`))
    return
  }
  const nextIdx = idx + 1

  // 1) Magicule-Gate (exponentiell)
  const need = MAGICULE[nextIdx]
  const mag = getMagicules(player)
  if (mag < need) {
    player.tell(Text.red(`✖ Evolution [${tree.names[nextIdx]}] gesperrt.`))
    player.tell(Text.gray(`Magicule: ${Math.round(mag)} / ${need}.`))
    return
  }

  // 2) hartes Zusatz-Gate (Boss / Item / Dimension)
  const gateMsg = extraGate(treeKey, nextIdx, player)
  if (gateMsg) {
    player.tell(Text.red(`✖ Bedingung nicht erfuellt fuer [${tree.names[nextIdx]}].`))
    player.tell(Text.gray('» ' + gateMsg))
    return
  }

  // 3) Aether-Kosten
  const aetherNeed = AETHER_COST[nextIdx]
  if (countItem(player, `${NS}:dark_aether`) < aetherNeed) {
    player.tell(Text.red(`✖ Katalysator fehlt: ${aetherNeed}x Dunkler Aether.`))
    return
  }

  // ── alles erfuellt: verbrauchen, Rasse setzen, Perks anwenden ──
  if (!player.isCreative()) {
    player.runCommandSilent(`clear @s ${NS}:dark_aether ${aetherNeed}`)
    if (nextIdx === 4) {
      const c = tree.catalyst
      player.runCommandSilent(`clear @s ${c.id} ${c.n}`)
    }
  }
  player.persistentData.putInt('sgTreeStage', nextIdx)
  const evolved = setRace(player, tree.stages[nextIdx])
  applyPerks(player, treeKey, nextIdx)

  // Inszenierung
  player.runCommandSilent('playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 4 0.6')
  player.runCommandSilent('particle minecraft:dragon_breath ~ ~1 ~ 0.7 1.2 0.7 0.02 220 force')
  player.runCommandSilent('particle minecraft:witch ~ ~1 ~ 0.7 1.2 0.7 0.1 140 force')
  player.runCommandSilent('effect give @s minecraft:strength 30 1 true')
  player.runCommandSilent('effect give @s minecraft:resistance 30 0 true')

  player.tell(Text.aqua(`§l✦ EVOLUTION ✦ §r§bStufe ${nextIdx + 1}/9 — [${tree.names[nextIdx]}]!`))
  if (!evolved) player.tell(Text.gray('(In-Mod-Rasse folgt via Companion-Mod/Ascension-Menue; Gating & Perks sind gesetzt.)'))
})

// ═══════════════ PERKS pro Baum & Stufe ═══════════════
function applyPerks(p, treeKey, idx) {
  // Slime-Baum: Fallschaden-Immunitaet ab Stufe 1 (Attribut, permanent).
  if (treeKey === 'slime') {
    p.runCommandSilent('attribute @s minecraft:generic.safe_fall_distance base set 1024')
  }
  // Alle Baeume: Void-Immunitaet ab Stufe 8 (Index 7) — Tick-Rettung unten.
  if (idx >= 7) p.persistentData.putBoolean('sgVoidImmune', true)
  // Slime Stufe 9: "I Am Atomic" freischalten + Katalysator schenken.
  if (treeKey === 'slime' && idx === 8 && !flag(p, 'sgAtomicUnlocked')) {
    p.persistentData.putBoolean('sgAtomicUnlocked', true)
    p.runCommandSilent(`give @s ${NS}:i_am_atomic_catalyst 1`)
    p.runCommandSilent('title @s title {"text":"I AM ATOMIC","color":"aqua","bold":true}')
    p.runCommandSilent('title @s subtitle {"text":"Der finale Skill ist entfesselt.","color":"dark_aqua"}')
    p.runCommandSilent('playsound minecraft:entity.warden.sonic_boom master @s ~ ~ ~ 3 0.5')
  }
}

// ═══════════════ SERVER-TICK: Void-Rettung + Vampir-Perks ═══════════════
let RT_TICK = 0
ServerEvents.tick(event => {
  RT_TICK++
  const players = event.server.players
  const n = players.size()

  // Void-Immunitaet: alle 10 Ticks pruefen (schnell genug fuer freien Fall).
  if (RT_TICK % 10 === 0) {
    for (let i = 0; i < n; i++) {
      const p = players.get(i)
      if (!p.persistentData.getBoolean('sgVoidImmune')) continue
      let minY = -64
      try { minY = p.level.getMinBuildHeight() } catch (e) {}
      if (p.y < minY - 6) {
        p.runCommandSilent('tp @s ~ 320 ~')
        p.runCommandSilent('effect give @s minecraft:slow_falling 45 0 true')
        p.runCommandSilent('effect give @s minecraft:resistance 15 4 true')
        p.runCommandSilent('playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 2 0.5')
        p.tell(Text.darkPurple('Der Abyss traegt seinen Monarchen — die Leere hat keine Macht ueber dich.'))
      }
    }
  }

  // Vampir-Baum: Nacht-Regeneration (+ Nebel-Unsichtbarkeit ab Mist Walker beim Schleichen).
  if (RT_TICK % 60 === 0) {
    for (let i = 0; i < n; i++) {
      const p = players.get(i)
      if (p.persistentData.getString('sgTree') !== 'vampire') continue
      let night = false
      try { night = p.level.isNight() } catch (e) {}
      if (!night) continue
      const stage = p.persistentData.getInt('sgTreeStage')
      p.runCommandSilent(`effect give @s minecraft:regeneration 4 ${stage >= 5 ? 1 : 0} true`)
      if (stage >= 2 && isSneaking(p)) {
        p.runCommandSilent('effect give @s minecraft:invisibility 4 0 true')
      }
    }
  }
})

// Vampir-Lifesteal: Kill bei Nacht heilt (skaliert mit Stufe).
EntityEvents.death(event => {
  const e = event.entity
  if (!e || e.isPlayer()) return
  let killer = null
  try { killer = event.source.player } catch (err) {}
  if (!killer || !killer.persistentData) return
  if (killer.persistentData.getString('sgTree') !== 'vampire') return
  const stage = killer.persistentData.getInt('sgTreeStage')
  const amount = 2 + stage // 2..10 HP
  killer.runCommandSilent(`effect give @s minecraft:instant_health 1 ${stage >= 6 ? 1 : 0} true`)
  killer.runCommandSilent('particle minecraft:damage_indicator ~ ~1 ~ 0.3 0.5 0.3 0.05 12 force')
})

// ═══════════════ DUNKLER SCHLEIM: +10.000 Magicules (aus shadow_evos.js uebernommen) ═══════════════
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
