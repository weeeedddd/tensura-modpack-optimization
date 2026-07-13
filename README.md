# Tensura Abyss — Modpack Optimization Repo

Optimization guide, configs, and KubeJS scripts for the **Tensura Abyss** modpack
(Minecraft 1.21.1 · NeoForge · v3.1.5).

> **CurseForge:** https://legacy.curseforge.com/minecraft/modpacks/tensura-abyss

---

## Files

| File | Description |
|------|-------------|
| `tensura-modpack-guide.html` | Bilingual DE/EN landing page (open in browser) |
| `kubejs/server_scripts/tensura_balancing.js` | Loot table & recipe balancing templates |
| `kubejs/startup_scripts/tensura_fixes.js` | Cross-mod item/block tag assignments |
| `configs/options_optimized.txt` | Drop-in Minecraft options.txt with optimized settings |

---

## Quick Start

### 1 — Install
1. Install [CurseForge App](https://curseforge.com)
2. Search **"Tensura Abyss"** → Install
3. Set RAM to **6 GB** (Profile Settings → Java Settings → 6144 MB)
4. Use **Java 21**

### 2 — Performance (after install, inside the instance mods/ folder)
- **Required:** Embeddium + Oculus (already included via pack)
- **Never install:** OptiFine — causes sensitivity bugs + conflicts with Embeddium

### 3 — Mouse Fix (if sensitivity resets)
Copy `configs/options_optimized.txt` into your instance folder,
then set the file **read-only** (right-click → Properties → Read-only).

### 4 — KubeJS Balancing (optional, for customization)
Copy the `kubejs/` folder into your instance folder. Open the scripts,
find your item IDs with `/kubejs hand` in-game, then uncomment the relevant lines.

---

## Modpack Summary

| Property | Value |
|----------|-------|
| MC Version | 1.21.1 |
| Modloader | NeoForge |
| Pack Version | v3.1.5 (April 17, 2026) |
| Quests | 500+ (15 chapters, in development) |
| Focus | Hardcore survival RPG + colony building + automation |

### Core Mods

| Mod | Purpose |
|-----|---------|
| Tensura: Magic Growth | Skill system, race evolution, Demon Lord progression |
| EP Scaling | Dynamic enemy scaling with player progress |
| MineColonies | Colony building with AI citizens |
| MCA Reborn | NPC interaction, trade, family system |
| Create + Steam 'n' Rails | Automation + railway network |
| Ice and Fire CE | Dragons, griffins, sea serpents |
| Gateways to Eternity | Summon portal boss challenges |
| Cataclysm Loot | Boss drop system |
| Serene Seasons | Season system |
| RUNIC: Enchants | Rune-based enchanting overhaul |
| YDM's Weapon Master | Weapon mastery leveling |

---

## Recommended Tensura Add-ons (1.21.1)

| Tier | Addon | Highlight |
|------|-------|-----------|
| S | [Tensura: Mysticism](https://www.curseforge.com/minecraft/mc-mods/tensura-mysticism) | 9+ races, 29 Ultimate Skills — must have |
| S | [SlimeThrone Extras](https://www.curseforge.com/minecraft/mc-mods/tensura-slimethrone-extras) | Skill locking, prestige system, auto config patcher |
| S | [Not Enough Bosses](https://www.curseforge.com/minecraft/mc-mods/tensura-not-enough-bosses) | Rimuru, Carrion, Luminous boss fights (official) |
| A | [Better Subordinates](https://www.curseforge.com/minecraft/mc-mods/tensura-better-subordinates) | Train subordinates, transfer skills |
| A | [Reincarnated Addon](https://www.curseforge.com/minecraft/mc-mods/tr-addon) | Slime → Dark → Abyss → Eldritch Slime evolution |
| A | [Tensura: Ascension](https://www.curseforge.com/minecraft/mc-mods/tensura-ascensions) | 10 race lines, Hyperbolic Chamber dimension |

---

## Recommended Magic Mods (thematic fit, 1.21.1 NeoForge)

| Mod | Why it fits |
|-----|-------------|
| **Ars Nouveau** | Skill-based spellcrafting mirrors Rimuru's skill absorption |
| **Iron's Spells 'n Spellbooks** | Class magic + leveling fits Tensura's evolution system |
| **Malum** | Dark ritual magic + soul system fits the "Abyss" theme |

---

## Performance Stack

| Mod | Function | Priority |
|-----|----------|----------|
| Embeddium | Rendering engine (replaces OptiFine/Rubidium) | Required |
| Oculus / NeOculus | Shader support | Recommended |
| FerriteCore | RAM usage −50% | Recommended |
| ModernFix | Startup time + RAM patches | Recommended |
| Entity Culling | Skip invisible entities | Recommended |
| ImmediatelyFast | GUI/text/entity rendering | Optional |
| Clumps | Group XP orbs | Optional |

**RAM:** 6–8 GB · **Java:** 21 · **Shaders:** Complementary Unbound (best balance)

---

## Known Conflicts

| Mod | Issue | Fix |
|-----|-------|-----|
| Oh The Biomes You'll Go | Feature order cycle crash | Tensura-BYG Patch (1.19.2 only) |
| Eldritch End | Duplicate registry key | Remove mod or wait for update |
| Epic Fight Mod | Armor rendering conflict | Tensura Epic Fight Compatibility Datapack |
| Apotheosis | Mixin conflict with CombatRules | Load order: Apotheosis before Tensura |
| OptiFine | Sensitivity bugs, rendering conflicts | Remove completely, use Embeddium + Oculus |

---

## Testing Checklist

- [ ] Check `crash-reports/` after a fresh launch
- [ ] Filter `logs/latest.log` for ERROR and WARN
- [ ] F3 screen: TPS ≥ 18.0
- [ ] Test new world in Superflat (fastest world gen)
- [ ] Run `/kill @e` → TPS improvement = entity lag
- [ ] RAM monitor: ≤ 80% after 30 min of play
- [ ] After mod updates: check crash log for Mixin errors
- [x] **NEVER install OptiFine** (conflicts with Embeddium)

---

*Last Updated: July 2026 · MC 1.21.1 NeoForge · Pack v3.1.5*
