# Prüfprotokoll — 0.1.0-alpha.2

Stand: 10.10.2026. Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, Original AFK Cinematics 1.0.6+26.3.

## Ergebnis

Finaler Lauf: `build runClientGameTest -Pcompat`, beendet am **10.10.2026 um 21:16 Uhr Europe/Berlin**. **BUILD SUCCESSFUL**, Exitcode **0**, **13 Unit-Tests ohne Fehler** und vollständiger Client-GameTest bestanden. Log: `.local/final-alpha2-validation.log`.

Der final ausgelieferte Ansatz berät die Original-Presets bei Standpunkt und Blickziel. Eigene Shot-Kurven, verlängerte Dauern, zusätzliche Fade-Effekte und separate Cozy-Modi wurden gemäß der letzten Nutzerpräzisierung entfernt. Originale Bewegung, Tempo, Interpolation, FOV, Balken und Übergänge bleiben erhalten. Ausschließlich die ausdrücklich unerwünschte Cinematic-Musik wird weiter unterdrückt.

## Logiktests

- Bauaktivität wiegt stärker als reine Aufenthalte.
- Ein vier Stunden altes Bauprojekt bleibt ein Motiv; entfernte Orte werden ausgefiltert.
- Ein räumlich zusammenhängendes Haus wird zu einem Motiv mit Ausdehnung zusammengefasst.
- Begrenzter Speicher, negative Koordinaten und explizites Leeren.
- Speichern/Laden nach Neustart, Trennung nach Server und Dimension, dauerhaftes Leeren und Umgang mit beschädigten Dateien.
- Hinweise erhalten Distanz/Höhe des Original-Presets, wechseln Motive/Winkel und ergänzen Kontext, ohne auf eine Ansicht festzulegen.
- Verdeckte/unzugängliche Vorschläge werden abgelehnt.
- Der Original-Fokusgrenzwert wird berücksichtigt, damit akzeptierte Blickziele nicht nach dem Schnitt nach innen driften.

Fehlende Bauprojekt-Erkennung, Persistenz, neue Motivberatung und Fokusgrenze wurden jeweils zuerst durch fehlschlagende Tests nachgewiesen. Nach der Implementierung bestanden sie.

## Tatsächlicher Minecraft-Lauf

Ein eigener Testclient erzeugte eine temporäre Welt und prüfte:

1. Einstellungsoberfläche und reale Blockplatzierung. Hebelbedienung mit einem Block in der Hand zählt nicht als Platzierung.
2. Originaler Start, Eingabeabbruch, normale Shot-Dauer von 100–120 Ticks. Der vorherige Ansatz mit eigenen Dauern wurde durch einen fehlschlagenden Runtime-Test erkannt und entfernt.
3. Tatsächliche Übernahme von Motivhinweisen durch Original-Presets und messbare weitere Kamerabewegung danach.
4. Unterdrückung automatischer sowie ausdrücklich angeforderter AFKC-Musik; eine auf null gesetzte Musiklautstärke bleibt null.
5. Echte Freecam-Aktivierung während der ersten Cinematic-Ticks und unverändertes FOV nach der Übergabe. Zusätzlich fremde Kamera-Entity während der Startphase.
6. Ende einer Aufnahme beim Dimensionswechsel, keine Übernahme von Overworld-Motiven in den Nether, Wiederladen der gemerkten Orte bei Rückkehr. Aktiver Speicher leer nach Disconnect, lokale Datei bleibt verfügbar.
7. Ein vorhandenes Haus wird über seine sichtbaren Oberflächen gemerkt und von beratenen Original-Presets gezeigt. Regen unter einem Dach wird ebenfalls durchgespielt.

Screenshots von Einstellungen, Bauwerk und Regen angesehen. Die UI ist lesbar. Das Haus ist als Motiv sichtbar; die Bilder sind technische Testfixtures, kein fertiger ästhetischer Stream-Showcase. Der originale enge FOV kann Teile eines Hauses abschneiden.

## Gemeinsam geladene Referenz-Mods

Sodium **0.9.2+mc26.3**, Iris **1.11.7+mc26.3**, Entity Culling **1.11.3**, Mod Menu **21.0.0**, Freecam **1.5.0-alpha.2**, Zoomify **2.17.0+mc26.3**, Do a Barrel Roll **3.8.5**, Flashback **0.43.6**, jeweils mit erforderlichen Bibliotheken. Versionen und Hashes: `docs/compatibility-lock.json`.

Iris lief **ohne aktiviertes Shaderpack**. Freecam wurde tatsächlich umgeschaltet; spezielle Zoom-/Replay-/Export-Funktionen wurden nicht vollständig durchgespielt. Die Windows-Performancecounter-, Entwicklungs-Refmap- und sonstigen dokumentierten Fremdmod-/Umgebungswarnungen im Log sind keine fehlerfreie-Log-Zusage. Der Testprozess endete erfolgreich.

## Unabhängige Codeprüfung

Die erste Prüfung korrigierte Platzierungsfehlzählung, Kamera-Vorrang in der Startphase und aktive Aufnahmen über Dimensionswechsel hinweg. Die Folgeprüfung bestätigte die Trennung und Begrenzung des Speichers sowie die Beibehaltung der Original-Bewegung. Ein relevanter Fokus-Clamp-Befund wurde reproduziert und korrigiert: Hinweise müssen innerhalb von 15,5 horizontalen Blöcken liegen, mit Reserve für die native Fokusbewegung. Ein früherer Befund zum eigenen Cozy-Modus entfiel durch dessen Entfernung gemäß Nutzerpräzisierung.

## Grenzen / Praxistest

- Kein Live-Test auf einem fremden Multiplayer-Server, mit dessen Regeln/Anti-Cheat oder mit dem vollständigen Creator-Pack und Shader.
- Keine FPS-/Frametime-Garantie und keine vollständige Prüfung beliebiger modifizierter Blockformen.
- Clientseitige Bestätigung nach zwölf Ticks kann sehr spät vom Server abgelehnte Änderungen zunächst als Aktivität zählen.
- Keine semantische Eigentumserkennung; Baugebiete sind aus beobachteten Aktivitäten abgeleitete Motive. Keine Garantie einer vollständigen Totalansicht großer Anlagen aus jeder Position.
- Original-Aufnahmen ohne angenommenen Motivhinweis behalten ursprüngliches Verhalten und ursprüngliche Kollisionsprüfungen.

Praxistest: an einem echten Haus/Farmbereich bauen oder `/afkdirector remember` benutzen; `/afkc start`; Bildausschnitt, Wiederholung und Bewegung prüfen. Danach an Fenster/Glas, Wänden, Sitzplatz bei Regen und Chunk-Grenzen testen, dann mit aktivem Shader und Stream-Aufnahme. Welt wechseln und zurückkommen; Erinnerung muss wieder verfügbar sein, während falsche Welt-Motive ausgeschlossen bleiben.

## Ausgelieferte Datei

Datei: dist/afkc-director-addon-0.1.0-alpha.2.jar (47873 Bytes). SHA-256: a35022b1a1aaf354d73ec9616c962f827e2498c952f14f4c5a00413b943c17af.

Archiv geprüft: client-only, exakte AFKC-Abhängigkeit, keine Original-Klassen, Testklassen, Fremd-JARs, Audioassets oder entfernten eigenen Animations-/Fade-Klassen. Original-JAR unverändert.
