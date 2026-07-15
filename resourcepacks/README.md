# Resource Pack — Tensura Abyss / Shadow Garden

Atmospheric icon set + pack metadata for the modpack's own resource pack.

## Install

Copy the folder `TensuraAbyss_ShadowGarden/` into your instance:

```
<instance>/resourcepacks/TensuraAbyss_ShadowGarden/
├── pack.mcmeta
└── pack.png        (128×128 modpack/resource-pack icon)
```

Then enable it in-game: **Options → Resource Packs → move to the right**.
Or ship it enabled via `options.txt` (`resourcePacks:["file/TensuraAbyss_ShadowGarden"]`).

- `pack_format: 34` targets Minecraft **1.21.1**.
- `pack.png` is the icon shown in the resource-pack list: a glowing teal
  Rimuru-slime orb over the void, ringed by a faint Shadow-Garden rune
  circle and a cyan/purple particle field.

## Regenerate the icon

```
python3 tools/gen_textures.py
```

It rewrites `pack.png` (function `pack_icon()` in `tools/gen_textures.py` —
tweak colors/particles there).

> Note: the KubeJS custom-item textures (Dark Aether, Slime Suit, …) live under
> `kubejs/assets/` and load via KubeJS automatically — they are **not** part of
> this resource pack and don't need it enabled.
