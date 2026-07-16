# Shadow Garden — Textur-Designs / Texture Design Specs

Alle Icons sind reproduzierbar über `python3 tools/gen_textures.py`
(reines Python, kein PIL). Stil: düster, magisch, **Slime-Teal + Arcane-Purple**.
Farb-Rampen und Formen sind im Generator dokumentiert und leicht anpassbar.

## Item-Icons (16×16) — `kubejs/assets/kubejs/textures/item/`

| Item | Design-Vorgabe / Design spec |
|------|------------------------------|
| **dark_aether** | Facettierter, schwebender Oktaeder-Kristall. Tiefes Teal an den Kanten → leuchtend cyan zur Mitte, **emissiver weiß-cyaner Kern**, feine Energie-Funken oben/unten, starke Cyan-Aura (2 Ringe). |
| **dark_slime** | Glänzende Schleim-Kugel mit dunklem Kern, großer Glanzpunkt oben-links, kleiner Zweit-Glanz, **Tropfen** an der Unterkante, weiche Teal-Aura. |
| **i_am_atomic_catalyst** | Elektrisch-blaue Energiekugel, **weiß-heißer Kern**, 8 radiale Spark-Strahlen, kräftige blaue Aura. |
| **cult_insignia** | Dunkles Magenta-Medaillon (Metallring + dunkle Innenscheibe), **nach unten zeigendes glühendes Rune-Dreieck**, Spikes an N/E/S/W, violette Aura. |
| **mitsugoshi_ledger** | Goldenes Handelsbuch mit dunklem Buchrücken links, cremefarbene Seitenkante rechts, diagonales Zierband, **teal Edelstein-Verschluss**, goldene Aura. |
| **shadow_pledge_note** | Vertikale Pergamentrolle mit gerollten Enden, angedeutete Textzeilen, **teal Wachs-Siegel** (Shadow Garden) in der Mitte. |
| **slime_suit_helmet / _chestplate / _leggings / _boots** | Teal Rüstungs-Silhouetten mit vertikalem Shading (oben hell → unten dunkel), Licht von links, dunkler Outline, Slime-Sheen oben-links, **magische Teal-Rim-Aura**. |

## Worn Armor Layers (64×32) — `.../textures/models/armor/`

`slime_suit_layer_1.png` (Helm/Brust/Stiefel) und `slime_suit_layer_2.png` (Hose):
Teal-Verlauf mit dezentem Panel-Raster und Glanzpunkten. Werden über den
Armor-Tier `slime_suit` automatisch am Spielerkörper gerendert
(siehe `kubejs/startup_scripts/shadow_garden_armor_tiers.js`).

## Anpassen / Customizing

Im Generator (`tools/gen_textures.py`):
- Farb-Rampen: `R_AETHER`, `R_SLIME`, `R_ATOMIC`, `R_CULT`, `R_BOOK`, `R_PAPER`, `R_SUIT`
- Glow-Farben: `GLOW_TEAL`, `GLOW_CYAN`, `GLOW_BLUE`, `GLOW_PURPLE`, `GLOW_GOLD`
- Silhouetten der Rüstung: `SUIT_MASKS`

Nach Änderungen: `python3 tools/gen_textures.py` → PNGs werden überschrieben.
