# Prüfprotokoll — 0.1.0-alpha.4

Stand: 10.10.2026. Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, Original AFK Cinematics 1.0.6+26.3. Historie: [Alpha.3](verification-alpha3.md).

## Ergebnis

`build runClientGameTest -Pcompat`: **BUILD SUCCESSFUL**, Exitcode **0**, abgeschlossen am **10.10.2026 um 23:23 Uhr Europe/Berlin**. Log: `.local/final-alpha4-validation.log`. **47 Unit-Tests ohne Fehler und vollständiger Client-GameTest bestanden.**

## Änderung und Fehlernachweis

Die Alpha.3 verbesserte die Motivwahl, ließ aber außerhalb der Gangfahrten die ursprünglichen Pendelbewegungen und das künstliche Kamerazittern bestehen. Auf das visuelle Feedback hin werden normale Einstellungen nun als einzelne ruhige Fahrten mit festem Motiv geplant. Gestaltung und Quellen: [camera-motion.md](camera-motion.md).

Der echte Minecraft-Test scheiterte zunächst mit `Enabled director still adds artificial camera shake` (`.local/steady-motion-red.log`). Nach der Umstellung wurde zusätzlich der wiederholte letzte Interpolationsschritt reproduziert: `Rejected shot replays the last movement on every render tick` (`.local/steady-motion-hold-red.log`). Beide Fälle sind im Abschlusslauf behoben.

Eine unabhängige Codeprüfung fand inkonsistente Radiusprüfung bei nicht beratenen Original-Presets. Der Schnitt wird jetzt schon vor dem Anzeigen abgelehnt, wenn Startposition, freier Weg vom Spieler, Sicht oder Bildprüfung nicht passen. Das verhindert kurze sichtbare Einstellungen, die erst im anschließenden Tick wegen des Radius abbrechen. Ein weiterer Review-Hinweis schloss denselben Interpolationsfehler bei direkt innerhalb einer Gangfahrt verworfenen Schnitten.

## Neue Prüfungen

- Sechs Logiktests für monotone Heran-/Herausfahrt, unterscheidbare Seiten-/Aufwärtsfahrten, festen Fokus, sanfte Beschleunigung/Verzögerung, begrenzte Geschwindigkeit, Radius und Hindernisse einschließlich verdeckter Wegmitte. Die bisherigen 37 Tests bleiben grün.
- Vier weitere Logiktests prüfen große angeschnittene Vordergrundfiguren, vollständige Figuren, außerhalb des Bildes liegende Figuren und Schnittpunkte an der Kameraebene. Der echte Client-Test scheiterte vor der Korrektur mit `A large foreground player cropped by the frame was accepted` (`.local/cropped-actor-red.log`) und besteht jetzt. Eine gezielte unabhängige Prüfung dieser Ergänzung ergab keine wesentlichen Befunde.
- 120 aufeinanderfolgende Minecraft-Ticks einer realen beratenen Einstellung: Kamera bleibt auf ihrer geraden Bahn, fährt nicht rückwärts und richtet sich auf dasselbe Motiv. Die Blickvektoren werden vor dem Winkelvergleich normalisiert, weil Minecraft hier trigonometrische Näherungswerte liefert.
- Der tatsächlich zurückgelegte Weg wird mit dem geplanten Fortschritt verglichen. Eine vorherige pauschale Mindeststrecke von 0,3 Blöcken nach 120 Ticks war bei sicherheitsbedingt kürzeren Fahrten unzutreffend; kurze Fahrten müssen ebenso präzise ihrem geplanten Verlauf folgen.
- Ein echter unzulässiger Schnitt wird mit unberatenem Original-Start außerhalb eines auf acht Blöcke begrenzten Radius versucht und darf nie angezeigt werden.
- Ein Schnitt in den festen Boden wird verworfen. Die Render-Interpolation muss sofort und auch im nächsten Tick stillstehen, statt die vorherige Bewegung zu wiederholen.
- Normale Einstellungen dauern 200–280 Ticks; die beobachtete kurze Gangfahrt behält 100–120 Ticks und erreicht weiterhin beide Schenkel der getesteten Ecke.

Der vollständige bestehende Integrationstest bleibt erhalten: Survival-Platzierung verbraucht einen Block, Client-Befehle ohne Berechtigungen, Start/Eingabeabbruch, keine AFKC-Musik/Credits, echte Freecam-Übergabe, fremde Kamera-Entity, FOV/HUD-Rückkehr, Dimensionswechsel und gespeicherte Projekte, Haus/Regen/Gang sowie abgelehnter Start im geschlossenen Raum ohne Neustartschleife.

## Gemeinsam geladene Mods

Sodium **0.9.2+mc26.3**, Iris **1.11.7+mc26.3**, Entity Culling **1.11.3**, Mod Menu **21.0.0**, Freecam **1.5.0-alpha.2**, Zoomify **2.17.0+mc26.3**, Do a Barrel Roll **3.8.5**, Flashback **0.43.6**, jeweils mit Pflichtbibliotheken. Inventar und Hashes: [compatibility-lock.json](compatibility-lock.json). Gesamter Mitschnitt: [compatibility.md](compatibility.md).

Iris lief ohne aktives Shaderpack. Freecam wurde tatsächlich umgeschaltet; nicht jede Funktion aller anderen Mods wurde durchgespielt. Bestehende Entwicklungs-Refmap-, Realms-Testkonto- und Gradle-Deprecation-Warnungen bleiben vorhanden; grüner Test bedeutet kein völlig warnungsfreies Log.

## Grenzen

- Keine Ausführung auf CraftAttack oder einem fremden Multiplayer-Server; kein Test des dortigen Anti-Cheats oder vollständigen Creator-Packs mit Shader.
- Geometrische Sicherheitsprüfungen liefern keine Garantie für schöne Bildkomposition. Große Bauten können wegen FOV und Kameraradius weiterhin nur teilweise ins Bild passen.
- Die Wegprüfung ist begrenzt und läuft vor dem Schnitt; keine gemessene FPS-/Frametime-Zusage für schwächere Hardware. Bei zu wenig Platz kann eine gültige Ansicht stehenbleiben.
- Die [Beispielvideos](video-comparison.md) sind echte ausgewählte Demo-Aufnahmen, kein systematischer Qualitätsvergleich oder Performance-Benchmark.
- Einstellungen, Bewegungsradius, Bau-Erinnerungen und clientseitiger Betrieb bleiben erhalten. Die Original-JAR wird weder geändert noch mitverteilt.

## Ausgelieferte Datei

`dist/afkc-director-addon-0.1.0-alpha.4.jar` — **66.543 Bytes**.

SHA-256: `50c78d44158b1232042cbf4f722c824f4945b4f30364e8054ad00707c6913bce`.

Archiv geprüft: **44 Einträge**, Version Alpha.4, client-only, exakte AFKC-Abhängigkeit, MIT-LICENSE und NOTICE. Keine Original-/Minecraft-Klassen, Testklassen, eingebetteten Fremd-JARs oder Audioassets. Öffentlicher Download: [GitHub-Alpha.4](https://github.com/Lugesmuges/afkc-director-addon/releases/tag/v0.1.0-alpha.4).
