# Tensura Abyss Modpack Optimization Guide

Complete analysis and optimization guide for the **Tensura: Reincarnated** modpack (That Time I Got Reincarnated as a Slime).

## 📋 Contents

**01. Updates & Crash-Check**
- Tensura: Reincarnated v2.0.1.1 (July 2026)
- Known incompatibilities (BYG, Eldritch End, Epic Fight, Apotheosis)
- Crash prevention strategies

**02. Tensura Add-ons (24+)**
- Tier S: Must-haves (Mysticism 1M+ downloads, Not Enough Bosses, SlimeThrone Extras)
- Tier A: Strong additions (Better Subordinates, No Game No Life, Dungeon)
- Tier B: Solid expansions (TensuraMoreSkills, Ascension, Creation)
- Tier C: Utility & niche addons

**03. Mouse Sensitivity Bug Fix**
- Root causes (OptiFine, BetterFPS, Controllable, modpack launchers)
- Step-by-step permanent fixes
- Windows system settings optimization

**04. Graphics & Performance**
- Embeddium stack for Forge/NeoForge
- Sodium stack for Fabric
- Optimal video settings (render distance, simulation distance, etc.)
- Shader recommendations (Complementary Unbound, BSL v10.1.3)
- Visual enhancement mods with minimal FPS impact
- RAM allocation guidance

## 🎮 Quick Start

1. **Mouse Fix**: Set `mouseSensitivity:0.5` in `options.txt` → Make file read-only
2. **Performance**: Install Embeddium + Entity Culling + ModernFix + FerriteCore
3. **Replace OptiFine**: Use Embeddium + Oculus (Forge) or Sodium + Iris (Fabric)
4. **Shaders**: Complementary Unbound for best balance, or BSL for customization
5. **Video Settings**: renderDistance=10, simulationDistance=8, graphicsMode=1 (Fancy)

## 📊 Key Statistics

- **Main Mod Downloads**: 1.8M (CurseForge) + 1.14M (Modrinth)
- **Latest Version**: 2.0.1.1 (July 5, 2026)
- **Supported Versions**: 1.21.1 (NeoForge/Fabric), 1.19.2 (Forge - legacy)
- **Add-ons Documented**: 24+ (from Mysticism with 1M downloads to niche utility)

## 🔧 Dependencies

- **ManasCore** (~1.5M downloads)
- **GeckoLib** (animation engine)
- **TerraBlender** (biome generation)

## ⚙️ Performance Stack (Forge/NeoForge)

**Rendering:**
- Embeddium (replaces Sodium/Rubidium)
- Oculus/NeOculus (shader support)

**Optimization:**
- Entity Culling (skip invisible entities)
- ModernFix (startup time, memory, load time)
- FerriteCore (memory usage, ~50% reduction)
- ImmediatelyFast (GUI/text/entity rendering)

**Optional:**
- Embeddium Extras (additional settings)
- Starlight (faster lighting updates)
- Clumps (group XP orbs)

## 🎨 Graphics Recommendations

| Use Case | Shader | FPS (GTX 1660) | Settings |
|----------|--------|----------------|---------:|
| Best Balance | Complementary Unbound | 80-110 | High (2048 shadow res) |
| Most Options | BSL v10.1.3 | 75-100 | High (disable bloom/DOF) |
| Low-End | Sildur's Vibrant Lite | 120+ | Medium+ |
| Ultra | Complementary Reimagined | 55-80 | High |

## 🐛 Known Issues

**Fixed in Recent Versions:**
- Duplication exploits (Kiln, Kunai, Summons)
- End dimension attribute bugs
- Labyrinth generation crashes

**Monitor:**
- Some race evolution requirements may be misconfigured
- Server startup issues with certain configurations

## 🔗 Resources

- [CurseForge - Tensura: Reincarnated](https://www.curseforge.com/minecraft/mc-mods/tensura-reincarnated)
- [Modrinth - Tensura: Reincarnated](https://modrinth.com/mod/tensura-reincarnated)
- [Official Wiki](https://tensura.wiki.gg/)
- [ManasMods GitHub](https://github.com/ManasMods)

## 📄 Guide Format

The main guide (`tensura-modpack-guide.html`) is a standalone HTML document with:
- Dark/light theme support
- Responsive design
- Searchable content
- Collapsible sections
- Code snippets for configs

## 🤝 Contributing

Found a new addon? Mod compatibility issue? Better performance tip? 
Please open an issue or submit a PR with:
- Addon name and link
- Download count / estimated popularity
- Compatibility notes
- Why it's worth including

---

**Last Updated**: July 12, 2026  
**Model**: Claude Opus 4.6 / Claude Haiku 4.5  
**Guide Version**: 2.0 (Complete)
