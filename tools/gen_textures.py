#!/usr/bin/env python3
"""
Erzeugt einfache Basis-Texturen (16x16 Item-Icons + 64x32 Armor-Layer) fuer
das Shadow-Garden-System. Reines Python (zlib+struct), keine externen Libs.

Ausfuehren aus dem Repo-Root:  python3 tools/gen_textures.py
Zielpfade entsprechen den KubeJS-Standardpfaden fuer NeoForge 1.21.1.
"""
import os, struct, zlib

# ── Minimaler PNG-Encoder (RGBA, 8-bit) ──
def write_png(path, w, h, pixels):
    def chunk(tag, data):
        return (struct.pack(">I", len(data)) + tag + data +
                struct.pack(">I", zlib.crc32(tag + data) & 0xffffffff))
    raw = bytearray()
    for y in range(h):
        raw.append(0)  # filter type 0
        for x in range(w):
            raw += bytes(pixels[y][x])
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)

CLEAR = (0, 0, 0, 0)

# ── Palette: (base, dark/outline, light/highlight) ──
PAL = {
    "dark_slime":            ((60, 90, 120),  (28, 44, 66),   (110, 150, 180)),
    "dark_aether":           ((42, 200, 220), (18, 104, 122), (160, 255, 255)),
    "i_am_atomic_catalyst":  ((70, 120, 255), (26, 52, 150),  (170, 205, 255)),
    "cult_insignia":         ((120, 40, 95),  (58, 16, 52),   (185, 85, 150)),
    "mitsugoshi_ledger":     ((205, 165, 60), (112, 82, 22),  (248, 224, 145)),
    "shadow_pledge_note":    ((205, 210, 220),(112, 118, 132),(242, 244, 250)),
    "slime_suit":            ((44, 210, 200), (18, 108, 106), (175, 255, 250)),
}

# ── 16x16 Body-Masken (Rechtecke: (r0,r1,c0,c1) inklusive) ──
def rects_to_body(rects, size=16):
    body = [[False] * size for _ in range(size)]
    for (r0, r1, c0, c1) in rects:
        for r in range(r0, r1 + 1):
            for c in range(c0, c1 + 1):
                body[r][c] = True
    return body

def blob_body(size=16, cx=7.5, cy=7.5, rad=5.4):
    body = [[False] * size for _ in range(size)]
    for r in range(size):
        for c in range(size):
            if (c - cx) ** 2 + (r - cy) ** 2 <= rad ** 2:
                body[r][c] = True
    return body

def render_icon(body, base, dark, light, size=16):
    px = [[list(CLEAR) for _ in range(size)] for _ in range(size)]
    for r in range(size):
        for c in range(size):
            if not body[r][c]:
                continue
            # Outline = Body-Pixel mit transparentem Nachbarn
            edge = False
            for dr, dc in ((1,0),(-1,0),(0,1),(0,-1)):
                nr, nc = r + dr, c + dc
                if nr < 0 or nr >= size or nc < 0 or nc >= size or not body[nr][nc]:
                    edge = True; break
            if edge:
                col = dark
            elif (r + c) % 7 == 0:      # sparsame Highlights
                col = light
            elif r <= size // 2 and c <= size // 2:
                col = light
            else:
                col = base
            px[r][c] = [col[0], col[1], col[2], 255]
    return px

MASKS = {
    "helmet":     [(2,8,4,11),(9,10,4,5),(9,10,10,11)],
    "chestplate": [(3,3,3,12),(4,12,4,11),(4,7,3,3),(4,7,12,12)],
    "leggings":   [(2,2,4,11),(3,12,4,6),(3,12,9,11)],
    "boots":      [(7,12,4,6),(7,12,9,11),(11,12,3,3),(11,12,8,8)],
}

def main():
    root = "kubejs/assets/kubejs/textures"

    # ── Resource-Icons (Blob) ──
    for name in ("dark_slime","dark_aether","i_am_atomic_catalyst",
                 "cult_insignia","mitsugoshi_ledger","shadow_pledge_note"):
        base, dark, light = PAL[name]
        body = blob_body()
        write_png(f"{root}/item/{name}.png", 16, 16,
                  render_icon(body, base, dark, light))

    # ── Slime-Suit Item-Icons (Silhouetten) ──
    base, dark, light = PAL["slime_suit"]
    for piece, rects in MASKS.items():
        body = rects_to_body(rects)
        write_png(f"{root}/item/slime_suit_{piece}.png", 16, 16,
                  render_icon(body, base, dark, light))

    # ── Armor-Layer (worn) fuer 1.21.1: 64x32, einfache Fuellung ──
    # layer_1 = Helm/Brust/Stiefel, layer_2 = Hose. Standardpfad 1.21.1.
    def layer(path, w, h):
        px = [[list(base) + [255] for _ in range(w)] for _ in range(h)]
        for r in range(h):
            for c in range(w):
                if r % 8 == 0 or c % 8 == 0:          # dezentes Panel-Raster
                    px[r][c] = [dark[0], dark[1], dark[2], 255]
                elif (r + c) % 11 == 0:
                    px[r][c] = [light[0], light[1], light[2], 255]
        write_png(path, w, h, px)
    layer(f"{root}/models/armor/slime_suit_layer_1.png", 64, 32)
    layer(f"{root}/models/armor/slime_suit_layer_2.png", 64, 32)

    print("Texturen erzeugt unter", root)

if __name__ == "__main__":
    main()
