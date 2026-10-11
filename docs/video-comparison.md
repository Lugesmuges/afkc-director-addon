# Echte Vergleichsaufnahmen

Die MP4-Dateien im [Alpha-Release](https://github.com/Lugesmuges/afkc-director-addon/releases/tag/v0.1.0-alpha.4) sind echte Aufnahmen des Minecraft-Fensters. Sie zeigen eine eigens erzeugte Demo-Bauwelt, **nicht CraftAttack oder Heikos Bauwerke**. Es sind ausgewählte Beispiele, kein kontrollierter Qualitäts- oder Performance-Benchmark.

## Gleicher Aufbau

- Minecraft 26.3, AFK Cinematics 1.0.6+26.3, derselbe Kompatibilitäts-Modsatz aus `compatibility-lock.json`, kein aktives Shaderpack.
- Dieselbe kompakte Werkstatt auf einer Plattform am Wasser, dieselbe Tageszeit, dasselbe Wetter und dieselbe Spielerposition/Blickrichtung. Testcode: `ShowcaseGameTest.java`.
- Vor dem Start sechs normale Blockplatzierungen im Survival-Modus. Das Add-on lernt diese automatisch. Kein `remember`, keine vorgegebene Kamera-Choreografie, keine erzwungenen Motivkoordinaten.
- Ohne Add-on: Fabric lädt `afkc_director` tatsächlich nicht (`fabric.debug.disableModIds`); die Aufnahmefixture prüft das. Mit Add-on: Die Fixture prüft, dass es geladen wurde.
- Start jeweils über den normalen Clientbefehl `/afkc start`. Zufällige Original-Presets bleiben zufällig; die Folgen sind kein paarweise identischer Shot-Test.

Weltaufbau und Teleports zur Vorbereitung gehören ausschließlich zum isolierten Testserver. Sie stecken nicht in der veröffentlichten JAR und sind keine Voraussetzung für deren Betrieb auf einem Survival-Server.

## Aufnahme

Windows Graphics Capture über FFmpeg `gfxcapture`, ausschließlich das Minecraft-Fenster. 1280 × 720, H.264, 30 Bilder/s im Ausgabeformat, ohne Audiospur. Keine KI-Bilder, keine nachgebauten Kamerafahrten, keine Zeitlupe, keine zusätzliche Bewegungsinterpolation. Die vorhandenen schwarzen Balken stammen aus AFK Cinematics; nur die erklärende Beschriftung wurde nachträglich ergänzt.

Beide kurzen Videos sind jeweils 48 Sekunden lang: Sekunde 12 bis 60 der jeweiligen 72-sekündigen Fensteraufnahme, kurz nach dem manuellen Cinematic-Start. Jeweils ein durchgehender Ausschnitt, keine neu sortierten Shots. Die begrenzte Testhardware kann Aufnahme-Ruckler verursachen; die 30 Bilder/s im Dateiformat sind keine Zusage über die erreichte Spiel-Framerate.

Im ausgewählten Original-Ausschnitt wechseln brauchbare Bauplatzansichten mit längeren Himmelsaufnahmen. Die neue Alpha.4 ergänzt längere, ruhige Fahrten mit festem Blickpunkt. Auch damit sind nahe Spieleransichten möglich; nicht jede Einstellung ist automatisch eine gelungene Bauwerksaufnahme. Die Auswahl dient der Illustration und ersetzt keine Auswertung vieler zufälliger Durchläufe.

## Nachstellen

Mit vorbereiteter Entwicklungsumgebung `gradlew.bat runClientGameTest -Pcompat -Pshowcase=before` beziehungsweise `-Pshowcase=after` starten. Alte Marker der entsprechenden Variante unter `.local/showcase/` vorher entfernen. Die Fixture wartet nach dem Weltaufbau auf die Datei `before.go` bzw. `after.go`, damit die Fensteraufnahme vor dem Cinematic-Start beginnen kann. Danach läuft die Kamera 55 Sekunden. Diese Aufnahmefixture wird nicht mit ausgeliefert.

Das Add-on soll passendere Motive liefern. Die Clips belegen keine zuverlässige Erkennung kompletter Großbauwerke und keine fehlerfreien Aufnahmen in beliebigen Welten. Für einen überzeugenden Stream-Showcase fehlen noch Tests am tatsächlichen Bauwerk mit dem tatsächlich verwendeten Shader und Modpack. Rückmeldungen und Verbesserungen sind willkommen.

## Alpha.5: große Anlage und Statistik-Karten

Zusätzliche Aufnahme: `03-totalen-und-statistiken.mp4`. Echte 1280 × 720-Fensteraufnahme, ohne Ton, mit dem vorbereiteten Kompatibilitäts-Modsatz und ohne Shader. Die separate 48 Blöcke breite synthetische Anlage wird durch `WideScene` aufgebaut. Die Fixture trägt dafür reproduzierbare frühere Bauaktivität in den Test-Speicher ein. Das ist ein Kameratest; es zeigt keine live über Stunden gelernte Ölplattform und keine CraftAttack-Aufnahme. Die Statistik-Karten stammen aus den tatsächlichen Werten des Testspielers Player0.

Mehrere reguläre Schnitte bleiben in einer fortlaufenden Aufnahme. Die Fixture prüft jede Sekunde die tatsächliche Render-Blickrichtung auf reale Szene; die Bildkontrolle ergänzt diese geometrische Prüfung. Die endgültige Video-Schnittstelle ist kein Beleg für beliebige Server, Shader oder uneingeschränkt gute Motive. Das ursprüngliche Alpha.4-Vorher/Nachher-Paar bleibt unter dem älteren Release erhalten.
