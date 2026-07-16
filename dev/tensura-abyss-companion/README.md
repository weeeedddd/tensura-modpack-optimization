# Tensura Abyss Companion (Weg B — echte Custom-Rassen)

NeoForge 1.21.1 · Java 21 · Mod-ID `tensura_abyss`

Registriert die Shadow-Garden-Custom-Rassen **nativ** in der Race-Registry von
**Tensura: Reincarnated**, damit sie im originalen Evolutionsmenü auftauchen und
von **Tensura: Ascension** erkannt werden. Das Gating (Boss-Kills, Dimension,
Items, Coins) bleibt in KubeJS (`overrides/kubejs/server_scripts/`).

---

## ⚠️ Mod-ID-Konflikt — zuerst lesen

Die **bestehende** `dev/companion-mod/` benutzt bereits die Mod-ID
`tensura_abyss`. **Zwei Mods können sich dieselbe Mod-ID nicht teilen** —
NeoForge lädt sonst keine von beiden. Du hast zwei Optionen:

1. **Zusammenführen (empfohlen):** die `AbyssRaces`-Registry in die bestehende
   `dev/companion-mod/` übernehmen (ein Mod = eine ID). Dann ist dieses Modul
   nur die Vorlage für die Race-Klasse.
2. **Getrennt lassen:** dann muss eines der beiden Module eine andere Mod-ID
   bekommen (z.B. dieses hier auf `tensura_abyss_races`) — Package, `mod_id` in
   `gradle.properties` und die Lang-/Asset-Ordner entsprechend umbenennen.

Sag mir, welchen Weg du willst — ich setze ihn sauber um.

---

## Die EINE Stelle, die gegen die echte Tensura-API muss

Alles ist fertig verdrahtet **außer** dem eigentlichen Race-Objekt-Aufbau.
Die Unsicherheit ist bewusst auf **eine Methode** isoliert:
`AbyssRaces.abyssRace(AbyssRaceDef)`.

Zu verifizieren (Kommentare `TODO(API-...)` im Code):

| # | Was | Annahme im Code | So findest du das Echte |
|---|-----|-----------------|--------------------------|
| 1 | Race-Import | `net.tensura.api.race.Race` | Jar dekompilieren; wahrscheinlicher `com.github.manasmods.tensura.race.Race` |
| 2 | Registry-Key | `tensura:races` | im dekompilierten Tensura nach `ResourceKey`/`Registry` für Rassen suchen |
| 3 | Builder/Ctor | Pseudocode im JavaDoc | 1.21.1-Beispiel-Addons (siehe unten) zeigen es 1:1 |
| 4 | Lang-Key-Präfix | `race.tensura_abyss.<id>` | prüfen, wie Tensura/Ascension ihre eigenen Rassen benennen |

**Referenz-Addons (1.21.1), die Race-Registrierung vormachen:**
- https://github.com/BanditHelps/TensuraAddonExample
- https://github.com/vel-mc/TensuraAddonExample4554
- https://github.com/AlitigerX/tensurapack

Sobald der Race-Import steht: in `AbyssRaces` das generische `Object` durch
`Race` ersetzen (bei `RACES`, den `Supplier`-Feldern und `abyssRace`).

---

## Dependencies (CurseMaven — exakt deine Pack-Versionen)

In `build.gradle` bereits mit echten, verifizierten fileIDs eingetragen:

| Mod | projectID | fileID | Datei |
|-----|-----------|--------|-------|
| ManasCore | 619025 | 8022425 | (Basis-Lib) |
| Tensura: Reincarnated | 643695 | 7905367 | `tensura-neoforge-2.0.1.1` |
| Nightmare's Tensura Utils | 1525266 | 8203788 | `nightmareutils-0.1.2` |

NeoForge 1.21.1 nutzt offizielle Mappings → die Produktions-Jars sind direkt
kompilierbar (kein `deobf` nötig). Alternativ JitPack (`com.github.manasmods`)
— auskommentiert in `build.gradle`.

---

## Build

```bash
./gradlew build      # -> build/libs/tensura_abyss-1.0.0.jar
```

Die fertige Jar gehört in den Pack-Export nach `overrides/mods/`
(siehe Haupt-README, Abschnitt „Custom companion mod").

## Enthaltene Beispiel-Rassen

shadow_slime · eminence_of_the_abyss · low_shadow_demon · human_apprentice ·
vampire_spawn · stylish_bandit_slayer — die restlichen 38 Stufen nach demselben
`AbyssRaceDef`-Muster ergänzen (Werte konsistent mit
`overrides/config/tensura/ascension-races.toml`).
