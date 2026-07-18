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

// Central balancing values ("I Am Atomic" now lives in IAmAtomicItem.java)
const RAID_INTERVAL_TICKS = 1200  // check every 60s
const RAID_CHANCE = 0.5           // 50% per interval (only with active Mitsugoshi cover)
const SUIT_INTERVAL_TICKS = 40    // refresh set bonus every 2s

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

  // Insignia of False Eminence — endgame rank-forging relic.
  // Deliberately brutal cost: post-Wither, deep Abyss progression required.
  event.shaped('tensura_abyss:false_eminence_insignia', [
    'APA',
    'DND',
    'ACA'
  ], {
    A: 'tensura_abyss:dark_aether',
    N: 'minecraft:nether_star',
    D: 'tensura_abyss:dark_slime',
    P: 'tensura_abyss:shadow_pledge_note',
    C: 'tensura_abyss:cult_insignia'
  }).id('tensurapack:false_eminence_insignia')

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
//    Handled ENTIRELY by the companion mod (IAmAtomicItem.java) — the Java item
//    checks the live race (eminence_of_the_abyss) + EP gate, spawns the thorn
//    field, applies AoE damage and the cooldown. The old KubeJS duplicate
//    handler was removed because it double-fired and produced conflicting
//    action-bar/chat requirement messages.
// ═════════════════════════════════════════════════════════════════════════════

// ═════════════════════════════════════════════════════════════════════════════
// 3) DARK AETHER -> ABYSS ATTUNEMENT
//    Right-click consumes 1x Dark Aether and deepens the player's attunement.
//    (Sneak + right-click stays reserved for the evolution trigger in
//    shadow_race_trees.js — ignored here.)
// ═════════════════════════════════════════════════════════════════════════════
ItemEvents.rightClicked('tensura_abyss:dark_aether', event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  if (player.isCrouching()) return

  if (!player.isCreative()) event.item.shrink(1)

  const breath = player.persistentData.getInt('sgBreathTier') + 1
  player.persistentData.putInt('sgBreathTier', breath)

  player.runCommandSilent('playsound minecraft:block.beacon.power_select master @s ~ ~ ~ 1 1.2')
  player.runCommandSilent('particle minecraft:soul_fire_flame ~ ~1 ~ 0.4 0.8 0.4 0.02 80 force')

  player.tell(Text.of(`§5The Dark Aether resonates within you §8— §7Abyss Attunement §fLevel ${breath}§7.`))
})

// ═════════════════════════════════════════════════════════════════════════════
// 4) SHADOW-GARDEN RANG-SYSTEM (Chat-Praefixe via Scoreboard-Teams)
//    Vanilla-Teams treiben Chat- UND Nametag-Praefix -> mod-unabhaengig robust.
// ═════════════════════════════════════════════════════════════════════════════
ServerEvents.loaded(event => {
  const s = event.server
  // Teams carry NO prefix anymore: rank visibility over heads is handled
  // client-side by ShadowSightHandler (only shadow-race viewers see ranks,
  // and the Insignia of False Eminence can spoof/mask them). Chat formatting
  // is owned by shadow_chat.js. Teams remain the rank DATA carrier + color.
  const teams = [
    ['sg_shadow',  'gray'],
    ['sg_numbers', 'aqua'],
    ['sg_seven',   'dark_aqua'],
    ['sg_lord',    'dark_purple']
  ]
  teams.forEach(t => {
    s.runCommandSilent(`team add ${t[0]}`)
    s.runCommandSilent(`team modify ${t[0]} prefix {"text":""}`)
    s.runCommandSilent(`team modify ${t[0]} color ${t[1]}`)
  })
})

// SHADOW GARDEN PLEDGE — the organisation's membership token.
// Right-click consumes the pledge and raises the player's HIDDEN faction
// reputation with Shadow Garden (sgReputation). Rank thresholds assign the
// matching scoreboard team (chat/nametag prefix). Styled in the faction's
// stealth palette: §5 dark purple / §8 dark gray / §f white accents.
ItemEvents.rightClicked('tensura_abyss:shadow_pledge_note', event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return

  if (!player.isCreative()) event.item.shrink(1)

  const pledges = player.persistentData.getInt('sgPledges') + 1
  player.persistentData.putInt('sgPledges', pledges)

  // Hidden reputation: pledges weigh more the deeper you are in the order.
  const repGain = 5 + Math.floor(pledges / 10) * 2
  const rep = player.persistentData.getInt('sgReputation') + repGain
  player.persistentData.putInt('sgReputation', rep)

  let rank, team, next
  if (pledges >= 50)      { rank = 'Shadow Lord';   team = 'sg_lord';    next = null }
  else if (pledges >= 25) { rank = 'Seven Shadows'; team = 'sg_seven';   next = 50 }
  else if (pledges >= 10) { rank = 'Numbers';       team = 'sg_numbers'; next = 25 }
  else                    { rank = 'Shadow';        team = 'sg_shadow';  next = 10 }

  player.runCommandSilent(`team join ${team} @s`)
  player.runCommandSilent('playsound minecraft:block.sculk_shrieker.hit master @s ~ ~ ~ 0.6 0.6')
  player.runCommandSilent('particle minecraft:squid_ink ~ ~1.2 ~ 0.25 0.4 0.25 0.02 25 force')

  player.tell(Text.of('§8── §5§lShadow Garden§r §8──'))
  player.tell(Text.of(`§7Your pledge has been accepted. §8(§f${pledges}§8 sworn§8)`))
  player.tell(Text.of(`§7Standing: §5${rank}§8 · §7Reputation §f+${repGain}`))
  if (next) player.tell(Text.of(`§8Next rank at ${next} pledges.`))
  else player.tell(Text.of('§8You stand at the apex of the order.'))
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
    player.tell(Text.of('§5Mitsugoshi cover §aACTIVE §8— §7your colony now quietly funnels resources to Shadow Garden.'))
    player.tell(Text.of('§8Warning: the Cult of Diablos may now launch raids against you.'))
  } else {
    player.tell(Text.of('§7Mitsugoshi cover disabled §8— no further cult raids.'))
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

      p.tell(Text.of('§5The Cult of Diablos is raiding your Mitsugoshi empire!'))
      p.runCommandSilent('playsound minecraft:event.raid.horn master @s ~ ~ ~ 4 1')

      const wave = 4 + Math.floor(Math.random() * 4) // 4-7 cultists
      for (let k = 0; k < wave; k++) {
        const ox = (Math.random() * 16 - 8).toFixed(1)
        const oz = (Math.random() * 16 - 8).toFixed(1)
        p.runCommandSilent(`summon minecraft:pillager ~${ox} ~1 ~${oz} {Tags:["cult_of_diablos"],PersistenceRequired:1b,CustomName:'{"text":"Diablos Cultist","color":"dark_purple"}'}`)
      }
      // Elite knight (guaranteed extra insignia drops)
      p.runCommandSilent(`summon minecraft:vindicator ~ ~1 ~ {Tags:["cult_of_diablos","cult_knight"],PersistenceRequired:1b,CustomName:'{"text":"Diablos Knight","color":"dark_red"}'}`)
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
