import os, shutil, json

SRC = "kubejs/assets/kubejs/textures"
DST = "companion-mod/src/main/resources/assets/tensura_abyss"

items = ["dark_slime","dark_aether","slime_suit_helmet","slime_suit_chestplate",
         "slime_suit_leggings","slime_suit_boots"]

os.makedirs(f"{DST}/textures/item", exist_ok=True)
os.makedirs(f"{DST}/models/item", exist_ok=True)
os.makedirs(f"{DST}/textures/models/armor", exist_ok=True)

# item textures + models
for it in items:
    src = f"{SRC}/item/{it}.png"
    if os.path.exists(src):
        shutil.copy(src, f"{DST}/textures/item/{it}.png")
    model = {"parent":"minecraft:item/generated","textures":{"layer0":f"tensura_abyss:item/{it}"}}
    if "slime_suit" in it:
        model["parent"] = "minecraft:item/generated"  # Icon; getragene Optik via Armor-Layer
    json.dump(model, open(f"{DST}/models/item/{it}.json","w"), indent=2)

# signature record (reuse pledge/paper icon if present, else book)
sig_src = f"{SRC}/item/shadow_pledge_note.png"
if os.path.exists(sig_src):
    shutil.copy(sig_src, f"{DST}/textures/item/signature_record.png")
    json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"tensura_abyss:item/signature_record"}},
              open(f"{DST}/models/item/signature_record.json","w"), indent=2)
else:
    json.dump({"parent":"minecraft:item/written_book"}, open(f"{DST}/models/item/signature_record.json","w"), indent=2)

# armor worn layers -> tensura_abyss namespace (required by ArmorMaterial.Layer id)
for n in (1,2):
    src = f"{SRC}/models/armor/slime_suit_layer_{n}.png"
    if os.path.exists(src):
        shutil.copy(src, f"{DST}/textures/models/armor/slime_suit_layer_{n}.png")

# lang files
os.makedirs(f"{DST}/lang", exist_ok=True)
en = {
    "itemGroup.tensura_abyss.shadow_garden":"Shadow Garden",
    "item.tensura_abyss.dark_slime":"Refined Dark Slime",
    "item.tensura_abyss.dark_aether":"Dark Aether",
    "item.tensura_abyss.signature_record":"Signature Record",
    "item.tensura_abyss.slime_suit_helmet":"Slime Suit Mask",
    "item.tensura_abyss.slime_suit_chestplate":"Slime Suit Coat",
    "item.tensura_abyss.slime_suit_leggings":"Slime Suit Leggings",
    "item.tensura_abyss.slime_suit_boots":"Slime Suit Boots",
    "tooltip.tensura_abyss.slime_suit.set":"Set Bonus (full set): Speed II + Resistance II",
    "tooltip.tensura_abyss.slime_suit.stealth":"Stealth: Invisibility while worn as a full set",
    "tooltip.tensura_abyss.signature_record.blank":"An unsigned guild record."
}
de = {
    "itemGroup.tensura_abyss.shadow_garden":"Shadow Garden",
    "item.tensura_abyss.dark_slime":"Veredelter Dunkler Schleim",
    "item.tensura_abyss.dark_aether":"Dunkler Aether",
    "item.tensura_abyss.signature_record":"Signatur-Urkunde",
    "item.tensura_abyss.slime_suit_helmet":"Slime-Suit-Maske",
    "item.tensura_abyss.slime_suit_chestplate":"Slime-Suit-Mantel",
    "item.tensura_abyss.slime_suit_leggings":"Slime-Suit-Hose",
    "item.tensura_abyss.slime_suit_boots":"Slime-Suit-Stiefel",
    "tooltip.tensura_abyss.slime_suit.set":"Set-Bonus (volles Set): Speed II + Resistance II",
    "tooltip.tensura_abyss.slime_suit.stealth":"Stealth: Unsichtbarkeit im vollen Set",
    "tooltip.tensura_abyss.signature_record.blank":"Eine unsignierte Gilden-Urkunde."
}
json.dump(en, open(f"{DST}/lang/en_us.json","w"), indent=2, ensure_ascii=False)
json.dump(de, open(f"{DST}/lang/de_de.json","w"), indent=2, ensure_ascii=False)
print("mod assets generated")
