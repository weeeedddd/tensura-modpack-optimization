# Tensura Abyss texture system

All companion-mod textures are reproducible with:

```text
python dev/tools/gen_textures.py
```

The generator uses only Python's standard library and writes directly to
`dev/companion-mod/src/main/resources/assets/tensura_abyss/textures/`.

## Visual language

- Refined Dark Slime: teal organic sheen.
- Dark Aether: faceted cyan energy.
- Magicule Spire Crystal: violet, asymmetric mineral shard.
- Condensed Dark Matter: near-black core with a restrained purple orbit.
- Abyssal Netherite: obsidian-purple metal with a silver energy seam.
- Slime Suit: teal living armor with a soft rim.

The Abyssal set includes inventory icons and both 64×32 worn armor layers.
The removed I Am Atomic catalyst intentionally has no model or texture; Atomic
is a native race ability, not an item.

Palette ramps, glows, armor masks, item silhouettes, portal block texture, and
pack icon are all defined in `dev/tools/gen_textures.py`.
