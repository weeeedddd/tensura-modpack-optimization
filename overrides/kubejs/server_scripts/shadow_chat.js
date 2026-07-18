// KubeJS — Tensura Abyss: unified chat formatting (prefix-overlap fix)
//
// PROBLEM: the pledge system assigns vanilla scoreboard teams (sg_shadow,
// sg_numbers, ...) whose prefixes render in chat AND stack on top of any
// global rank prefix (LuckPerms etc.), producing overlapping, unreadable
// lines like "[Shadow][Member] [Shadow] name ...".
//
// FIX: this handler takes full ownership of the chat line. It cancels the
// vanilla broadcast and emits ONE clean, deterministic format:
//
//     [Shadow Rank] Name » message          (faction members)
//     Name » message                        (everyone else — LP prefix intact
//                                            via the player's display name)
//
// The scoreboard teams KEEP their prefixes for NAMETAGS above the head —
// those don't stack with chat anymore because chat never uses them again.

const SHADOW_TEAM_RANKS = {
  sg_lord:    { label: 'Shadow Lord',   color: '§5' },
  sg_seven:   { label: 'Seven Shadows', color: '§d' },
  sg_numbers: { label: 'Numbers',       color: '§b' },
  sg_shadow:  { label: 'Shadow',        color: '§7' }
}

PlayerEvents.chat(event => {
  const { player, message, server } = event

  // Which shadow rank (if any) does the sender hold?
  let rank = null
  try {
    const team = player.getTeam()
    if (team) rank = SHADOW_TEAM_RANKS[String(team.getName())] || null
  } catch (e) { rank = null }

  // Non-members keep the default pipeline (LuckPerms & friends untouched).
  if (!rank) return

  // Members get the unified, overlap-free format.
  event.cancel()
  const name = player.getGameProfile().getName()
  server.tell(Text.of(
    `§8[${rank.color}${rank.label}§8]§r §f${name} §8» §7${message}`
  ))
})
