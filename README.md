# Tensura Abyss — v5.0.0 "Reborn from the Abyss"

A story-driven **"That Time I Got Reincarnated as a Slime" × "The Eminence in Shadow"**
modpack for **Minecraft 1.21.1 · NeoForge 21.1.222** — **rebuilt from the ground
up in v5**: every one of the **366 mods** was individually resolved by name,
categorized, curated (junk cut, gaps filled) and pinned to a verified
1.21.1 NeoForge build. The complete categorized list lives in
[`docs/MODLIST.md`](docs/MODLIST.md).

> **CurseForge:** https://legacy.curseforge.com/minecraft/modpacks/tensura-abyss
> **Pack site:** served from this repo via GitHub Pages (`index.html`)

---

## What's in the Rebuild (v5.0.0)

### ⚙️ Create & Automation — 8 mods
| Mod | Purpose |
|-----|---------|
| Create | Core kinetic automation |
| Create: Steam 'n' Rails | Railway network across the realms |
| Create Crafts & Additions | Electrical age bridge |
| Create Deco | Decorative Create blocks |
| Create: Connected | Seamless contraption extensions *(new)* |
| Create Ore Excavation | Automated ore drilling *(new)* |
| Create Confectionery | Chocolate & sweets *(new)* |
| Create: Copycats+ | Copycat block variants *(new)* |

### 🐉 Tensura & Addons — 13+ mods
Tensura: Reincarnated as the core, plus **Magic Growth, Not Enough Bosses,
Better Subordinates, Boss Structures, Ice & Fire Compat, Iron's Spells Compat,
MineColonies Compat, OPAC Compat, Trepu, Blensura, Nightmare's Tensura Utils**
— and our own **Tensura Abyss Companion** mod (see below).

### 🌍 World & Biomes
**Oh The Biomes We've Gone** + **Terralith** *(new)* layered via Lithostitched,
**Alex's Caves**, Enderscape, Nullscape, Darker Depths, Nether Depths Upgrade,
The Bumblezone, Dynamic Trees, Serene Seasons, and 60+ structure mods
(Moog's, YUNG's, Towns & Towers, Formations, …) plus the custom
**Shadow Abyss dimension** with three bespoke biomes.

### 🏠 Decoration & Building — big push in v4
**Macaw's suite ×10** *(new: Furniture, Doors, Trapdoors, Windows, Fences &
Walls, Roofs, Paths & Pavings, Lights & Lamps, Paintings — plus Bridges)*,
**Supplementaries + Amendments** *(new)*, **Handcrafted** *(new)*,
**Chipped** *(new)*, Fetzi's Asian Deco, Domum Ornamentum, MineColonies
style packs.

### ✨ Cosmetics & Immersion
3D Skin Layers, **Not Enough Animations** *(new)*, **First-person Model**
*(new)*, **Visuality: Reforged** *(new)*, **AmbientSounds 6** *(new)*, Falling
Leaves, Item Physic, Player Animator, Cosmetic Armor Reworked, Melody, Simple
Voice Chat, dynamic lights.

### 🚀 Performance stack
Sodium + Iris (+ Sodium Extras), Lithium, ModernFix, FerriteCore,
EntityCulling, BadOptimizations, ScalableLux, ServerCore, Noisium,
Structure Layout Optimizer — **no OptiFine, ever.**

---

## The Companion Mod (`tensura_abyss`)

Custom NeoForge mod (source in `dev/companion-mod/`) that wires our content
natively into Tensura/ManasCore:

- **37 custom races** in 4 evolution paths (Shadow Slime · Shadow Demon ·
  Ancient Shadow Hero · Progenitor Vampire), 9 stages each, registered in the
  **native Tensura menu** with full descriptions, stats and EP-gated
  evolution chains — plus a secret 38th race earned through a hidden quest.
- **Shadow Garden guild system** — `/shadowguild` opens the obsidian-styled
  guild GUI; parties, commissions, invites, the Mitsugoshi black market.
- **Slime Sword** — auto-granted on entering the Shadow path. Shadow Step
  blink (darkness only) + Form Shift: Aegis defensive stance.
- **Rank sight & deception** — only shadow races see faction ranks over
  heads; the *Insignia of False Eminence* forges or masks them.
- **I Am Atomic** — the ultimate skill, gated behind Eminence of the Abyss.
- **Cult of Diablos** — challenge events, raids, insignia drops.
- **Dark violet UI reskin** of the Tensura reincarnation menu (bundled
  always-on resource pack).

> The compiled companion jar is **not** on CurseForge: build it with
> `dev/companion-mod/gradlew build` and ship it in `overrides/mods/`.

---

## Quick Start

1. Install the [CurseForge App](https://curseforge.com)
2. Search **"Tensura Abyss"** → Install
3. RAM: **8 GB** recommended (Profile Settings → Java Settings → 8192 MB)
4. **Java 21** (bundled with the launcher for 1.21.1)
5. First launch takes a while (371 mods) — later launches are much faster
   thanks to ModernFix.

**Never install OptiFine** — the pack renders through Sodium + Iris.
Shaders: any Iris-compatible pack (Complementary works great).

---

## Repository Structure

```
manifest.json            ← CurseForge mod list (366 mods, category-ordered)
index.html               ← Landing page (GitHub Pages)
overrides/               ← Instance payload shipped to players
├── config/
│   ├── tensura/reincarnation_config.toml  ← adds Abyss starter races to the menu
│   ├── tensura/ascension-races.toml
│   └── ftbquests/…
├── kubejs/
│   ├── data/…           ← Shadow Abyss dimension, biomes, worldgen features
│   ├── server_scripts/  ← recipes, faction systems, chat, quests
│   └── startup_scripts/ ← auxiliary item registration
└── resourcepacks/
dev/                     ← Source & tooling (NOT shipped)
├── companion-mod/       ← the tensura_abyss NeoForge mod (Gradle)
└── tools/               ← generators
docs/                    ← guides & notes (NOT shipped)
```

---

## Building a Release

```bash
zip -r tensura-abyss-5.0.0.zip manifest.json overrides
```

Upload via [console.curseforge.com](https://console.curseforge.com) → your
project → Files. (Remember: build the companion jar into `overrides/mods/`
first.)

---

## Removed in the v5 Rebuild

Cut on purpose (see `docs/MODLIST.md` for the full table): Gemini Live
Library, BisectHosting menu, Server Country Flags, TownTalk, and four
redundant shader packs (Complementary Unbound + Reimagined stay).
New in v5: **Farmer's Delight**, **Waystones**, **Sound Physics Remastered**.

## Compatibility Notes (v5.0.0)

- Every mod in the manifest was resolved against CurseForge for an explicit
  **1.21.1 NeoForge** build — no Fabric/legacy-Forge jars.
- **Terralith + Oh The Biomes We've Gone** coexist via Lithostitched (both
  inject, neither overwrites the other).
- **Moonlight Lib** is included as the required dependency for
  Supplementaries/Amendments; Chipped's deps (Resourceful Lib, Athena) and
  AmbientSounds' dep (CreativeCore) were already in the pack.
- The Shadow Abyss dimension runs on vanilla noise (`minecraft:overworld`) —
  independent of any terrain mod, so worldgen mods can't break it.
- **Do not add:** OptiFine (breaks Sodium), Tectonic (conflicts with the
  layered worldgen stack).

---

## Testing Checklist

- [ ] Fresh launch → `crash-reports/` empty
- [ ] `logs/latest.log` → no `RegistryDataLoader` errors, KubeJS loads 9/9 scripts
- [ ] New world → Reincarnation menu shows the 4 Abyss starter races
- [ ] Evolution menu shows next stages with EP bars (no dead ends)
- [ ] `/shadowguild` opens the guild GUI
- [ ] Shadow Abyss portal → terrain has abyssal ground/flora (new chunks only)

---

*Last updated: July 2026 · MC 1.21.1 · NeoForge 21.1.222 · Pack v5.0.0 · 366 mods*
