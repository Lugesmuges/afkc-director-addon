# Kompatibilitätsabgleich zum Stream-Mitschnitt

Stand: 10.10.2026. Der Mitschnitt ist eine Kandidatenliste, keine bestätigte installierte Modpack-Konfiguration. Die folgende Tabelle prüft, ob die offiziellen Modrinth-Metadaten eine Fabric-Version für **Minecraft 26.3** anbieten. Das allein bestätigt weder gemeinsames Laden noch visuelle Fehlerfreiheit. Ausgeführte Laufzeittests stehen in [verification.md](verification.md).

## Besonders relevante Überschneidungen

- **Sodium / Iris:** Der vorbereitete gemeinsame Test verwendet Sodium **0.9.2** und Iris **1.11.7**. Iris nennt diese Sodium-Version als konkrete Abhängigkeit; nicht einfach die neueste Sodium-Alpha dazumischen.
- **Freecam / Zoomify / Flashback:** Öffentliche Statusmethoden in den 26.3-JARs geprüft. Das Add-on pausiert die AFK-Kamera bei aktiver Freecam, Zoomify-Zoom sowie Flashback-Wiedergabe oder Export. Flashback-Aufnahme allein wird nicht blockiert. Eine veränderte, nicht lesbare API führt zu einer Logmeldung und konservativem Aussetzen der Cinematics.
- **Do a Barrel Roll:** Während Elytra-Flug startet keine AFK-Kamera. Keine Eingriffe in dessen Roll-Berechnung.
- **Entity Culling / Better Block Entities / ETF / 3D Skin Layers:** Keine Render-Mixins dieser Mods überschrieben. Sichtbares Nachladen, verschwindende Entities und Shader-Effekte müssen im konkreten Pack geprüft werden.
- **Bobby / Distant Horizons / Voxy / Nvidium:** Werden im Mitschnitt als Alternativen diskutiert. Kamerawege benutzen nur die aktuelle Client-Geometrie. LODs oder gespeicherte Chunks erweitern den Kameraradius nicht. [Nvidium](https://modrinth.com/mod/nvidium) deaktiviert seinen Renderer bei aktiven Iris-Shadern. Voxy bot bei diesem API-Abgleich keine Fabric-26.3-Version.
- **Sound Physics / Simple Voice Chat / Cool Rain / Extreme Sound Muffler:** Das Add-on erzeugt keine eigenen Sounds und unterdrückt die AFKC-Musik. Andere Sound- oder Voice-Chat-Einstellungen werden nicht verändert; die Wirkung der entfernten Kameraposition auf vorhandene Umgebungssounds muss im Stream-Pack beurteilt werden.

## Versionsinventar

Die Versionen in dieser Tabelle sind Suchergebnisse, keine pauschalen Installationsempfehlungen. Wo die neuesten Dateien Alphas sind, ist das am Versionsnamen sichtbar.

| Projekt | Gefundene Fabric-26.3-Version | Prüfung |
|---|---|---|
| [sodium](https://modrinth.com/mod/sodium/version/v4PSXean) | mc26.3-0.9.3-alpha.1-fabric | Metadaten vorhanden |
| [iris](https://modrinth.com/mod/iris/version/vTN4NRGW) | 1.11.7+26.3-fabric | Metadaten vorhanden |
| [entityculling](https://modrinth.com/mod/entityculling/version/X1g3VhTx) | 1.11.3 | Metadaten vorhanden |
| [modmenu](https://modrinth.com/mod/modmenu/version/kyy7dbrZ) | 21.0.0 | Metadaten vorhanden |
| [sodium-extra](https://modrinth.com/mod/sodium-extra/version/te2y9qZn) | mc26.3-0.9.4+fabric | Metadaten vorhanden |
| [entitytexturefeatures](https://modrinth.com/mod/entitytexturefeatures/version/O8J3sxxp) | 7.2.5-fabric-26.3 | Metadaten vorhanden |
| [3dskinlayers](https://modrinth.com/mod/3dskinlayers/version/G8BCRxv5) | 1.11.3 | Metadaten vorhanden |
| [zoomify](https://modrinth.com/mod/zoomify/version/RUBpNu00) | 2.17.0+mc26.3 | Metadaten vorhanden |
| [mouse-tweaks](https://modrinth.com/mod/mouse-tweaks/version/LeMuMhLv) | 26.3-2.31-fabric | Metadaten vorhanden |
| [sound-physics-remastered](https://modrinth.com/mod/sound-physics-remastered/version/9loJgQlO) | fabric-1.5.1+26.3 | Metadaten vorhanden |
| [shulkerboxtooltip](https://modrinth.com/mod/shulkerboxtooltip/version/Cj9VEeGt) | 5.4.2+26.3-fabric | Metadaten vorhanden |
| [distanthorizons](https://modrinth.com/mod/distanthorizons/version/TGgEbP9A) | 3.3.4-26.3 | Metadaten vorhanden |
| [voxy](https://modrinth.com/mod/voxy) | — | no Fabric 26.3 release in API |
| [nvidium](https://modrinth.com/mod/nvidium/version/8mVK1zbk) | 0.4.4-beta7-26.3 | Metadaten vorhanden |
| [bobby](https://modrinth.com/mod/bobby/version/J7YnfCss) | 5.2.16+mc26.3 | Metadaten vorhanden |
| [clientsort](https://modrinth.com/mod/clientsort/version/UWMryUad) | 3.104.1+26.3 | Metadaten vorhanden |
| [coolrain](https://modrinth.com/mod/coolrain/version/K9R06E1l) | 1.4.0-26.3 | Metadaten vorhanden |
| [particle-rain](https://modrinth.com/mod/particle-rain/version/ecVAaY1R) | v4.0.2+26.3-fabric | Metadaten vorhanden |
| [herenthere](https://modrinth.com/mod/herenthere/version/NSrBS40o) | 3.104.0+26.3 | Metadaten vorhanden |
| [simple-voice-chat](https://modrinth.com/mod/simple-voice-chat/version/OLnMVWXy) | fabric-2.6.24+26.3 | Metadaten vorhanden |
| [do-a-barrel-roll](https://modrinth.com/mod/do-a-barrel-roll/version/fmAJrjVS) | 3.8.5+26.3-fabric | Metadaten vorhanden |
| [continuity](https://modrinth.com/mod/continuity/version/Jt2sCWTQ) | 3.0.1+26.3 | Metadaten vorhanden |
| [freecam](https://modrinth.com/mod/freecam/version/fZPu4tiS) | 1.5.0-alpha.2 | Metadaten vorhanden |
| [flashback](https://modrinth.com/mod/flashback/version/cV1pa0YV) | 0.43.6 | Metadaten vorhanden |
| [scribble](https://modrinth.com/mod/scribble/version/aXAUZoEb) | 2.1.0+mc26.3 | Metadaten vorhanden |
| [bigsignwriter](https://modrinth.com/mod/bigsignwriter/version/ics7z6RR) | 3.1.1+26.3 | Metadaten vorhanden |
| [better-block-entities](https://modrinth.com/mod/better-block-entities/version/9VvhfLcA) | 1.3.9+mc26.3 | Metadaten vorhanden |
| [gamma-utils](https://modrinth.com/mod/gamma-utils/version/b4sCL1Qe) | 3.2.0+Fabric | Metadaten vorhanden |
| [extreme_sound_muffler](https://modrinth.com/mod/extreme_sound_muffler/version/REw9JB5b) | 4.05-26.3 | Metadaten vorhanden |
| [axiom](https://modrinth.com/mod/axiom/version/CrfjL7cY) | 6.1.3 | Metadaten vorhanden |
| [boat-item-view](https://modrinth.com/mod/boat-item-view/version/ZlP1ROUh) | 26.3-0.0.10-fabric | Metadaten vorhanden |
| [talkingheads](https://modrinth.com/mod/talkingheads/version/G8vIUTxk) | 1.1.4+26.3+fabric | Metadaten vorhanden |

## Zusätzliche Einordnungen

- **Cinematic Zoom:** Der Name ist nicht eindeutig. Die zuvor recherchierte [Fabric-Ausgabe auf CurseForge](https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom-fabric) ist ein möglicher Kandidat. Ohne exakte JAR keine gezielte Übernahme seiner Kamera-/Balkenlogik; im ersten Test nicht zusätzlich aktivieren, um doppelte Balken zu vermeiden.
- **„Amalia“:** Nicht eindeutig identifiziert. Die beschriebene Funktion blendet Rüstung/Helme kosmetisch aus. Für einen belastbaren Vergleich wird die tatsächliche JAR oder der Projektlink benötigt.
- **„Admetica“:** Vermutlich Litematica, im Mitschnitt nur für ein separates Singleplayer-Profil vorgesehen. Identität nicht als bestätigt behandelt. Axiom ist ebenfalls für das separate Singleplayer-Profil eingeordnet.
- **Enhanced Block Entities / Better Block Entities:** Unterschiedliche Projekte; die Tabelle enthält die im Mitschnitt zuletzt genannte Variante Better Block Entities.
- **NoFog:** Kein eindeutig festgelegtes eigenständiges Projekt; Gamma Utils und Sodium-Einstellungen wurden ebenfalls erwähnt. Keine Änderungen an Nebel-Einstellungen durch das Add-on.
- **„No-Brainer“** ist eine Bemerkung zu Sodium. **No Chat Reports** wurde ausdrücklich nicht gewünscht.

## Lokal vorbereiteter Test-Modsatz

Er wird nicht mit der Zusatz-JAR ausgeliefert. Pflichtbibliotheken sind eingeschlossen; die Fabric API stammt zusätzlich aus dem Build. Eine in Zoomifys plattformübergreifenden Metadaten genannte Forge-Kotlin-Abhängigkeit wurde nach Prüfung der Loader-Metadaten nicht in den Fabric-Test übernommen.

- Iris 1.11.7 for Fabric 26.3 — `1.11.7+26.3-fabric`
- Sodium 0.9.2 for Fabric 26.3 — `mc26.3-0.9.2-fabric`
- 1.11.3-26.3 - Fabric — `1.11.3`
- v21.0.0 for 26.3 — `21.0.0`
- 3.2.0+26.3 — `3.2.0+26.3`
- 1.5.0-alpha.2 for MC 26.3 (fabric) — `1.5.0-alpha.2`
- Zoomify 2.17.0+mc26.3 — `2.17.0+mc26.3`
- Fabric Language Kotlin 1.14.2+kotlin.2.4.21 — `1.14.2+kotlin.2.4.21`
- 3.9.7 for fabric 26.3 — `3.9.7+26.3-fabric`
- 3.8.5 for Fabric 26.3 — `3.8.5+26.3-fabric`
- 0.15.3 for 26.3 — `0.15.3+26.3`
- Flashback 0.43.6 — `0.43.6`
