#!/usr/bin/env python3
"""
Shadow-Garden Textur-Generator v2 — "hand-drawn" mystische Item-Icons,
Armor-Layer und das Resourcepack-Icon. Reines Python (zlib+struct).

Aus dem Repo-Root:  python3 tools/gen_textures.py

Stil: duester, magisch, Slime-Teal + Arcane-Purple (passend zum Modpack).
Jedes Icon: mehrstufiges Shading, emissiver Kern, Rim-Glow/Aura.
"""
import os, struct, zlib, math

# ─────────────────────────────────────────────────────────────
# PNG-Encoder (RGBA, 8-bit)
# ─────────────────────────────────────────────────────────────
def write_png(path, img):
    h = len(img); w = len(img[0])
    def chunk(tag, data):
        return (struct.pack(">I", len(data)) + tag + data +
                struct.pack(">I", zlib.crc32(tag + data) & 0xffffffff))
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            p = img[y][x]
            raw += bytes((int(p[0]), int(p[1]), int(p[2]), int(p[3])))
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)

def img_new(w, h=None):
    h = h or w
    return [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]

def blend(dst, src):
    sa = src[3] / 255.0
    if sa <= 0: return dst
    da = dst[3] / 255.0
    oa = sa + da * (1 - sa)
    if oa <= 0: return [0, 0, 0, 0]
    return [(src[i] * sa + dst[i] * da * (1 - sa)) / oa for i in range(3)] + [oa * 255]

def setp(img, x, y, rgba):
    if 0 <= y < len(img) and 0 <= x < len(img[0]):
        img[y][x] = blend(img[y][x], rgba)

def clamp(v, lo, hi): return lo if v < lo else hi if v > hi else v

def shade(ramp, t):
    t = clamp(t, 0.0, 1.0)
    i = int(round(t * (len(ramp) - 1)))
    return list(ramp[i]) + [255]

def aura(img, color, layers=((150, 2), (70, 1))):
    """Weicher Rim-Glow: dilatiert die Silhouette nach aussen mit fallendem Alpha."""
    h = len(img); w = len(img[0])
    solid = [[img[y][x][3] > 30 for x in range(w)] for y in range(h)]
    for alpha, _ in layers:
        add = []
        for y in range(h):
            for x in range(w):
                if solid[y][x]: continue
                near = False
                for dy in (-1, 0, 1):
                    for dx in (-1, 0, 1):
                        ny, nx = y + dy, x + dx
                        if 0 <= ny < h and 0 <= nx < w and solid[ny][nx]:
                            near = True; break
                    if near: break
                if near: add.append((x, y))
        for (x, y) in add:
            setp(img, x, y, [color[0], color[1], color[2], alpha])
        for (x, y) in add:
            solid[y][x] = True

# ─────────────────────────────────────────────────────────────
# Farb-Rampen (dunkel -> hell)
# ─────────────────────────────────────────────────────────────
R_SLIME  = [(10,30,32),(16,58,60),(26,96,98),(42,146,142),(74,196,188),(155,238,228)]
R_AETHER = [(8,20,30),(14,72,94),(24,122,152),(52,192,216),(122,242,250),(212,255,255)]
R_ATOMIC = [(10,20,52),(26,48,124),(44,96,212),(96,156,255),(174,214,255),(232,246,255)]
R_CULT   = [(20,8,22),(52,16,46),(92,30,76),(142,52,112),(188,92,152),(228,152,202)]
R_BOOK   = [(58,38,12),(108,76,22),(168,118,40),(210,164,60),(240,208,118),(252,236,178)]
R_PAPER  = [(120,114,94),(168,162,142),(206,200,182),(228,224,208),(242,240,228),(252,250,242)]
R_SUIT   = [(12,64,62),(18,104,102),(30,150,146),(44,204,196),(120,238,230),(190,255,250)]

GLOW_TEAL   = (120, 240, 255)
GLOW_CYAN   = (90, 220, 240)
GLOW_BLUE   = (110, 170, 255)
GLOW_PURPLE = (170, 110, 240)
GLOW_GOLD   = (245, 210, 130)

# ─────────────────────────────────────────────────────────────
# Item-Zeichner (16x16)
# ─────────────────────────────────────────────────────────────
def sphere(img, cx, cy, r, ramp, light=(-0.55, -0.6), core=None, gloss=True):
    for y in range(len(img)):
        for x in range(len(img[0])):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(dx, dy)
            if d > r: continue
            nx, ny = dx / r, dy / r
            nz = math.sqrt(max(0.0, 1 - nx*nx - ny*ny))
            lam = clamp(nx*light[0] + ny*light[1] + nz*0.62, 0, 1)
            t = 0.2 + 0.82 * lam
            if d > r - 1.05: t -= 0.35            # dunkler Rand
            setp(img, x, y, shade(ramp, t))
    if core:
        setp(img, int(cx), int(cy), list(core) + [180])
    if gloss:
        gx, gy = cx + light[0]*r*0.5, cy + light[1]*r*0.5
        setp(img, int(gx), int(gy), [255,255,255,210])
        setp(img, int(gx)+1, int(gy), [255,255,255,120])
        setp(img, int(gx), int(gy)+1, [255,255,255,120])

def draw_dark_slime(size=16):
    img = img_new(size)
    sphere(img, 8, 9, 6.2, R_SLIME, core=(14,44,46))
    # Schleim-Tropfen unten
    for (x,y,a) in [(8,15,255),(7,15,150),(9,15,150),(8,14,255)]:
        setp(img, x, y, shade(R_SLIME, 0.32))
    # zweiter kleiner Glanzpunkt
    setp(img, 10, 7, [220,255,250,150])
    aura(img, GLOW_TEAL, layers=((110,1),(45,1)))
    return img

def draw_dark_aether(size=16):
    img = img_new(size)
    cx, cy, rx, ry = 8, 8, 5.4, 6.6
    for y in range(size):
        for x in range(size):
            dx, dy = (x+0.5-cx)/rx, (y+0.5-cy)/ry
            m = abs(dx) + abs(dy)                 # Diamant/Oktaeder
            if m > 1: continue
            top = dy < 0
            left = dx < 0
            t = 0.30 + (0.5 if top else 0.18) + (0.12 if left else 0.0)
            t -= 0.30 * m                          # zu den Kanten dunkler
            if m > 0.86: t -= 0.25                  # Kanten-Outline
            # Facetten-Mittellinie
            if abs(dx) < 0.09: t += 0.22
            if abs(dy) < 0.08 and not top: t += 0.10
            setp(img, x, y, shade(R_AETHER, t))
    # emissiver Kern
    for (x,y,a) in [(8,8,230),(7,8,150),(8,7,150),(8,9,120),(9,8,120)]:
        setp(img, x, y, [210,255,255,a])
    # feine Energie-Funken
    setp(img, 8, 1, [180,250,255,180]); setp(img, 8, 14, [150,240,255,150])
    aura(img, GLOW_CYAN, layers=((150,2),(70,1)))
    return img

def draw_atomic(size=16):
    img = img_new(size)
    sphere(img, 8, 8, 5.2, R_ATOMIC, core=(230,245,255))
    # weiss-heisser Kern
    for (x,y,a) in [(8,8,255),(7,8,160),(8,7,160),(9,8,140),(8,9,140)]:
        setp(img, x, y, [235,248,255,a])
    # Spark-Strahlen (8 Richtungen)
    rays = [(0,-7),(0,7),(-7,0),(7,0),(-5,-5),(5,-5),(-5,5),(5,5)]
    for (rx,ry) in rays:
        for s in (0.7, 1.0):
            x = int(8 + rx*s*0.5); y = int(8 + ry*s*0.5)
            setp(img, x, y, [190,225,255,200])
        setp(img, int(8+rx*0.55), int(8+ry*0.55), [230,245,255,230])
    aura(img, GLOW_BLUE, layers=((160,2),(80,1)))
    return img

def draw_cult(size=16):
    img = img_new(size)
    cx, cy = 8, 8.2
    for y in range(size):
        for x in range(size):
            d = math.hypot(x+0.5-cx, y+0.5-cy)
            if d > 6.3: continue
            if d > 5.0:                               # Aussenring (Metall)
                t = 0.42 + 0.10*math.sin((x+y)*1.2)
                if d > 6.0: t -= 0.28
                setp(img, x, y, shade(R_CULT, t))
            else:                                     # innere dunkle Scheibe
                setp(img, x, y, shade(R_CULT, 0.14))
    # nach unten zeigendes Rune-Dreieck (glow)
    tri = [(8,4),(6,5),(10,5),(5,6),(11,6),(6,7),(10,7),(7,8),(9,8),(8,9),(8,10)]
    for (x,y) in tri:
        setp(img, x, y, [225,120,190,235])
    # Spikes N/E/S/W
    for (x,y) in [(8,1),(8,15),(1,8),(15,8)]:
        setp(img, x, y, shade(R_CULT, 0.5))
    aura(img, GLOW_PURPLE, layers=((120,1),(55,1)))
    return img

def draw_ledger(size=16):
    img = img_new(size)
    # Buchkoerper
    for y in range(2, 15):
        for x in range(3, 14):
            edge = x in (3,13) or y in (2,14)
            t = 0.30 + 0.35*(1 - (y-2)/12.0)          # oben heller
            if x < 5: t -= 0.15                        # Buchruecken (links)
            setp(img, x, y, shade(R_BOOK, 0.2 if edge else t))
    # Seiten (rechte Kante, creme)
    for y in range(3, 14):
        setp(img, 13, y, [238,228,196,255])
        setp(img, 12, y, [250,242,214,120])
    # diagonale Zierband
    for i in range(11):
        setp(img, 3+i, 13-i, [120,80,20,200])
    # Teal-Edelstein-Verschluss
    setp(img, 8, 8, [60,220,210,255]); setp(img, 8, 7, [150,245,238,220])
    setp(img, 7, 8, [30,160,155,220]); setp(img, 9, 8, [30,160,155,220])
    setp(img, 8, 9, [20,120,116,220])
    aura(img, GLOW_GOLD, layers=((90,1),(40,1)))
    return img

def draw_pledge(size=16):
    img = img_new(size)
    # Pergament-Koerper
    for y in range(2, 14):
        for x in range(4, 12):
            edge = x in (4,11)
            t = 0.55 + 0.25*(1-(y-2)/11.0)
            setp(img, x, y, shade(R_PAPER, 0.28 if edge else t))
    # Rollen oben/unten
    for x in range(3, 13):
        setp(img, x, 2, shade(R_PAPER, 0.35))
        setp(img, x, 1, shade(R_PAPER, 0.5))
        setp(img, x, 13, shade(R_PAPER, 0.35))
        setp(img, x, 14, shade(R_PAPER, 0.5))
    # angedeutete Textzeilen
    for y in (5, 7, 9):
        for x in range(5, 11):
            setp(img, x, y, [120,116,98,150])
    # Wachs-Siegel (teal, Shadow Garden)
    for (x,y,a) in [(8,11,255),(7,11,220),(9,11,220),(8,10,220),(8,12,220)]:
        setp(img, x, y, [40,200,190,a])
    setp(img, 8, 11, [150,245,236,255])
    aura(img, GLOW_TEAL, layers=((80,1),(35,1)))
    return img

# ── Slime-Suit Silhouetten (16x16) mit Shading + Rim-Glow ──
SUIT_MASKS = {
    "helmet":     [(2,8,4,11),(9,10,4,5),(9,10,10,11)],
    "chestplate": [(3,3,3,12),(4,12,4,11),(4,7,3,3),(4,7,12,12)],
    "leggings":   [(2,2,4,11),(3,12,4,6),(3,12,9,11)],
    "boots":      [(7,12,4,6),(7,12,9,11),(11,12,3,3),(11,12,8,8)],
}
def rects_body(rects, size=16):
    b = [[False]*size for _ in range(size)]
    for (r0,r1,c0,c1) in rects:
        for r in range(r0,r1+1):
            for c in range(c0,c1+1):
                if 0<=r<size and 0<=c<size: b[r][c]=True
    return b

def draw_suit(rects, size=16):
    img = img_new(size)
    body = rects_body(rects, size)
    # Bounding-Box fuer vertikales Shading
    ys = [r for r in range(size) if any(body[r])]
    y0, y1 = (min(ys), max(ys)) if ys else (0, size-1)
    for r in range(size):
        for c in range(size):
            if not body[r][c]: continue
            edge = False
            for dr,dc in ((1,0),(-1,0),(0,1),(0,-1)):
                nr,nc = r+dr, c+dc
                if nr<0 or nr>=size or nc<0 or nc>=size or not body[nr][nc]:
                    edge=True; break
            if edge:
                setp(img, c, r, shade(R_SUIT, 0.06)); continue
            t = 0.72 - 0.42*((r-y0)/max(1,(y1-y0)))   # oben hell, unten dunkel
            if c <= size//2: t += 0.12                  # Licht von links
            setp(img, c, r, shade(R_SUIT, t))
    # Slime-Sheen (2 Glanzpunkte oben-links)
    for r in range(size):
        for c in range(size):
            if body[r][c] and r<=y0+2 and c<=size//2:
                setp(img, c, r, [200,255,250,90]); break
    aura(img, GLOW_TEAL, layers=((120,1),(50,1)))
    return img

# ─────────────────────────────────────────────────────────────
# Block: Abyss Portal Frame (16x16) — dunkler Obsidian + teal Rune
# ─────────────────────────────────────────────────────────────
def draw_portal_frame(size=16):
    img = img_new(size)
    R_OBS = [(8,6,16),(16,12,28),(26,20,44),(38,30,64),(54,44,88)]
    for y in range(size):
        for x in range(size):
            # obsidianartiger Untergrund mit leichtem Rauschen
            n = ((x*7 + y*13) % 5) / 4.0
            t = 0.18 + 0.5*n
            if x in (0,15) or y in (0,15): t = 0.05    # Fassung/Rahmen
            img[y][x] = shade(R_OBS, t)
    # eingelassene teal Rune (Raute + Kern)
    rune = [(8,3),(7,4),(9,4),(6,5),(10,5),(5,6),(11,6),(6,7),(10,7),
            (7,8),(9,8),(8,9),(8,10),(7,11),(9,11),(8,12)]
    for (x,y) in rune:
        setp(img, x, y, [46,214,204,235])
    for (x,y) in [(8,7),(8,8),(7,7),(9,7)]:
        setp(img, x, y, [150,245,238,255])
    # Eck-Glimmer
    for (x,y) in [(2,2),(13,2),(2,13),(13,13)]:
        setp(img, x, y, [60,200,200,180])
    return img

# ─────────────────────────────────────────────────────────────
# Armor-Layer (worn) 64x32 — passend zur Slime-Optik, nicht flach
# ─────────────────────────────────────────────────────────────
def armor_layer(w=64, h=32):
    img = img_new(w, h)
    for y in range(h):
        for x in range(w):
            # sanfter vertikaler Verlauf + Panel-Raster
            t = 0.30 + 0.35*(1 - y/(h-1))
            if x % 8 == 0 or y % 8 == 0: t -= 0.22        # Panelkanten
            if (x+y) % 16 == 0: t += 0.25                  # Glanzpunkte
            img[y][x] = shade(R_SUIT, t)
    return img

# ─────────────────────────────────────────────────────────────
# Resourcepack-Icon 128x128 — atmosphaerisch (Abyss + Shadow Garden)
# ─────────────────────────────────────────────────────────────
def pack_icon(size=128):
    import random
    random.seed(7)
    img = img_new(size)
    cx, cy = size/2, size*0.52
    for y in range(size):
        for x in range(size):
            # Void-Gradient (oben dunkler) + radialer Teal-Schimmer
            vy = y/(size-1)
            base = (6+int(6*vy), 10+int(10*vy), 16+int(14*vy))
            d = math.hypot(x-cx, y-cy) / (size*0.7)
            glow = clamp(1 - d, 0, 1) ** 2
            r = base[0] + glow*38
            g = base[1] + glow*120
            b = base[2] + glow*140
            img[y][x] = [clamp(r,0,255), clamp(g,0,255), clamp(b,0,255), 255]

    # Shadow-Garden Runen-Ring (arcane purple), dezent hinter dem Motiv
    ring_r = size*0.34
    for a in range(0, 360, 6):
        rad = math.radians(a)
        x = int(cx + math.cos(rad)*ring_r)
        y = int(cy + math.sin(rad)*ring_r)
        seg = (a // 6) % 3 != 0
        col = [150,90,220,150] if seg else [90,50,150,90]
        setp(img, x, y, col)
        if a % 30 == 0:                                  # Rune-Knoten
            for (dx,dy) in [(0,0),(1,0),(0,1),(-1,0),(0,-1)]:
                setp(img, x+dx, y+dy, [180,120,245,190])

    # Zentrales Slime/Magatama-Motiv (Rimuru-Teal), glossy Sphere
    r = size*0.24
    for y in range(size):
        for x in range(size):
            dx, dy = x+0.5-cx, y+0.5-cy
            dd = math.hypot(dx, dy)
            if dd > r: continue
            nx, ny = dx/r, dy/r
            nz = math.sqrt(max(0.0, 1-nx*nx-ny*ny))
            lam = clamp(nx*(-0.5)+ny*(-0.6)+nz*0.62, 0, 1)
            t = 0.18 + 0.85*lam
            if dd > r-2: t -= 0.30
            col = shade(R_SUIT, t)
            img[y][x] = blend(img[y][x], col)
    # Slime-Auge (dunkel) + Glanzlichter
    setp(img, int(cx-r*0.28), int(cy-r*0.05), [8,26,30,255])
    for (ox,oy,a,s) in [(-0.42,-0.42,235,3),(0.15,-0.5,150,2)]:
        gx, gy = int(cx+r*ox), int(cy+r*oy)
        for dx in range(-s,s+1):
            for dy in range(-s,s+1):
                if dx*dx+dy*dy<=s*s:
                    setp(img, gx+dx, gy+dy, [235,255,252,int(a*0.7)])

    # Rim-Glow um das Slime-Motiv
    for a in range(0, 360, 3):
        rad = math.radians(a)
        for rr in (r+1, r+2, r+3):
            x = int(cx + math.cos(rad)*rr); y = int(cy + math.sin(rad)*rr)
            al = 130 if rr==r+1 else (70 if rr==r+2 else 35)
            setp(img, x, y, [120,240,255,al])

    # schwebende Partikel (cyan/purple)
    for _ in range(70):
        x = random.randint(4, size-5); y = random.randint(4, size-5)
        if math.hypot(x-cx, y-cy) < r+2: continue
        cyan = random.random() < 0.6
        col = [130,240,255, random.randint(60,170)] if cyan else [175,120,245, random.randint(50,140)]
        setp(img, x, y, col)
        if random.random() < 0.3:
            setp(img, x+1, y, [col[0],col[1],col[2],col[3]//2])

    # Vignette
    for y in range(size):
        for x in range(size):
            d = math.hypot(x-size/2, y-size/2)/(size*0.72)
            if d > 0.7:
                a = clamp((d-0.7)*3.0, 0, 0.8)
                img[y][x] = blend(img[y][x], [0,0,0,int(a*255)])
    return img

# ─────────────────────────────────────────────────────────────
def main():
    root = "kubejs/assets/kubejs/textures"
    icons = {
        "dark_slime": draw_dark_slime(),
        "dark_aether": draw_dark_aether(),
        "i_am_atomic_catalyst": draw_atomic(),
        "cult_insignia": draw_cult(),
        "mitsugoshi_ledger": draw_ledger(),
        "shadow_pledge_note": draw_pledge(),
    }
    for name, im in icons.items():
        write_png(f"{root}/item/{name}.png", im)
    for piece, rects in SUIT_MASKS.items():
        write_png(f"{root}/item/slime_suit_{piece}.png", draw_suit(rects))

    write_png(f"{root}/models/armor/slime_suit_layer_1.png", armor_layer())
    write_png(f"{root}/models/armor/slime_suit_layer_2.png", armor_layer())

    # Block-Textur: Abyss Portal Frame
    write_png(f"{root}/block/abyss_portal_frame.png", draw_portal_frame())

    # Resourcepack-Icon
    write_png("resourcepacks/TensuraAbyss_ShadowGarden/pack.png", pack_icon())

    print("Alle Texturen + Pack-Icon erzeugt.")

if __name__ == "__main__":
    main()
