# Prüfprotokoll — 0.1.0-alpha.5

Stand: 11.10.2026. Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, AFK Cinematics 1.0.6+26.3. Historie: [Alpha.4](verification-alpha4.md).

## Ergebnis

`build runClientGameTest -Pcompat`: **BUILD SUCCESSFUL**, Exitcode **0**, abgeschlossen am **11.10.2026 um 07:45 Uhr Europe/Berlin**. Log: `.local/alpha5-final-validation.log`. **71 Unit-Tests ohne Fehler und vollständiger Client-GameTest bestanden.** Das geprüfte Bild zeigt die komplette 48 Blöcke breite Demo-Anlage mit echter Statistik-Karte.

## Änderungen und Fehlernachweise

Totalen berechnen Abstand und Bildwinkel aus allen acht Ecken des gemerkten Projekts, mit Platz für schwarze Balken und die gesamte Kamerabewegung. Separater Maximalradius standardmäßig 96 Blöcke; Details bleiben bei 16. Die effektive Server-Sichtweite begrenzt zusätzlich den Abstand zwischen Kamera und Bauwerk. Der Bildwinkel bleibt pro Einstellung konstant, bei Bedarf bis 70 Grad.

Der erste Clientlauf bestand die Positionsprüfungen, zeigte aber im Bild nur Blau. Drei zusätzliche Messungen bestätigten die korrekte tatsächliche Render-Kameraposition und Blickrichtung. Ursache: fünf Chunks Server-Sichtweite ergeben vollständigen Nebel bei 80 Blöcken; die nächste Gebäudefläche lag über 88 Blöcke entfernt. Belege: `.local/alpha5-integration.log`, `.local/alpha5-wide-render-diagnostic.log`. Der Sichtweiten-Regressionstest scheiterte zuerst (`alpha5-fog-red.log`). Eine zusätzliche Ansicht auf Gebäude-Mittelhöhe erlaubt die vollständige Totale auch bei knapper Sichtweite und leicht seitlicher Spielerposition. Reine Himmelsbilder ohne reale Szene im inneren Bild werden als Ersatzaufnahme verworfen.

Der Clienttest reproduzierte außerdem den vorzeitigen Linsenwechsel bei verworfenem Folgeschnitt: `Rejected cut changed the held wide lens from 55 to 30` (`alpha5-lens-red.log`). Die zuletzt akzeptierte Linse bleibt jetzt bis zum nächsten angenommenen Schnitt erhalten.

Die unabhängige Codeprüfung fand zwei Statistikfehler: widersprüchliche UUIDs konnten wegen Namensgleichheit passieren, und ein HTTP-Timeout beendete nur das Future statt einer hängenden Übertragung. Drei neue Regressionen scheiterten zuerst und bestehen nach UUID-Priorisierung und aktiver Subscription-/Transport-Cancellation. Die Text-Alpha-Verarbeitung wurde gegen Minecraft 26.3 geprüft; kleine Alphawerte bleiben erhalten.

## Prüfumfang

71 Unit-Tests einschließlich bisheriger Fälle sowie vollständiges Framing und Bewegung, effektive Sichtweite, seitliche Spielerposition, gesperrte Wege, Himmelsablehnung, Statistik-Einheiten, fehlende/veraltete Websitewerte, UUID-Zuordnung, Weltwechsel, Abrufgrenzen, Streaming-Limit, Übertragungsabbruch und Karten-Timing/Fades.

Der vollständige Clientlauf prüft Survival-Platzierung mit Blockverbrauch, lokale Befehle ohne Rechte, Start/Eingabeabbruch, stille Original-Mod, ausgeblendete Credits, echte Freecam-Übergabe, fremde Kamera, FOV/HUD-Rückkehr, Dimensionswechsel/Persistenz, Haus/Regen/Gang und abgelehnten Start im geschlossenen Raum. Neu: 140 Ticks einer Großprojekt-Totale, fester Bildwinkel, ganze Gebäudegrenze im Frame, reale Minecraft-Statistikantwort, Entfernen der Anzeige nach Eingabe und Linse bei verworfenem Folgeschnitt.

## Kompatibilität und Grenzen

Gemeinsam geladen: Sodium 0.9.2+mc26.3, Iris 1.11.7+mc26.3, Entity Culling 1.11.3, Mod Menu 21.0.0, Freecam 1.5.0-alpha.2, Zoomify 2.17.0+mc26.3, Do a Barrel Roll 3.8.5 und Flashback 0.43.6 mit Bibliotheken. [Modinventar](compatibility-lock.json), [gesamter Mitschnitt](compatibility.md).

Iris ohne aktives Shaderpack. Kein CraftAttack-, vollständiger Creator-Pack- oder Anti-Cheat-Test; keine FPS-Garantie. Die große Fixture legt eine synthetische Anlage und reproduzierbare Bau-Erinnerungen an. Sie belegt Kameraframing, keine automatische Rekonstruktion unbekannter Gebäude. Ein Test an der tatsächlichen Ölplattform mit Shader und Server bleibt nötig.

Entwicklungs-Refmap-, Windows-Performancecounter- und Realms-Testkonto-Warnungen bleiben vorhanden. Ein einzelner Windows-Zugriffsfehler beim Ersetzen einer Erinnerungsdatei trat auf; spätere Speicher-/Dimensionswechselprüfungen bestanden. Das Add-on behält ungespeicherte Daten für einen späteren Schreibversuch. Grün bedeutet kein warnungsfreies Log.

Original-JAR, Minecraft-Klassen, Testwelt und Testklassen werden nicht mitverteilt.

## Ausgelieferte Alpha.5

`dist/afkc-director-addon-0.1.0-alpha.5.jar` — 101.678 Bytes, 61 Archiveinträge. SHA-256: `96b1a21dcd35a264c840603b04f7985863e61b6d51f970f2b7a101340554e3d2`.

Client-only, MIT, exakte AFKC-Abhängigkeit. Keine Original-/Minecraft-Klassen, Testklassen, Fremd-JARs oder Audioassets im Archiv.

Der zusätzliche Aufnahme-Lauf `runClientGameTest -Pcompat -Pshowcase=wide` bestand am 11.10.2026 um 07:49 Uhr. Er prüfte über 53 Sekunden jede Sekunde den tatsächlichen Render-Blick auf vorhandene Szene. Das final geprüfte Video ist ein fortlaufender 48-Sekunden-Ausschnitt mit mehreren Einstellungen: H.264, 1280×720, SAR1:1, 16:9, 30fps-Ausgabe, 1440 Frames, keine Audiospur, vollständig fehlerfrei dekodiert. Die begrenzte Testhardware verursacht einzelne doppelte Aufnahmeframes; es wurden keine Zwischenbilder erzeugt. Sichtkontrolle von Gesamtfolge und zweiter Einstellung: keine vollständige Himmels-/Nebelansicht in diesem Clip.
