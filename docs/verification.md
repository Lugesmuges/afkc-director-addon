# Prüfprotokoll — 0.1.0-alpha.6

Stand: 11.10.2026. Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, AFK Cinematics 1.0.6+26.3. Historie: [Alpha.5](verification-alpha5.md).

## Ergebnis

`build runClientGameTest -Pcompat`: **BUILD SUCCESSFUL**, Exitcode **0**, abgeschlossen am **11.10.2026 um 08:33 Uhr Europe/Berlin**. Log: `.local/alpha6-final-validation.log`. **80 Unit-Tests ohne Fehler und vollständiger Client-GameTest bestanden.**

## Änderungen und Nachweise

- Normale Bewegungen dauern 18–26 Sekunden und umfassen zehn Richtungsvarianten. Die Weglänge bleibt begrenzt, sodass die Kamerageschwindigkeit sinkt. Die Tests für weiterlaufende Bewegung nach 14 Sekunden sowie abwärts/diagonale Fahrt schlugen zuvor fehl (`alpha6-motion-red.log`).
- Spieleransichten wechseln bevorzugt zwischen Vorderseite, Seiten und Rückseite. Eine optionale Fensteransicht verwendet besuchte Innenräume und erneut geprüfte Zugänge. `WindowScene` bestätigt im echten Client die Sicht durch Glas, weiterhin physische Glaskollision, erreichbaren Innenraum, bewegte Fensteraufnahme sowie Ablehnung eines nachträglich geschlossenen Eingangs und getönten Glases.
- Gangfahrten dauern jetzt 18 Sekunden mit kleinerem Vorlauf statt schneller Bewegung. Der Geschwindigkeitsfall schlug zuvor fehl (`alpha6-corridor-red.log`); der vollständige Clienttest prüft beide Schenkel des Gangs ohne Dach-/Wandclip.
- Schnitte warten auf vollständige Schwarzdeckung: 0,6 Sekunden Abblende, eine Sekunde Aufblende, auf Renderzeit interpoliert. Ein eigener zuerst fehlgeschlagener Test schützt vor Aufblitzen bei schnellen Wiederholungsversuchen (`alpha6-transition-retry-red.log`). `TransitionScene` bestätigt den echten normalen Einstellungswechsel und den Originalaufruf nach passiver Positionskorrektur: keine vorzeitige Kameraversetzung, volle Deckung beim Wechsel und anschließende Freigabe des Bilds.
- Unabhängige Codeprüfung fand den zuerst ungeprüften passiven Refresh und das feste 16:9-Seitenverhältnis bei Spieleraufnahmen. Beide wurden korrigiert; die Nachprüfung meldete keine weiteren wichtigen Befunde.
- Statistik-Karten sind um 28 % kleiner und unabhängig von der Spielsprache deutsch. Sichtkontrolle der Großprojekt-Aufnahme bestätigt die kompakte Karte.

Der erste Clientlauf scheiterte an einem veralteten Testablauf: Der Linsen-Test wählte nun den neuen, gültigen Spieler-Schnitt und erwartete trotzdem die alte Linse. Der Test verwendet wieder einen tatsächlich verworfenen Folgeschnitt. Die bisherige Dauererwartung wurde an die ausdrücklich gewünschten längeren Einstellungen angepasst. Der endgültige vollständige Lauf enthält weiterhin die Linsenprüfung bei verworfenem Schnitt.

## Prüfumfang und Grenzen

Alle bisherigen Speicher-, Statistik-, HTTP-, Framing- und Kompatibilitätstests bleiben enthalten. Survival-Platzierung mit Blockverbrauch, Befehle ohne Adminrechte, Original-AFK-Start/Eingabeabbruch, unterdrückte AFKC-Musik, ausgeblendete Credits, Freecam-Übergabe, FOV/HUD-Rückkehr, Dimensionswechsel/Persistenz, Haus/Regen/Gang, komplette große Anlage innerhalb der effektiven Server-Sichtweite und Ablehnung eines Starts im versiegelten Raum bestanden.

Gemeinsam geladen: Sodium 0.9.2+mc26.3, Iris 1.11.7+mc26.3, Entity Culling 1.11.3, Mod Menu 21.0.0, Freecam 1.5.0-alpha.2, Zoomify 2.17.0+mc26.3, Do a Barrel Roll 3.8.5 und Flashback 0.43.6 mit Bibliotheken. [Modinventar](compatibility-lock.json), [gesamter Mitschnitt](compatibility.md).

Iris ohne aktives Shaderpack. Kein Test auf CraftAttack, mit dem vollständigen Creator-Pack oder einem Anti-Cheat. Fensteransichten brauchen eine geeignete Geometrie und passende Laufpunkte während der aktuellen Weltverbindung. Die begrenzte Suche kann nicht jeden komplexen Gebäudezugang finden. FPS hängen weiterhin von Welt, Shader und Hardware ab.

Entwicklungs-Refmap-, Windows-Performancecounter- und Realms-Testkonto-Warnungen bleiben vorhanden. Der einmalige große Testweltaufbau verursachte eine Überlastungsmeldung des integrierten Servers. Grün bedeutet kein warnungsfreies Log.

Original-JAR, Minecraft-Klassen, Testwelt und Testklassen werden nicht mitverteilt.

Die erste neue Videoaufnahme zeigte eine angeschnittene Spielerfigur am Rand. Ein zusätzlicher Regressionstest scheiterte zuerst (alpha6-actor-edge-red.log). Die Prüfung berücksichtigt den eigenen Spieler nun auch jenseits von vier Blöcken und reagiert bereits bei zwei Prozent sichtbar angeschnittener Bildfläche. Vollständige Figuren bleiben erlaubt. Der neue echte Clientfall bei sechs Blöcken und der vollständige Folgelauf bestanden; unabhängige Nachprüfung ohne wichtige Befunde.

## Ausgelieferte Alpha.6

Die Client-JAR umfasst 109.661 Bytes und 66 Archiveinträge. SHA-256: `4d60287b184e16b30b2c15f9606eecc5d7f0bdcfbeb838c27fd45dcbd47d014b`. Client-only, MIT, exakte AFKC-Abhängigkeit. Keine Original-/Minecraft-Klassen, Testklassen, Fremd-JARs oder Audioassets im Archiv.

Der abschließende Aufnahme-Lauf `runClientGameTest -Pcompat -Pshowcase=after` bestand am 11.10.2026 um 08:37 Uhr. Er bestätigte die automatisch gelernte Fensteransicht nach tatsächlichem Gehen und prüfte während der Aufnahme jede Sekunde die reale Kameraperspektive. Die protokollierte Spiel-Framerate lag bei 58–60 FPS; wiederholte Captureframes bleiben vorhanden.

Finales Video: `04-ruhige-fahrten-fenster-und-uebergaenge.mp4`, ein fortlaufender 70-Sekunden-Ausschnitt ohne Zusatzbeschriftungen, ohne Audiospur und ohne künstliche Zwischenbilder. H.264, 1280×720, 16:9, SAR1:1, 60fps-Ausgabe, 4200 Frames, 22.918.439 Bytes; vollständige Dekodierung ohne Fehler. SHA-256: `f584e2690723a68bbf592e307e1f3030d31899f68f6839875747ca061ff35866`.

Sichtkontrolle: Fensteransicht, Rücken-, Seiten- und Vorderansicht; der störende Randanschnitt der ersten Aufnahme ist in der finalen Folge nicht vorhanden. Drei Ab-/Aufblenden, kurze vollständige Deckung jeweils etwa 0,2–0,25 Sekunden; die übrige Übergangszeit besteht aus der weichen Helligkeitsänderung. Kein Beleg für durchgehend perfekte Bildgestaltung auf beliebigen Servern und Bauwerken.
