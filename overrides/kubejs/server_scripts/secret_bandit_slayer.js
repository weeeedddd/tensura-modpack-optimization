// KubeJS — Tensura Abyss: GEHEIME RASSE "Stylish Bandit Slayer"
// server_scripts/ — /kubejs reload server_scripts
//
// Die versteckte Gilden-Quest "Crazy Kid???" (Cid als Kind, das im Wald
// Banditen jagt). NICHT im Startmenue — Freischaltung nur ueber diesen Ablauf:
//
//   1) READINESS: Spieler hat einen Baum gewaehlt (shadow_race_trees.js) und
//      >= READY_MAGICULE Magicules -> personalisierte Titel-Einblendung
//      "Neue Gilden Quest??? freigeschaltet" (NUR fuer diesen Spieler).
//   2) QUEST: 500.000 Mitsugoshi-Muenzen besitzen UND BANDITS_REQUIRED
//      Banditen (#tensura_abyss:bandits) INNERHALB eines Banditen-Dorfs
//      (#tensura_abyss:bandit_villages, Standard: Pillager Outpost) toeten.
//      Fortschritt: /shadowquest status
//   3) TURN-IN: /shadowquest turnin -> faint event (Nausea + Blindness +
//      Bewegungssperre) -> permanenter Rassen-Wechsel auf
//      tensura_abyss:stylish_bandit_slayer (via TensuraBridge).
//
// GILDEN-GUI-HOOK: Das Skript setzt Scoreboard sg_secret_quest = 1 fuer den
// freigeschalteten Spieler. Die Companion-Mod (CommissionManager) kann daran
// die Quest im Gilden-Interface NUR fuer diesen Spieler einblenden.
//
// LIEGE-ANIMATION: Es ist kein playerAnimator im Pack — die Ohnmacht wird
// ueber Blackscreen (Blindness+Darkness) + totale Bewegungssperre inszeniert.
// Fuer eine ECHTE Liege-Pose (Pose.SLEEPING) einen Packet-Hook in der
// Companion-Mod ergaenzen (siehe TODO unten).

const SBS_RACE = 'tensura_abyss:stylish_bandit_slayer'
const COINS_REQUIRED = 500000
const BANDITS_REQUIRED = 15
const READY_MAGICULE = 100000
const READY_CHECK_TICKS = 200      // alle 10s Readiness pruefen
const FAINT_TICKS = 100            // 5s Ohnmacht vor der Verwandlung

// ── Bridges (optional, mit Fallbacks) ──
let SBS_BRIDGE = null
try { SBS_BRIDGE = Java.loadClass('net.tensura.abyss.bridge.TensuraBridge') } catch (e) { SBS_BRIDGE = null }
let MARKET = null
try { MARKET = Java.loadClass('net.tensura.abyss.market.MarketManager') } catch (e) { MARKET = null }

function sbsMagicules(p) {
  if (SBS_BRIDGE) { try { return SBS_BRIDGE.getMagicules(p) } catch (e) {} }
  return p.runCommandSilent('scoreboard players get @s sg_magicule')
}
// Muenzen lesen: MarketManager -> Entity-NBT (NeoForgeData) -> Scoreboard sg_coins
function sbsCoins(p) {
  if (MARKET) { try { return MARKET.getCoins(p) } catch (e) {} }
  try {
    const nbt = p.getNbt()
    for (const key of ['NeoForgeData', 'ForgeData']) {
      if (nbt.contains(key) && nbt.getCompound(key).contains('mitsugoshiCoins')) {
        return nbt.getCompound(key).getLong('mitsugoshiCoins')
      }
    }
  } catch (e) {}
  return p.runCommandSilent('scoreboard players get @s sg_coins')
}
function sbsSpendCoins(p, amount) {
  if (MARKET) { try { return MARKET.trySpend(p, amount) } catch (e) {} }
  // Fallback: Scoreboard abziehen (nur wenn dort gefuehrt)
  if (p.runCommandSilent(`execute if score @s sg_coins matches ${amount}..`) >= 1) {
    p.runCommandSilent(`scoreboard players remove @s sg_coins ${amount}`)
    return true
  }
  return false
}

ServerEvents.loaded(event => {
  event.server.runCommandSilent('scoreboard objectives add sg_secret_quest dummy "SecretQuest"')
  event.server.runCommandSilent('scoreboard objectives add sg_coins dummy "MitsugoshiCoins"')
})

// ═══════════════ 1) READINESS-CHECK + personalisierte Einblendung ═══════════════
let SBS_TICK = 0
ServerEvents.tick(event => {
  SBS_TICK++
  if (SBS_TICK % READY_CHECK_TICKS !== 0) return

  const players = event.server.players
  for (let i = 0; i < players.size(); i++) {
    const p = players.get(i)
    const d = p.persistentData
    if (d.getBoolean('sbsQuestUnlocked') || d.getBoolean('sbsTransformed')) continue
    if (!d.getString('sgTree')) continue                    // erst nach Rassen-Wahl
    if (sbsMagicules(p) < READY_MAGICULE) continue          // erst wenn "bereit"

    d.putBoolean('sbsQuestUnlocked', true)
    p.runCommandSilent('scoreboard players set @s sg_secret_quest 1') // GUI-Hook

    // Fette, NUR fuer diesen Spieler sichtbare Einblendung mitten auf dem Screen
    p.runCommandSilent('title @s times 10 90 30')
    p.runCommandSilent('title @s title {"text":"New Guild Quest???","color":"gold","bold":true}')
    p.runCommandSilent('title @s subtitle {"text":"unlocked","color":"yellow","italic":true}')
    p.runCommandSilent('playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 0.8')
    p.tell(Text.gold('§l??? §r§7Something new has appeared in the guild interface... (/shadowquest status)'))
  }
})

// ═══════════════ 2) QUEST-TRACKING: Banditen im Banditen-Dorf ═══════════════
EntityEvents.death(event => {
  const e = event.entity
  if (!e || e.isPlayer()) return

  let killer = null
  try { killer = event.source.player } catch (err) {}
  if (!killer || !killer.persistentData) return
  const d = killer.persistentData
  if (!d.getBoolean('sbsQuestUnlocked') || d.getBoolean('sbsTransformed')) return

  // Ist das Opfer ein Bandit? (Entity-Type-Tag #tensura_abyss:bandits)
  let isBandit = false
  try { isBandit = e.type.is ? e.type.is('tensura_abyss:bandits') : false } catch (err) {}
  if (!isBandit) {
    // Fallback ueber Command-Selektor am Todesort
    isBandit = killer.server.runCommandSilent(
      `execute positioned ${e.x} ${e.y} ${e.z} if entity @e[type=#tensura_abyss:bandits,distance=..1,limit=1]`) >= 1
  }
  if (!isBandit) return

  // Lag der Kill INNERHALB der Banditen-Dorf-Struktur? (Datapack-Predicate)
  const inVillage = killer.server.runCommandSilent(
    `execute positioned ${e.x} ${e.y} ${e.z} if predicate tensura_abyss:in_bandit_village`) >= 1
  if (!inVillage) return

  const kills = d.getInt('sbsBanditKills') + 1
  d.putInt('sbsBanditKills', kills)
  if (kills < BANDITS_REQUIRED) {
    killer.runCommandSilent(`title @s actionbar {"text":"Crazy Kid??? — Bandits: ${kills}/${BANDITS_REQUIRED}","color":"gold"}`)
  } else if (kills === BANDITS_REQUIRED) {
    killer.runCommandSilent('title @s times 10 70 20')
    killer.runCommandSilent('title @s title {"text":"The village has been cleansed.","color":"dark_red","bold":true}')
    killer.runCommandSilent('title @s subtitle {"text":"Return to the guild: /shadowquest turnin","color":"gray"}')
    killer.runCommandSilent('playsound minecraft:entity.wither.death master @s ~ ~ ~ 0.6 0.5')
  }
})

// ═══════════════ 3) /shadowquest — Status & Abgabe ═══════════════
ServerEvents.commandRegistry(event => {
  const { commands: Commands } = event

  event.register(Commands.literal('shadowquest')
    .then(Commands.literal('status').executes(ctx => {
      const p = ctx.source.player
      if (!p) return 0
      const d = p.persistentData
      if (d.getBoolean('sbsTransformed')) { p.tell(Text.darkPurple('You ARE the Stylish Bandit Slayer.')); return 1 }
      if (!d.getBoolean('sbsQuestUnlocked')) { p.tell(Text.gray('No secret quest active.')); return 1 }
      const coins = sbsCoins(p)
      const kills = d.getInt('sbsBanditKills')
      p.tell(Text.gold('§l— Guild Quest: "Crazy Kid???" —'))
      p.tell(Text.gray(`A crazy kid is hunting bandits in the woods...`))
      p.tell(Text.gray(`» Shadow Coins: ${coins} / ${COINS_REQUIRED}  ${coins >= COINS_REQUIRED ? '§a✔' : '§c✘'}`))
      p.tell(Text.gray(`» Bandit village cleansed: ${kills} / ${BANDITS_REQUIRED}  ${kills >= BANDITS_REQUIRED ? '§a✔' : '§c✘'}`))
      p.tell(Text.gray('Turn in: /shadowquest turnin'))
      return 1
    }))
    .then(Commands.literal('turnin').executes(ctx => {
      const p = ctx.source.player
      if (!p) return 0
      const d = p.persistentData
      if (d.getBoolean('sbsTransformed')) { p.tell(Text.gray('Already transformed.')); return 0 }
      if (!d.getBoolean('sbsQuestUnlocked')) { p.tell(Text.gray('No secret quest active.')); return 0 }

      const kills = d.getInt('sbsBanditKills')
      if (kills < BANDITS_REQUIRED) {
        p.tell(Text.red(`✖ The bandit village is not cleansed yet (${kills}/${BANDITS_REQUIRED}).`))
        return 0
      }
      if (sbsCoins(p) < COINS_REQUIRED) {
        p.tell(Text.red(`✖ Not enough Shadow Coins (${sbsCoins(p)}/${COINS_REQUIRED}).`))
        return 0
      }
      if (!sbsSpendCoins(p, COINS_REQUIRED)) {
        p.tell(Text.red('✖ Coins could not be deducted.'))
        return 0
      }

      startTransformation(p)
      return 1
    })))
})

// ═══════════════ DAS VERWANDLUNGS-EVENT ═══════════════
function startTransformation(p) {
  const d = p.persistentData
  d.putBoolean('sbsTransformed', true)          // sofort sperren (kein Doppel-Trigger)
  d.remove('sbsQuestUnlocked')
  p.runCommandSilent('scoreboard players set @s sg_secret_quest 2')  // GUI-Hook: erledigt

  // Phase 1 — Ohnmacht: Schwindel, Blackscreen, totale Bewegungssperre.
  // TODO (Companion-Mod): echter Liege-Pose-Packet-Hook (Pose.SLEEPING) —
  // z.B. GuildEventHandler.sendFaintPose(p, FAINT_TICKS). Bis dahin:
  // Blindness+Darkness+Slowness-255 liest sich in-game als Zusammenbrechen.
  const secs = Math.ceil(FAINT_TICKS / 20) + 2
  p.runCommandSilent(`effect give @s minecraft:nausea ${secs + 6} 0 true`)
  p.runCommandSilent(`effect give @s minecraft:blindness ${secs} 0 true`)
  p.runCommandSilent(`effect give @s minecraft:darkness ${secs} 0 true`)
  p.runCommandSilent(`effect give @s minecraft:slowness ${secs} 255 true`)
  p.runCommandSilent(`effect give @s minecraft:jump_boost ${secs} 250 true`)  // Level 250 = Sprung deaktiviert
  p.runCommandSilent(`effect give @s minecraft:resistance ${secs} 4 true`)    // waehrenddessen unverwundbar
  p.runCommandSilent('playsound minecraft:entity.player.breath master @s ~ ~ ~ 1 0.5')
  p.runCommandSilent('playsound minecraft:block.deepslate.break master @s ~ ~ ~ 1 0.4')
  p.tell(Text.darkGray('§oEverything fades to black...'))

  // Phase 2 — nach FAINT_TICKS: permanenter Rassen-Wechsel + Reveal.
  p.server.scheduleInTicks(FAINT_TICKS, () => {
    let ok = false
    if (SBS_BRIDGE) { try { ok = SBS_BRIDGE.setTensuraRace(p, SBS_RACE) } catch (e) { ok = false } }
    d.putString('sgTree', 'secret')            // aus den 4 Baeumen ausklinken
    d.putInt('sgTreeStage', 8)
    d.putBoolean('sgVoidImmune', true)

    p.runCommandSilent('title @s times 15 100 40')
    p.runCommandSilent('title @s title {"text":"STYLISH BANDIT SLAYER","color":"dark_purple","bold":true}')
    p.runCommandSilent('title @s subtitle {"text":"The crazy kid smiles in the shadows.","color":"gray","italic":true}')
    p.runCommandSilent('playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 3 0.5')
    p.runCommandSilent('playsound minecraft:entity.warden.sonic_boom master @s ~ ~ ~ 2 0.6')
    p.runCommandSilent('particle minecraft:dragon_breath ~ ~1 ~ 1.0 1.4 1.0 0.03 320 force')
    p.runCommandSilent('particle minecraft:end_rod ~ ~1 ~ 0.8 1.2 0.8 0.05 180 force')
    p.runCommandSilent('effect give @s minecraft:regeneration 10 1 true')

    if (!ok) p.tell(Text.gray('(In-mod race pending via companion mod; status & perks are applied.)'))
    p.tell(Text.darkPurple('§lThe guild will never learn who erased the bandits.'))
  })
}
