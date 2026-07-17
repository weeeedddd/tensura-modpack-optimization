// KubeJS 6 — Tensura Abyss: "The Eminence in Shadow" Fraktions- & Progressions-System
// MC 1.21.1 / NeoForge. Ordner: kubejs/server_scripts/
// Hot-reloadbar mit: /kubejs reload server_scripts
//
// DESIGN-PRINZIP: Die gesamte Spiel-Logik laeuft ueber stabile Vanilla-Commands
// (/damage, /particle, /execute if items, /team, /summon), die via KubeJS
// ausgefuehrt werden. Das ist versionsstabil und vermeidet fragile Java-Mappings.
//
// ABHAENGIGKEIT: die Items (tensura_abyss:dark_slime, ...slime_suit_*, ...)
// registriert die Companion-Mod 'tensura_abyss' in Java.
//
// ─────────────────────────────────────────────────────────────────────────────
// Modul-State (server-seitig, robust ueber eigenen Tick-Zaehler)
// ─────────────────────────────────────────────────────────────────────────────
let SG_TICK = 0
let SG_SERVER = null

// Zentrale Balancing-Werte
const ATOMIC_MANA_COST = 30       // XP-Level als Mana-Proxy (Iron's Spells Mana ist per KubeJS nicht sicher lesbar)
const ATOMIC_COOLDOWN_MS = 120000 // 2 Minuten Cooldown
const ATOMIC_RADIUS = 30          // Wirkungsradius in Blocks
const ATOMIC_DAMAGE = 120         // Schaden pro Mob (magisch)
const ATOMIC_MIN_MAXEP = 5000000  // Voraussetzung: >= 5.000.000 Max EP (via Ascension-API)

// Java-Bridge (optional) — Rang/EP-Gate. Ohne Companion-Mod: Gate wird uebersprungen.
let ATOMIC_BRIDGE = null
try { ATOMIC_BRIDGE = Java.loadClass('net.tensura.abyss.bridge.TensuraBridge') } catch (e) { ATOMIC_BRIDGE = null }
const RAID_INTERVAL_TICKS = 1200  // alle 60s pruefen
const RAID_CHANCE = 0.5           // 50% pro Intervall (nur bei aktiver Mitsugoshi-Tarnung)
const SUIT_INTERVAL_TICKS = 40    // Set-Bonus alle 2s erneuern

// ═════════════════════════════════════════════════════════════════════════════
// 1) REZEPTE — Dark Slime / Dark Aether / Katalysator / Slime Suit / Reworks
// ═════════════════════════════════════════════════════════════════════════════
ServerEvents.recipes(event => {

  // Dark Slime: veredelter Schleim aus Schleimbaellen + Wither-Rose (dunkle Essenz)
  event.shapeless('2x tensura_abyss:dark_slime', [
    'minecraft:slime_ball', 'minecraft:slime_ball', 'minecraft:slime_ball',
    'minecraft:wither_rose', 'minecraft:ink_sac'
  ]).id('tensurapack:dark_slime')

  // Dark Aether: Endgame-Kern (Netherstern-Gate = erst nach Wither-Kill)
  // Muster: 6x Dark Slime + 2x Echo Shard + 1x Nether Star
  event.shaped('2x tensura_abyss:dark_aether', [
    'SES',
    'SNS',
    'SES'
  ], {
    S: 'tensura_abyss:dark_slime',
    E: 'minecraft:echo_shard',
    N: 'minecraft:nether_star'
  }).id('tensurapack:dark_aether')

  // "I Am Atomic" Katalysator: 1x Dark Aether + Obsidian-Rahmen + Netherstern
  event.shaped('tensura_abyss:i_am_atomic_catalyst', [
    'ONO',
    'OAO',
    'OOO'
  ], {
    O: 'minecraft:obsidian',
    N: 'minecraft:nether_star',
    A: 'tensura_abyss:dark_aether'
  }).id('tensurapack:i_am_atomic_catalyst')

  // Shadow-Garden-Pledge (Rang-Aufstieg-Token, guenstig)
  event.shapeless('4x tensura_abyss:shadow_pledge_note', [
    'minecraft:paper', 'minecraft:paper', 'tensura_abyss:dark_slime', 'minecraft:ink_sac'
  ]).id('tensurapack:shadow_pledge_note')

  // Mitsugoshi Trade Ledger (schaltet Tarn-/Raid-Mechanik frei)
  event.shaped('tensura_abyss:mitsugoshi_ledger', [
    'LGL',
    'GBG',
    'LGL'
  ], {
    L: 'minecraft:leather',
    G: 'minecraft:gold_ingot',
    B: 'minecraft:writable_book'
  }).id('tensurapack:mitsugoshi_ledger')

  // ── Slime Suit: Netherit-Basis + Dark Slime + Kult-Insignie ──
  const suit = { helmet: 'helmet', chestplate: 'chestplate', leggings: 'leggings', boots: 'boots' }
  Object.keys(suit).forEach(piece => {
    event.shapeless(`tensura_abyss:slime_suit_${piece}`, [
      `minecraft:netherite_${piece}`,
      'tensura_abyss:dark_slime', 'tensura_abyss:dark_slime',
      'tensura_abyss:cult_insignia'
    ]).id(`tensurapack:slime_suit_${piece}`)
  })

  // ── High-Tier Reworks: Dark Aether ERSETZT Diamanten-Upgrades ──
  // Netherit-Waffe + 1x Dark Aether -> garantiertes Endgame-Enchant-Paket
  event.shapeless('minecraft:netherite_sword', [
    'minecraft:netherite_sword', 'tensura_abyss:dark_aether'
  ]).enchant('minecraft:sharpness', 5)
    .enchant('minecraft:mending', 1)
    .enchant('minecraft:unbreaking', 3)
    .enchant('minecraft:looting', 3)
    .id('tensurapack:rework_netherite_sword')

  event.shapeless('minecraft:netherite_axe', [
    'minecraft:netherite_axe', 'tensura_abyss:dark_aether'
  ]).enchant('minecraft:sharpness', 5)
    .enchant('minecraft:mending', 1)
    .enchant('minecraft:unbreaking', 3)
    .id('tensurapack:rework_netherite_axe')

  Object.keys(suit).forEach(piece => {
    event.shapeless(`minecraft:netherite_${piece}`, [
      `minecraft:netherite_${piece}`, 'tensura_abyss:dark_aether'
    ]).enchant('minecraft:protection', 4)
      .enchant('minecraft:unbreaking', 3)
      .enchant('minecraft:mending', 1)
      .id(`tensurapack:rework_netherite_${piece}`)
  })
})

// ═════════════════════════════════════════════════════════════════════════════
// 2) ULTIMATE SKILL "I AM ATOMIC"
//    Verbraucht Mana (XP-Level) + 1x Katalysator (= 1x Dark Aether im Craft).
//    Riesige neon-blaue Partikelexplosion + massiver AoE-Schaden.
//    WICHTIG: Nutzt /damage + /particle -> ZERSTOERT KEINE BLOECKE (kein Server-Lag).
// ═════════════════════════════════════════════════════════════════════════════
ItemEvents.rightClicked('tensura_abyss:i_am_atomic_catalyst', event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return

  const now = Date.now()
  const readyAt = player.persistentData.getLong('sgAtomicReadyAt')
  if (now < readyAt) {
    const secs = Math.ceil((readyAt - now) / 1000)
    player.tell(Text.gray(`I Am Atomic laedt noch nach... / recharging... (${secs}s)`))
    return
  }

  // Voraussetzung: [Eminence of the Abyss] (Slime-Baum Stufe 9, setzt
  // sgAtomicUnlocked in shadow_race_trees.js) ODER Legacy-Rang "Shadow".
  const atomicUnlocked = player.persistentData.getBoolean('sgAtomicUnlocked') ||
    player.persistentData.getInt('sgEvoRank') >= 6
  if (!atomicUnlocked) {
    player.tell(Text.gray('Nur die [Eminence of the Abyss] kann "I Am Atomic" entfesseln.'))
    return
  }
  if (ATOMIC_BRIDGE) {
    let maxEp = 0
    try { maxEp = ATOMIC_BRIDGE.getMaxEP(player) } catch (e) { maxEp = ATOMIC_MIN_MAXEP }
    if (maxEp < ATOMIC_MIN_MAXEP) {
      player.tell(Text.gray(`Zu wenig Max EP: ${Math.round(maxEp)} / ${ATOMIC_MIN_MAXEP}.`))
      return
    }
  }

  // Mana-Proxy: XP-Level pruefen und abziehen (via Command = robust)
  let lvl = 0
  try { lvl = player.xpLevel } catch (err) { lvl = -1 }
  if (lvl >= 0 && lvl < ATOMIC_MANA_COST) {
    player.tell(Text.gray(`Nicht genug Mana / not enough mana (braucht ${ATOMIC_MANA_COST} XP-Level).`))
    return
  }
  player.runCommandSilent(`xp add @s -${ATOMIC_MANA_COST} levels`)

  // Katalysator verbrauchen
  if (!player.isCreative()) event.item.shrink(1)

  // Neon-blaue Explosion (mehrschichtige Partikel), zentriert auf den Spieler
  player.runCommandSilent('particle minecraft:flash ~ ~1 ~ 0 0 0 0 2 force')
  player.runCommandSilent('particle minecraft:sonic_boom ~ ~1 ~ 0 0 0 0 4 force')
  player.runCommandSilent(`particle minecraft:soul_fire_flame ~ ~1 ~ ${ATOMIC_RADIUS * 0.4} ${ATOMIC_RADIUS * 0.4} ${ATOMIC_RADIUS * 0.4} 0.02 900 force`)
  player.runCommandSilent(`particle minecraft:electric_spark ~ ~1 ~ ${ATOMIC_RADIUS * 0.45} ${ATOMIC_RADIUS * 0.45} ${ATOMIC_RADIUS * 0.45} 0.12 500 force`)
  player.runCommandSilent(`particle minecraft:end_rod ~ ~1 ~ ${ATOMIC_RADIUS * 0.45} ${ATOMIC_RADIUS * 0.45} ${ATOMIC_RADIUS * 0.45} 0.04 350 force`)

  // Kreisfoermiges neon-blaues Dornenfeld (expandierender Ring)
  for (let a = 0; a < 360; a += 12) {
    const rad = a * Math.PI / 180
    const dx = (Math.cos(rad) * ATOMIC_RADIUS * 0.7).toFixed(1)
    const dz = (Math.sin(rad) * ATOMIC_RADIUS * 0.7).toFixed(1)
    player.runCommandSilent(`particle minecraft:soul_fire_flame ~${dx} ~0.3 ~${dz} 0.2 0.6 0.2 0.01 12 force`)
  }

  // Tiefer Bass-Sound
  player.runCommandSilent('playsound minecraft:entity.warden.sonic_boom master @a ~ ~ ~ 6 0.5')
  player.runCommandSilent('playsound minecraft:block.beacon.deactivate master @a ~ ~ ~ 6 0.4')
  player.runCommandSilent('playsound minecraft:entity.generic.explode master @a ~ ~ ~ 5 0.7')

  // AoE-Schaden an ALLEN Nicht-Spieler-Entities im Radius (keine Blockschaeden!)
  player.runCommandSilent(`damage @e[distance=..${ATOMIC_RADIUS},type=!minecraft:player] ${ATOMIC_DAMAGE} minecraft:magic by @s`)

  player.persistentData.putLong('sgAtomicReadyAt', now + ATOMIC_COOLDOWN_MS)
  player.tell(Text.aqua('§lI... AM... ATOMIC.'))
})

// ═════════════════════════════════════════════════════════════════════════════
// 3) DARK AETHER -> TENSURA "BREATH"-SYSTEM FREISCHALTEN
//    Rechtsklick verbraucht 1x Dark Aether und schaltet eine Breath-Stufe frei.
//    !! Die exakte Tensura-Skill-Grant-Syntax MUSS im Spiel verifiziert werden !!
//    (Tensura hat eigene Commands; teste /tensura help oder pruefe das Wiki.)
// ═════════════════════════════════════════════════════════════════════════════
ItemEvents.rightClicked('tensura_abyss:dark_aether', event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  // Sneak + Rechtsklick ist der EVOLUTIONS-Trigger (shadow_evos.js) — hier ignorieren.
  if (player.isCrouching()) return

  if (!player.isCreative()) event.item.shrink(1)

  const breath = player.persistentData.getInt('sgBreathTier') + 1
  player.persistentData.putInt('sgBreathTier', breath)

  player.runCommandSilent('playsound minecraft:block.beacon.power_select master @s ~ ~ ~ 1 1.2')
  player.runCommandSilent('particle minecraft:soul_fire_flame ~ ~1 ~ 0.4 0.8 0.4 0.02 80 force')

  // >>> HIER Tensura-Command aktivieren, sobald die Syntax verifiziert ist: <<<
  // player.runCommandSilent('tensura skill unlock @s breath_of_the_abyss')

  player.tell(Text.aqua(`Dark Aether resoniert — Breath-Attunement Stufe ${breath}.`))
  player.tell(Text.gray('(Tensura-Skill-Command in shadow_garden.js aktivieren, siehe Kommentar.)'))
})

// ═════════════════════════════════════════════════════════════════════════════
// 4) SHADOW-GARDEN RANG-SYSTEM (Chat-Praefixe via Scoreboard-Teams)
//    Vanilla-Teams treiben Chat- UND Nametag-Praefix -> mod-unabhaengig robust.
// ═════════════════════════════════════════════════════════════════════════════
ServerEvents.loaded(event => {
  const s = event.server
  // [id, prefix, color]
  const teams = [
    ['sg_shadow',  '[Shadow] ',        'gray'],
    ['sg_numbers', '[Numbers] ',       'aqua'],
    ['sg_seven',   '[Seven Shadows] ', 'dark_aqua'],
    ['sg_lord',    '[Shadow Lord] ',   'dark_purple']
  ]
  teams.forEach(t => {
    s.runCommandSilent(`team add ${t[0]}`)
    s.runCommandSilent(`team modify ${t[0]} prefix {"text":"${t[1]}","color":"${t[2]}"}`)
  })
})

// Pledge einloesen -> Rang steigt an Schwellen; Team-Zuweisung setzt den Praefix.
ItemEvents.rightClicked('tensura_abyss:shadow_pledge_note', event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return

  if (!player.isCreative()) event.item.shrink(1)

  const pledges = player.persistentData.getInt('sgPledges') + 1
  player.persistentData.putInt('sgPledges', pledges)

  let rank, team
  if (pledges >= 50)      { rank = 'Shadow Lord';   team = 'sg_lord' }
  else if (pledges >= 25) { rank = 'Seven Shadows'; team = 'sg_seven' }
  else if (pledges >= 10) { rank = 'Numbers';       team = 'sg_numbers' }
  else                    { rank = 'Shadow';        team = 'sg_shadow' }

  player.runCommandSilent(`team join ${team} @s`)
  player.tell(Text.aqua(`Shadow Garden — Rang [${rank}]  (${pledges} Pledges)`))
})

// ═════════════════════════════════════════════════════════════════════════════
// 5) MITSUGOSHI-TARNUNG (Toggle) + INFRASTRUKTUR-HINWEIS
//    Aktiviert die Kult-Invasions-Mechanik fuer den Spieler/seine Kolonie.
// ═════════════════════════════════════════════════════════════════════════════
ItemEvents.rightClicked('tensura_abyss:mitsugoshi_ledger', event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return

  const on = !player.persistentData.getBoolean('sgMitsugoshi')
  player.persistentData.putBoolean('sgMitsugoshi', on)

  if (on) {
    player.tell(Text.green('Mitsugoshi-Tarnung AKTIV — deine Kolonie farmt heimlich fuer Shadow Garden.'))
    player.tell(Text.gray('Warnung: Der Kult von Diablos kann nun Raids starten.'))
  } else {
    player.tell(Text.gray('Mitsugoshi-Tarnung deaktiviert — keine Kult-Raids mehr.'))
  }
})

// ═════════════════════════════════════════════════════════════════════════════
// 6) SERVER-TICK: Slime-Suit Set-Bonus + Kult-Invasionen
// ═════════════════════════════════════════════════════════════════════════════
ServerEvents.tick(event => {
  SG_SERVER = event.server
  SG_TICK++

  const players = event.server.players
  const count = players.size()

  // ── Slime Suit Set-Bonus (Stealth): Speed + Resistance + Invisibility ──
  // EIN gekettetes /execute-if-items prueft alle 4 Slots -> nur bei vollem Set.
  // "true" am Ende = Partikel verstecken (echter Stealth, kein Leuchten).
  if (SG_TICK % SUIT_INTERVAL_TICKS === 0) {
    const check = 'execute if items entity @s armor.head tensura_abyss:slime_suit_helmet' +
      ' if items entity @s armor.chest tensura_abyss:slime_suit_chestplate' +
      ' if items entity @s armor.legs tensura_abyss:slime_suit_leggings' +
      ' if items entity @s armor.feet tensura_abyss:slime_suit_boots run '
    for (let i = 0; i < count; i++) {
      const p = players.get(i)
      p.runCommandSilent(check + 'effect give @s minecraft:speed 3 1 true')
      p.runCommandSilent(check + 'effect give @s minecraft:resistance 3 1 true')
      p.runCommandSilent(check + 'effect give @s minecraft:invisibility 3 0 true')
    }
  }

  // ── Kult von Diablos: zufaellige Raids auf Mitsugoshi-Spieler ──
  if (SG_TICK % RAID_INTERVAL_TICKS === 0) {
    for (let i = 0; i < count; i++) {
      const p = players.get(i)
      if (!p.persistentData.getBoolean('sgMitsugoshi')) continue
      if (Math.random() > RAID_CHANCE) continue

      p.tell(Text.aqua('Der Kult von Diablos greift dein Mitsugoshi-Imperium an!'))
      p.runCommandSilent('playsound minecraft:event.raid.horn master @s ~ ~ ~ 4 1')

      const wave = 4 + Math.floor(Math.random() * 4) // 4-7 Kultisten
      for (let k = 0; k < wave; k++) {
        const ox = (Math.random() * 16 - 8).toFixed(1)
        const oz = (Math.random() * 16 - 8).toFixed(1)
        p.runCommandSilent(`summon minecraft:pillager ~${ox} ~1 ~${oz} {Tags:["cult_of_diablos"],PersistenceRequired:1b,CustomName:'{"text":"Kultist von Diablos","color":"dark_purple"}'}`)
      }
      // Elite-Ritter (droppt garantiert mehr Insignien)
      p.runCommandSilent(`summon minecraft:vindicator ~ ~1 ~ {Tags:["cult_of_diablos","cult_knight"],PersistenceRequired:1b,CustomName:'{"text":"Diablos-Ritter","color":"dark_red"}'}`)
    }
  }
})

// ═════════════════════════════════════════════════════════════════════════════
// 7) KULT-DROPS: Insignien beim Tod von Kult-Mobs (Slime-Suit-Material)
//    Hinweis: Drop wird an den Todeskoordinaten in der Overworld gespawnt
//    (Raids finden ueblicherweise an der Overworld-Basis statt).
// ═════════════════════════════════════════════════════════════════════════════
EntityEvents.death(event => {
  const e = event.entity
  if (!e || e.isPlayer()) return
  if (!e.tags.contains('cult_of_diablos')) return
  if (!SG_SERVER) return

  const knight = e.tags.contains('cult_knight')

  // Kult-Anfuehrer (Diablos-Ritter) besiegt -> Evolutions-Bedingung fuer [Numbers].
  // Killer ermitteln (best effort); markiert nahe Spieler als Bezwinger.
  if (knight) {
    let killer = null
    try { killer = event.source.player } catch (err) {}
    if (!killer) { try { killer = event.source.getActual() } catch (err) {} }
    if (killer && killer.persistentData) {
      killer.persistentData.putBoolean('sgKilledCultLeader', true)
    } else {
      // Fallback: alle Spieler im Umkreis von 24 Blocks markieren
      SG_SERVER.runCommandSilent(
        `execute positioned ${e.x} ${e.y} ${e.z} as @a[distance=..24] run scoreboard players set @s sg_cult_leader 1`)
    }
  }

  const drop = knight ? (2 + Math.floor(Math.random() * 3)) : (Math.random() < 0.6 ? 1 : 0)
  if (drop <= 0) return
  SG_SERVER.runCommandSilent(`summon item ${e.x} ${e.y} ${e.z} {Item:{id:"tensura_abyss:cult_insignia",count:${drop}}}`)
})
