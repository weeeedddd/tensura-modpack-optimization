# Tensura Abyss — Java Companion Mod

Companion mod for the **Tensura Abyss** modpack (NeoForge **1.21.1**). Handles the
"hard" registrations that KubeJS can't do robustly, and exposes a bridge into
Tensura's player data for the KubeJS gameplay scripts.

## What's in here

| Area | Status |
|------|--------|
| Items (`dark_slime`, `dark_aether`, `signature_record`) | ✅ vanilla/NeoForge only — real |
| Slime Suit armor + **own `ArmorMaterial`** → slime layers render on the body (no netherite fallback) | ✅ real (1.21.1 armor material + layer) |
| Creative tab, item models, textures, lang (de/en) | ✅ real |
| `TensuraBridge` (magicules / EP / race) | ⚠️ **reflection** — class/method names are constants to **verify** against the installed Tensura/Ascensions jars |
| Guild backend (SavedData: guilds, ranks, member numbers, `lastOnline`) | ✅ real |
| Party system + party chat (`/p`) | ✅ real |
| Commission engine (Gathering/Crafting/Subjugation/Coordinate/Party/Dwarf) | ✅ real data model + generator; completion driven from KubeJS |
| `/shadow …` commands, gated **invisible** to non-Tensura players | ✅ real |
| Minimap icons (Xaero/JourneyMap) | ⚠️ backend + `/shadow radar` real; **client icon render needs the minimap mod's API** (documented endpoint in `MinimapIntegration`) |

## Honest limits (read this)

1. **Not built in CI here.** The code is written against NeoForge 1.21.1 APIs but
   was not compiled in this environment. Run `./gradlew build` in IntelliJ to
   produce the jar and fix any mapping drift.
2. **Tensura internals** (`TensuraBridge`) use reflection so the mod compiles
   without Tensura on the classpath. The `private static final String` constants
   at the top of `TensuraBridge.java` are the **single place** to correct the
   real package/method names (get them from the installed jars or Robinator1103's
   open-source ArcanePotions repo). Until then the bridge degrades gracefully
   (returns 0 / no-op) and the KubeJS scripts fall back to the `sg_magicule`
   scoreboard.
3. **Minimap** member icons need Xaero's / JourneyMap's API — that integration is
   a documented endpoint, not shipped.

## Build (IntelliJ)

```bash
# 1. Import companion-mod/ as a Gradle project (JDK 21)
# 2. Adjust gradle.properties -> neo_version to match the pack (21.1.x)
./gradlew build          # -> build/libs/tensura-abyss-companion-1.0.0.jar
# 3. Drop the jar into the instance's mods/ folder (next to Tensura + Ascensions)
```

`neoforge.mods.toml` declares **hard** dependencies on `tensura` and
`tensura_ascensions` (ordering AFTER) so load order and presence are enforced.

## Namespace note (KubeJS ⇄ Mod)

The pack currently registers the items via KubeJS under `kubejs:*`. This mod
registers them under `tensura_abyss:*`. **Pick one owner:**

- **Use the mod (recommended):** set `const NS = 'tensura_abyss'` at the top of
  `kubejs/server_scripts/shadow_evos.js` and `shadow_recipes.js`, and delete the
  now-redundant `kubejs/startup_scripts/shadow_garden_items.js` +
  `shadow_garden_armor_tiers.js` (the mod supersedes them).
- **KubeJS only (no mod yet):** keep `NS = 'kubejs'`. The bridge calls no-op and
  the scoreboard fallback drives magicules.

The KubeJS scripts are written to work **both** ways.
