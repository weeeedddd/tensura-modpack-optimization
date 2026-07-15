// KubeJS 6 — Tensura Abyss: "Eminence in Shadow" ⇄ Tensura Evolutions-Kopplung
// server_scripts/ — hot-reloadbar mit /kubejs reload server_scripts
//
// Koppelt die Shadow-Garden-Raenge (Possessed → Delta → Gamma → Beta → Alpha)
// DIREKT an Tensura-Reincarnated-Fortschritt: Magicule-Schwelle + Katalysator-
// Items (Dunkler Aether/Schleim) + optionale Tensura-Skill/Evo-Gates.
//
// ┌───────────────────────────────────────────────────────────────────────────┐
// │ EHRLICH & WICHTIG:                                                          │
// │ Die EXAKTEN Tensura-Datenpfade/Commands (Magicule lesen, Skill pruefen,     │
// │ Evolution ausloesen) kenne ich nicht ohne laufendes Spiel. Deshalb ist die  │
// │ Logik so gebaut, dass sie HEUTE funktioniert (Magicule via Scoreboard,      │
// │ Katalysatoren via /clear-Zaehlung — beides 100% robust), und die echten     │
// │ Tensura-Hooks als klar markierte, ein-kommentierbare Zeilen bereitstehen.   │
// └───────────────────────────────────────────────────────────────────────────┘
//
// Ausloeser: SNEAK + Rechtsklick mit Dunklem Aether (Evolution).
//            Normaler Rechtsklick mit Dunklem Aether = Breath (shadow_garden.js).
//            Rechtsklick mit Dunklem Schleim = Slime-Faehigkeiten maxen.

// ═══════════════════════════════════════════════════════════════════════════
// CONFIG — hier die realen Tensura-Werte anpassen/verifizieren
// ═══════════════════════════════════════════════════════════════════════════

// Scoreboard, das den Tensura-Magicule/EP-Wert SPIEGELT.
// -> Siehe unten "MAGICULE-SYNC" wie man das an Tensura koppelt.
const MAGICULE_OBJ = 'sg_magicule'

// Rang-Leiter (aufsteigend). team = Praefix-Team aus shadow_garden.js.
// magicule = benoetigter Magicule/EP-Wert. aether/slime = Katalysator-Kosten.
// tensuraEvo = OPTIONAL: exakte Tensura-Evolutions-/Rassen-ID (verifizieren!).
const RANKS = [
  { id: 'possessed', name: 'Possessed',     team: 'sg_shadow',  magicule: 0,     aether: 0, slime: 0, tensuraEvo: '' },
  { id: 'delta',     name: 'Delta',         team: 'sg_numbers', magicule: 2000,  aether: 1, slime: 0, tensuraEvo: '' },
  { id: 'gamma',     name: 'Gamma',         team: 'sg_numbers', magicule: 6000,  aether: 2, slime: 2, tensuraEvo: '' },
  { id: 'beta',      name: 'Beta',          team: 'sg_seven',   magicule: 15000, aether: 3, slime: 4, tensuraEvo: '' },
  { id: 'alpha',     name: 'Alpha',         team: 'sg_lord',    magicule: 40000, aether: 5, slime: 8, tensuraEvo: '' }
]

// ═══════════════════════════════════════════════════════════════════════════
// Helfer
// ═══════════════════════════════════════════════════════════════════════════
function isSneaking(p) {
  try { return p.isCrouching() } catch (e) {
    try { return p.crouching } catch (e2) {
      try { return p.isShiftKeyDown() } catch (e3) { return false }
    }
  }
}
// zaehlt passende Items ohne zu entfernen: /clear @s <item> 0 gibt die Anzahl zurueck
function countItem(player, id) {
  return player.runCommandSilent(`clear @s ${id} 0`)
}
// aktueller Rang-Index aus persistentData (Default 0 = Possessed)
function rankIndex(player) {
  return player.persistentData.getInt('sgEvoRank') // 0, wenn nicht gesetzt
}

// Scoreboard-Objektiv sicherstellen
ServerEvents.loaded(event => {
  event.server.runCommandSilent(`scoreboard objectives add ${MAGICULE_OBJ} dummy "Magicule"`)
})

// ═══════════════════════════════════════════════════════════════════════════
// EVOLUTION: Sneak + Rechtsklick mit Dunklem Aether
// ═══════════════════════════════════════════════════════════════════════════
ItemEvents.rightClicked('kubejs:dark_aether', event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  if (!isSneaking(player)) return   // normaler Rechtsklick = Breath (shadow_garden.js)

  const idx = rankIndex(player)
  if (idx >= RANKS.length - 1) {
    player.tell(Text.gold(`Du hast bereits den hoechsten Rang: [${RANKS[idx].name}].`))
    return
  }
  const next = RANKS[idx + 1]

  // ── 1) Magicule-Gate (Tensura-Fortschritt, via Scoreboard) ──
  const hasMag = player.runCommandSilent(`execute if score @s ${MAGICULE_OBJ} matches ${next.magicule}..`)
  if (hasMag < 1) {
    const cur = player.runCommandSilent(`scoreboard players get @s ${MAGICULE_OBJ}`)
    player.tell(Text.gray(`Zu wenig Magicule: ${cur} / ${next.magicule} fuer [${next.name}].`))
    player.tell(Text.gray('(Magicule steigt mit deinem Tensura-Fortschritt — siehe MAGICULE-SYNC.)'))
    return
  }

  // ── 2) OPTIONALE Tensura-Skill/Evo-Stufen-Gates (verifizieren & aktivieren) ──
  // Beispiel Skill-Gate (wenn Tensura die Daten via /data lesbar macht):
  //   const hasSkill = player.runCommandSilent('execute if data entity @s <TENSURA_SKILL_PFAD>')
  //   if (hasSkill < 1) { player.tell(Text.gray('Benoetigt Tensura-Skill: ...')); return }
  // Beispiel Evo-Stufen-Gate:
  //   const stage = player.runCommandSilent('execute if data entity @s <TENSURA_EVO_STAGE_PFAD>')
  //   if (stage < 1) { player.tell(Text.gray('Benoetigt Tensura-Evolutionsstufe: ...')); return }

  // ── 3) Katalysator-Check (Dunkler Aether/Schleim) ──
  const haveAether = countItem(player, 'kubejs:dark_aether')
  const haveSlime  = next.slime > 0 ? countItem(player, 'kubejs:dark_slime') : 0
  if (haveAether < next.aether || haveSlime < next.slime) {
    player.tell(Text.gray(`Katalysator fehlt: ${next.aether}x Dunkler Aether` +
      (next.slime > 0 ? ` + ${next.slime}x Dunkler Schleim` : '') +
      ` (du hast ${haveAether}/${haveSlime}).`))
    return
  }

  // ── 4) Alles erfuellt: Katalysator verbrauchen ──
  if (!player.isCreative()) {
    if (next.aether > 0) player.runCommandSilent(`clear @s kubejs:dark_aether ${next.aether}`)
    if (next.slime > 0)  player.runCommandSilent(`clear @s kubejs:dark_slime ${next.slime}`)
  }

  // ── 5) Rang setzen + Praefix-Team ──
  player.persistentData.putInt('sgEvoRank', idx + 1)
  player.runCommandSilent(`team join ${next.team} @s`)

  // ── 6) Tensura-Evolution ausloesen (ECHTER HOOK — Syntax verifizieren!) ──
  // Sobald du die exakte Tensura-Command-Syntax kennst, hier aktivieren.
  // Die Rassen/Evo-ID steht pro Rang in RANKS[].tensuraEvo.
  // if (next.tensuraEvo) {
  //   player.runCommandSilent(`tensura evolution set @s ${next.tensuraEvo}`)
  // }

  // ── 7) Evolutions-Inszenierung (Partikel/Sound, keine Blockschaeden) ──
  player.runCommandSilent('playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 4 1.3')
  player.runCommandSilent('particle minecraft:end_rod ~ ~1 ~ 0.6 1.2 0.6 0.06 220 force')
  player.runCommandSilent('particle minecraft:soul_fire_flame ~ ~1 ~ 0.6 1.2 0.6 0.03 200 force')
  player.runCommandSilent('effect give @s minecraft:strength 20 0 true')
  player.runCommandSilent('effect give @s minecraft:regeneration 10 1 true')

  player.tell(Text.aqua(`§lEVOLUTION§r§b — Shadow Garden Rang [${next.name}] erreicht!`))
})

// ═══════════════════════════════════════════════════════════════════════════
// DUNKLER SCHLEIM: Tensura-Schleim-Faehigkeiten maxen (Rechtsklick, kein Sneak)
// ═══════════════════════════════════════════════════════════════════════════
ItemEvents.rightClicked('kubejs:dark_slime', event => {
  const { player, level, hand } = event
  if (level.isClientSide()) return
  if (hand !== 'main_hand') return
  if (isSneaking(player)) return   // Sneak fuer Schleim ungenutzt -> frei fuer Zukunft

  if (!player.isCreative()) event.item.shrink(1)

  const tier = player.persistentData.getInt('sgSlimeMastery') + 1
  player.persistentData.putInt('sgSlimeMastery', tier)

  player.runCommandSilent('playsound minecraft:entity.slime.squish master @s ~ ~ ~ 1 0.8')
  player.runCommandSilent('particle minecraft:item_slime ~ ~1 ~ 0.4 0.6 0.4 0.1 40 force')

  // >>> ECHTER TENSURA-HOOK (Syntax verifizieren): Schleim-Skill maxen <<<
  // player.runCommandSilent('tensura skill level @s predator_slime max')

  player.tell(Text.aqua(`Dunkler Schleim absorbiert — Schleim-Meisterschaft Stufe ${tier}.`))
  player.tell(Text.gray('(Tensura-Skill-Command in shadow_evos.js aktivieren, siehe Kommentar.)'))
})

// ═══════════════════════════════════════════════════════════════════════════
// MAGICULE-SYNC — wie sg_magicule an Tensura gekoppelt wird
// ═══════════════════════════════════════════════════════════════════════════
// sg_magicule ist ein Scoreboard-Proxy fuer den Tensura-Magicule/EP-Wert.
// Optionen, ihn zu fuellen (eine waehlen):
//
//   A) Falls Tensura den Wert via /data entity lesbar macht, hier periodisch
//      synchronisieren (exakten Pfad verifizieren):
//        ServerEvents.tick(e => {
//          if (e.server.tickCount % 100 !== 0) return
//          const players = e.server.players
//          for (let i = 0; i < players.size(); i++) {
//            const p = players.get(i)
//            p.runCommandSilent('execute store result score @s sg_magicule run data get entity @s <TENSURA_MAGICULE_PFAD>')
//          }
//        })
//
//   B) Falls Tensura ein eigenes Scoreboard/Command bereitstellt, dieses direkt
//      als MAGICULE_OBJ oben eintragen (kein Sync noetig).
//
//   C) Manuell/Debug: /scoreboard players set <Spieler> sg_magicule <Wert>
//      oder ueber FTB-Quests-Rewards den Score erhoehen.
//
// Bis A/B eingerichtet ist, steuert C den Fortschritt — die Kopplung selbst
// (Gates, Katalysatoren, Raenge, Evolutionen) funktioniert bereits vollstaendig.
