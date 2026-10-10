import pathlib,json
root=pathlib.Path(__file__).resolve().parents[1]
rows=json.loads((root/'.local/compatibility/inventory.json').read_text())
runtime=json.loads((root/'.local/compat-runtime/versions.json').read_text())
text='''# Kompatibilitätsabgleich zum Stream-Mitschnitt

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
'''
for row in rows:
    slug=row['project']
    url='https://modrinth.com/mod/'+slug
    if 'id' in row:url+='/version/'+row['id']
    text+=f"| [{slug}]({url}) | {row.get('version','—')} | {'Metadaten vorhanden' if 'version' in row else row.get('status',row.get('error','Unklar'))} |\n"
text+='''
## Zusätzliche Einordnungen

- **Cinematic Zoom:** Der Name ist nicht eindeutig. Die zuvor recherchierte [Fabric-Ausgabe auf CurseForge](https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom-fabric) ist ein möglicher Kandidat. Ohne exakte JAR keine gezielte Übernahme seiner Kamera-/Balkenlogik; im ersten Test nicht zusätzlich aktivieren, um doppelte Balken zu vermeiden.
- **„Amalia“:** Nicht eindeutig identifiziert. Die beschriebene Funktion blendet Rüstung/Helme kosmetisch aus. Für einen belastbaren Vergleich wird die tatsächliche JAR oder der Projektlink benötigt.
- **„Admetica“:** Vermutlich Litematica, im Mitschnitt nur für ein separates Singleplayer-Profil vorgesehen. Identität nicht als bestätigt behandelt. Axiom ist ebenfalls für das separate Singleplayer-Profil eingeordnet.
- **Enhanced Block Entities / Better Block Entities:** Unterschiedliche Projekte; die Tabelle enthält die im Mitschnitt zuletzt genannte Variante Better Block Entities.
- **NoFog:** Kein eindeutig festgelegtes eigenständiges Projekt; Gamma Utils und Sodium-Einstellungen wurden ebenfalls erwähnt. Keine Änderungen an Nebel-Einstellungen durch das Add-on.
- **„No-Brainer“** ist eine Bemerkung zu Sodium. **No Chat Reports** wurde ausdrücklich nicht gewünscht.

## Lokal vorbereiteter Test-Modsatz

Er wird nicht mit der Zusatz-JAR ausgeliefert. Pflichtbibliotheken sind eingeschlossen; die Fabric API stammt zusätzlich aus dem Build. Eine in Zoomifys plattformübergreifenden Metadaten genannte Forge-Kotlin-Abhängigkeit wurde nach Prüfung der Loader-Metadaten nicht in den Fabric-Test übernommen.

'''
for v in runtime:text+=f"- {v['name']} — `{v['version_number']}`\n"
(root/'docs/compatibility.md').write_text(text,encoding='utf-8')
