# Generator für FTB-Quests-Kapitel "Shadow Garden" (SNBT, FTB Quests 1.21 NeoForge)
import os

_counter = 0x5A00000000000000
def nid():
    global _counter
    _counter += 1
    return f"{_counter:016X}"

def esc(s): return s.replace('\\', '\\\\').replace('"', '\\"')

def snbt_desc(lines):
    inner = "\n".join('\t\t\t\t"' + esc(l) + '"' for l in lines)
    return "[\n" + inner + "\n\t\t\t]"

def task_item(item, count=1, nbtless=True):
    return {'type':'item','item':item,'count':count}
def task_check(title):
    return {'type':'checkmark','title':title}
def task_kill(entity, value):
    return {'type':'kill','entity':entity,'value':value}

def reward_item(item, count=1):
    return {'type':'item','item':item,'count':count}
def reward_xp(xp):
    return {'type':'xp','xp':xp}

def emit_task(t):
    tid = nid()
    if t['type']=='item':
        return (f'\t\t\t\t{{ id: "{tid}" type: "item" '
                f'item: {{ id: "{t["item"]}", count: {t["count"]} }} }}')
    if t['type']=='checkmark':
        return f'\t\t\t\t{{ id: "{tid}" type: "checkmark" title: "{esc(t["title"])}" }}'
    if t['type']=='kill':
        return (f'\t\t\t\t{{ id: "{tid}" type: "kill" '
                f'entity: "{t["entity"]}" value: {t["value"]}L }}')

def emit_reward(r):
    rid = nid()
    if r['type']=='item':
        return (f'\t\t\t\t{{ id: "{rid}" type: "item" '
                f'item: {{ id: "{r["item"]}", count: {r["count"]} }} }}')
    if r['type']=='xp':
        return f'\t\t\t\t{{ id: "{rid}" type: "xp" xp: {r["xp"]} }}'

def emit_quest(q):
    lines = []
    lines.append('\t\t{')
    if q.get('deps'):
        deps = ", ".join('"'+d+'"' for d in q['deps'])
        lines.append(f'\t\t\tdependencies: [{deps}]')
    lines.append('\t\t\tdescription: ' + snbt_desc(q['desc']))
    if q.get('shape'):
        lines.append(f'\t\t\tshape: "{q["shape"]}"')
    if q.get('size'):
        lines.append(f'\t\t\tsize: {q["size"]}d')
    lines.append(f'\t\t\ticon: "{q["icon"]}"')
    lines.append(f'\t\t\tid: "{q["id"]}"')
    rew = "\n".join(emit_reward(r) for r in q['rewards'])
    lines.append('\t\t\trewards: [\n' + rew + '\n\t\t\t]')
    tsk = "\n".join(emit_task(t) for t in q['tasks'])
    lines.append('\t\t\ttasks: [\n' + tsk + '\n\t\t\t]')
    lines.append(f'\t\t\ttitle: "{esc(q["title"])}"')
    lines.append(f'\t\t\tx: {q["x"]}d')
    lines.append(f'\t\t\ty: {q["y"]}d')
    lines.append('\t\t}')
    return "\n".join(lines)

# ── Quest-Definitionen ──
Q = {}
def add(key, **kw):
    kw['id'] = nid()
    Q[key] = kw
    return kw['id']

add('possessed', title="Der Besessene / The Possessed",
    x=-6, y=0, icon="minecraft:wither_skeleton_skull", shape="hexagon", size=1.5,
    desc=["§7Du erwachst als §5Besessener§7 in einer alten Ruine.",
          "You awaken as one of the §5Possessed§7 in an ancient ruin.",
          "",
          "Sammle §aSchleim§7, um deinen Koerper zu reinigen.",
          "Gather §aslime§7 to purge your body."],
    tasks=[task_item("minecraft:slime_ball", 8)],
    rewards=[reward_xp(50)])

add('heal', title="Heilung durch Schleim-Magie / Slime Healing",
    x=-4.5, y=0, icon="kubejs:dark_slime",
    desc=["§7Veredle den Schleim zu §5Dunklem Schleim§7 und heile die Besessenheit.",
          "Refine slime into §5Dark Slime§7 to cure the possession."],
    deps=[Q['possessed']['id']],
    tasks=[task_item("kubejs:dark_slime", 2)],
    rewards=[reward_item("kubejs:dark_slime", 2), reward_xp(80)])

add('join', title="Beitritt zum Shadow Garden / Join the Shadow Garden",
    x=-3, y=0, icon="kubejs:shadow_pledge_note", shape="diamond", size=1.25,
    desc=["§7Schwoere dem §3Shadow Garden§7 die Treue.",
          "Pledge your loyalty to the §3Shadow Garden§7.",
          "",
          "§8Loese eine Pledge ein, um deinen ersten Rang [Shadow] zu erhalten.",
          "§8Redeem a pledge to earn your first rank [Shadow]."],
    deps=[Q['heal']['id']],
    tasks=[task_item("kubejs:shadow_pledge_note", 1)],
    rewards=[reward_item("kubejs:shadow_pledge_note", 4), reward_xp(100)])

# ── Klassen-Verzweigungen ──
add('alpha', title="Klasse: Alpha / Class: Alpha",
    x=-1.5, y=-3, icon="minecraft:netherite_sword",
    desc=["§b§lAlpha§r §7- Die staerkste Kriegerin, Anfuehrerin im Kampf.",
          "§b§lAlpha§r §7- The strongest warrior, leader in battle.",
          "",
          "Beweise deine Kampfkraft: reworke ein Netherit-Schwert mit Dunklem Aether.",
          "Prove your might: rework a Netherite Sword with Dark Aether."],
    deps=[Q['join']['id']],
    tasks=[task_item("minecraft:netherite_sword", 1)],
    rewards=[reward_item("minecraft:diamond", 6), reward_xp(150)])

add('beta_eta', title="Klasse: Beta / Eta / Class: Beta / Eta",
    x=-1.5, y=-1, icon="minecraft:writable_book",
    desc=["§b§lBeta§r §7- Die Autorin & Chronistin. §b§lEta§r §7- Die Forscherin.",
          "§b§lBeta§r §7- Author & chronicler. §b§lEta§r §7- The researcher.",
          "",
          "Dokumentiere & erforsche: stelle das Mitsugoshi Trade Ledger her.",
          "Document & research: craft the Mitsugoshi Trade Ledger."],
    deps=[Q['join']['id']],
    tasks=[task_item("kubejs:mitsugoshi_ledger", 1)],
    rewards=[reward_item("minecraft:experience_bottle", 8), reward_xp(150)])

add('gamma', title="Klasse: Gamma / Class: Gamma",
    x=-1.5, y=1, icon="minecraft:gold_ingot",
    desc=["§b§lGamma§r §7- Die Finanz- & Handelsexpertin von Mitsugoshi.",
          "§b§lGamma§r §7- The finance & trade genius of Mitsugoshi.",
          "",
          "Baue Wohlstand auf: beschaffe das MineColonies-Bauwerkzeug.",
          "Build wealth: obtain the MineColonies building tool."],
    deps=[Q['join']['id']],
    tasks=[task_item("minecolonies:buildtool", 1)],
    rewards=[reward_item("minecraft:gold_block", 3), reward_xp(150)])

add('delta', title="Klasse: Delta / Class: Delta",
    x=-1.5, y=3, icon="minecraft:wolf_spawn_egg",
    desc=["§b§lDelta§r §7- Die wilde Bestie, roh und unbaendig.",
          "§b§lDelta§r §7- The wild beast, raw and untamed.",
          "",
          "Jage: erlege 15 feindliche Kreaturen.",
          "Hunt: slay 15 hostile creatures."],
    deps=[Q['join']['id']],
    tasks=[task_kill("minecraft:zombie", 15)],
    rewards=[reward_item("minecraft:cooked_beef", 16), reward_xp(150)])

# ── Mitsugoshi-Imperium (MineColonies) ──
add('town', title="Mitsugoshi-Handelsimperium / Mitsugoshi Empire",
    x=0, y=1, icon="minecolonies:blockhuttownhall",
    desc=["§7Errichte das Herz deiner Tarnung: das §6Mitsugoshi§7-Rathaus.",
          "Establish the heart of your disguise: the §6Mitsugoshi§7 Town Hall.",
          "",
          "§8Platziere & baue das MineColonies Town Hall (Bausystem).",
          "§8Place & build the MineColonies Town Hall."],
    deps=[Q['gamma']['id']],
    tasks=[task_item("minecolonies:blockhuttownhall", 1)],
    rewards=[reward_item("minecraft:emerald", 8), reward_xp(200)])

add('market', title="Marktplatz & Gilde / Marketplace & Guild",
    x=1.5, y=1, icon="minecolonies:blockhutmarketplace",
    desc=["§7Erweitere Mitsugoshi um Marktplatz und Handelsgilde.",
          "Expand Mitsugoshi with a marketplace and trade guild."],
    deps=[Q['town']['id']],
    tasks=[task_item("minecolonies:blockhutmarketplace", 1)],
    rewards=[reward_item("minecraft:emerald", 12), reward_xp(200)])

add('activate', title="Tarnung aktivieren / Activate the Disguise",
    x=3, y=1, icon="kubejs:mitsugoshi_ledger", shape="diamond",
    desc=["§7Aktiviere das Trade Ledger am Kolonie-Zentrum.",
          "Activate the Trade Ledger at your colony core.",
          "",
          "§cWarnung: Der Kult von Diablos wird nun Raids starten!",
          "§cWarning: The Cult of Diablos will now start raids!"],
    deps=[Q['market']['id']],
    tasks=[task_check("Rechtsklick mit dem Mitsugoshi Trade Ledger / Right-click the Ledger")],
    rewards=[reward_item("kubejs:dark_aether", 1), reward_xp(250)])

# ── Diablos-Kult-Verteidigung ──
add('defend', title="Verteidige gegen den Diablos-Kult / Defend vs. the Cult",
    x=4.5, y=1, icon="minecraft:crossbow",
    desc=["§7Der §5Kult von Diablos§7 greift dein Imperium an.",
          "The §5Cult of Diablos§7 raids your empire.",
          "",
          "Schlage die Raids zurueck und sammle §5Kult-Insignien§7.",
          "Repel the raids and collect §5Cult Insignia§7."],
    deps=[Q['activate']['id']],
    tasks=[task_item("kubejs:cult_insignia", 6)],
    rewards=[reward_item("kubejs:cult_insignia", 2), reward_xp(300)])

# ── Endgame: Dark Aether Kette ──
add('aether', title="Dunkler Aether / Dark Aether",
    x=-1.5, y=-5, icon="kubejs:dark_aether", shape="hexagon", size=1.25,
    desc=["§7Die ultimative Endgame-Ressource. Ersetzt alle Diamanten-Upgrades.",
          "The ultimate endgame resource. Replaces all diamond upgrades.",
          "",
          "§86x Dunkler Schleim + 2x Echo-Scherbe + Netherstern.",
          "§86x Dark Slime + 2x Echo Shard + Nether Star."],
    deps=[Q['join']['id']],
    tasks=[task_item("kubejs:dark_aether", 1)],
    rewards=[reward_item("kubejs:dark_aether", 2), reward_xp(300)])

add('atomic', title="\"I Am Atomic\" Ultimate",
    x=0, y=-5, icon="kubejs:i_am_atomic_catalyst", shape="gear", size=1.5,
    desc=["§b§lI... AM... ATOMIC.",
          "",
          "§7Crafte den Katalysator und entfessle die neon-blaue Explosion.",
          "Craft the catalyst and unleash the neon-blue blast.",
          "§8Massiver AoE-Schaden, zerstoert KEINE Bloecke.",
          "§8Massive AoE damage, destroys NO blocks."],
    deps=[Q['aether']['id']],
    tasks=[task_item("kubejs:i_am_atomic_catalyst", 1)],
    rewards=[reward_item("kubejs:dark_aether", 3), reward_xp(500)])

add('breath', title="Breath-System freischalten / Unlock the Breath System",
    x=0, y=-6.5, icon="minecraft:dragon_breath",
    desc=["§7Nutze Dunklen Aether, um das Tensura-§bBreath§7-System zu erwecken.",
          "Use Dark Aether to awaken the Tensura §bBreath§7 system.",
          "",
          "§8Rechtsklick mit Dunklem Aether (siehe shadow_garden.js).",
          "§8Right-click Dark Aether (see shadow_garden.js)."],
    deps=[Q['aether']['id']],
    tasks=[task_check("Breath-Attunement erhoehen / Increase Breath Attunement")],
    rewards=[reward_xp(400)])

# ── Slime Suit ──
add('suit', title="Slime Suit (Stealth-Ruestung) / Slime Suit",
    x=6, y=1, icon="kubejs:slime_suit_chestplate", shape="pentagon", size=1.5,
    desc=["§7Craft das komplette §3Slime Suit§7 aus Netherit, Dunklem Schleim",
          "§7und Kult-Insignien.",
          "Craft the full §3Slime Suit§7 from Netherite, Dark Slime and Cult Insignia.",
          "",
          "§8Set-Bonus: Speed II + Resistance II + Unsichtbarkeit (Stealth).",
          "§8Set bonus: Speed II + Resistance II + Invisibility (stealth)."],
    deps=[Q['defend']['id'], Q['alpha']['id']],
    tasks=[task_item("kubejs:slime_suit_helmet",1), task_item("kubejs:slime_suit_chestplate",1),
           task_item("kubejs:slime_suit_leggings",1), task_item("kubejs:slime_suit_boots",1)],
    rewards=[reward_item("kubejs:dark_aether", 2), reward_xp(400)])

# ── Finaler Rang ──
add('lord', title="Aufstieg zum Shadow Lord / Ascend to Shadow Lord",
    x=7.5, y=-2, icon="minecraft:nether_star", shape="hexagon", size=2.0,
    desc=["§5§lDer Herrscher der Schatten.",
          "§5§lThe ruler of shadows.",
          "",
          "§7Meistere den I-Am-Atomic-Skill, das Slime Suit und erreiche",
          "§7den Rang [Shadow Lord] (50 Pledges).",
          "Master the I-Am-Atomic skill, the Slime Suit, and reach [Shadow Lord] (50 pledges)."],
    deps=[Q['atomic']['id'], Q['suit']['id'], Q['breath']['id']],
    tasks=[task_check("Rang [Shadow Lord] erreichen / Reach rank [Shadow Lord]")],
    rewards=[reward_item("kubejs:dark_aether", 8), reward_item("minecraft:netherite_ingot",4), reward_xp(1000)])

# ── Chapter-Datei zusammensetzen ──
chapter_id = nid()
body = ",\n".join(emit_quest(Q[k]) for k in Q)
out = f'''{{
	default_hide_dependency_lines: false
	default_quest_shape: "circle"
	filename: "shadow_garden"
	group: ""
	icon: "kubejs:dark_aether"
	id: "{chapter_id}"
	order_index: 0
	quest_links: [ ]
	quests: [
{body}
	]
	title: "The Eminence in Shadow"
	images: [ ]
}}
'''

dst = "kubejs_ftbquests/config/ftbquests/quests/chapters/shadow_garden.snbt"
os.makedirs(os.path.dirname(dst), exist_ok=True)
with open(dst, "w", encoding="utf-8") as f:
    f.write(out)
print("Wrote", dst)
print("Quests:", len(Q))
