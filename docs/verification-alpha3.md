# Prüfprotokoll — 0.1.0-alpha.3

Stand: 10.10.2026. Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, Original AFK Cinematics 1.0.6+26.3. Historie: [alpha.2](verification-alpha2.md).

## Ergebnis

Abschlusslauf `build runClientGameTest -Pcompat`: **BUILD SUCCESSFUL**, Exitcode **0**, abgeschlossen am **10.10.2026 um 22:21 Uhr Europe/Berlin**. Log: `.local/final-alpha3-validation.log`. **37 Unit-Tests ohne Fehler und vollständiger Client-GameTest bestanden.**

Die abschließenden Screenshots wurden angesehen: Haus als Motiv, lesbare Einstellungen sowie Gangstart und Blick nach der Ecke. Der offene Gangverlauf bleibt sichtbar; die zuvor dominierende Gegenwand und der Spielerkopf vor der Linse sind in diesen korrigierten Testaufnahmen nicht mehr vorhanden. Das ist eine begrenzte visuelle Kontrolle der Testszene, keine allgemeine Qualitätsgarantie.

Das Add-on bleibt rein clientseitig. Es ergänzt automatische Orts-Erinnerung, sichere kurze Gangfahrten und Bildprüfungen. AFKC behält Interpolation, Blickdrehung, normale Shot-Dauer, FOV und Balken/Übergänge. Nur bei einer geführten Gangfahrt ersetzen Wegziele die originale Seitenschwingung. Der Blick kann in einen freien Gangausgang vorausgehen; der Kameraweg wird dadurch nicht verlängert. Die „Made by“-Einblendung und AFKC-Musik werden unterdrückt, die Herkunftsnennung bleibt in NOTICE und Mod-Unterlagen erhalten.

## Logiktests

37 Tests in sieben Testklassen prüfen:

- Dauerhafte, räumlich zusammengefasste Bauprojekte neben allmählich abklingender aktueller Aktivität. Große alte Projekte dürfen einen aktuellen nahen Aufenthalt nicht immer verdrängen; ein Sitzplatz innerhalb eines großen Projekts bleibt ein eigenes lokales Motiv.
- Aufenthaltsqualifikation statt bloßem Durchlaufen, keine unbegrenzte Punktesammlung während der Cinematic, begrenzte AFK-Besuchswertung. Halbwertszeiten von 5/10/20 Minuten bestehen dieselben Szenarien; das beweist kein ästhetisches Optimum. Begründete Startwerte: [Recherche](activity-research.md).
- Speicherung über Sessions, Trennung von Welt/Server und Dimension, beschädigte Dateien, Alpha.2-Migration, Kapazität von 2.048 Zellen und zulässige Dateigröße bei voller Belegung.
- Unterschiedliche Motiv- und Winkelhinweise innerhalb des ursprünglichen Fokusbereichs, Ablehnung verdeckter/unzugänglicher Vorschläge.
- Wegführung um eine Ecke bei originaler Positionsglättung, Unterbrechung bei neuer Geometrie, keine Verbindung über Teleports/Aktivitätspausen, begrenzter Wegspeicher und Kameraradius.
- Streng freie Sicht bei Gangfahrten: Die Toleranz für sichtbare Oberflächen eines Bauprojekts darf hier keine Wanddurchsicht erlauben. Ein Blick in einen offenen Gangausgang bleibt hinter einer vorhandenen Endwand.
- Hindernisse vor der Linse und am Bildrand, Figuren im unmittelbaren Blickfeld, nahe frontal bildfüllende Wände. Ein kleiner freier Bildrand genügt nicht; normale Seitenwände eines offenen schmalen Gangs dürfen trotzdem vorkommen.
- Begrenztes erneutes Auswählen nach abgelehnten Shots, statt kompletter Motivsuche in jedem Tick.

Neue Fehlerfälle wurden jeweils zunächst mit fehlschlagenden Tests reproduziert. Dazu gehören die örtliche Erinnerung im großen Projekt, verbotene Routen-Sichttoleranz, Neustartschleife, wandfüllendes Bild mit freiem Rand sowie die zu strenge Bewertung schmaler Gänge.

## Tatsächlicher Minecraft-Lauf

Ein isolierter Client mit temporärer Welt prüft folgende Abläufe. Weltaufbau-Befehle laufen ausschließlich im Testserver; sie sind nicht Bestandteil der ausgelieferten Mod.

1. **Survival:** kein Kreativflug/`instabuild`, normale Platzierung verbraucht einen Block (64 → 63). Hebelbedienung mit einem Block in der Hand zählt nicht als Platzierung.
2. **Ohne Client-Berechtigungen:** `PermissionSet.NO_PERMISSIONS`; echte Eingabe von `afkdirector off`, `afkdirector on` und `afkc start` durch den Client-Befehlsweg. Keine Voraussetzung für OP oder Cheats im Add-on.
3. Start und Eingabeabbruch, native Shot-Dauer von 100–120 Ticks, weitere Bewegung nach angenommener Motivberatung. Originaler Credits-Timer bleibt null.
4. Keine automatische oder ausdrücklich angeforderte AFKC-Musik; die Musiklautstärke bleibt unverändert.
5. Tatsächliche Freecam-Aktivierung während der Startphase; FOV wird wiederhergestellt. Eine fremde Kamera-Entity erhält ebenfalls Vorrang.
6. Ende der Cinematic beim Dimensionswechsel, keine falschen Welt-Motive, Wiederladen der Erinnerungen bei Rückkehr, leerer aktiver Speicher nach Disconnect.
7. Vorhandenes Haus über sichtbare Oberflächen merken; Regen unter einem Dach; Einstellungsoberfläche.
8. Echtes Gehen entlang eines niedrigen L-Gangs, automatisches Lernen des Aufenthalts und Kameraweg über beide Schenkel. Kein Dach-/Wandkontakt, keine initiale Kamera im Spieler und unveränderte native Shot-Dauer.
9. Vollständig eingeschlossener enger Standort: Kamera startet nicht, HUD/FOV werden wiederhergestellt; nach 20 Ticks sind keine Start-/Fade-Timer mehr aktiv und es entsteht keine Neustartschleife. Vor dem Fix schlug genau dieser Test mit `inactivityTicks=520` fehl.

## Gemeinsam geladene Referenz-Mods

Sodium **0.9.2+mc26.3**, Iris **1.11.7+mc26.3**, Entity Culling **1.11.3**, Mod Menu **21.0.0**, Freecam **1.5.0-alpha.2**, Zoomify **2.17.0+mc26.3**, Do a Barrel Roll **3.8.5**, Flashback **0.43.6**, jeweils mit Pflichtbibliotheken. Versionen und Hashes: `compatibility-lock.json`. Der Abgleich zum gesamten Mitschnitt steht in [compatibility.md](compatibility.md).

Iris läuft **ohne aktiviertes Shaderpack**. Freecam wurde tatsächlich umgeschaltet; Zoom-, Replay- und Export-Funktionen wurden nicht vollständig durchgespielt. Windows-Performancecounter-, Entwicklungs-Refmap- und Realms-Testkonto-Warnungen im Log bleiben dokumentierte Umgebungs-/Fremdmod-Warnungen; ein erfolgreicher Test bedeutet kein völlig warnungsfreies Log.

## Unabhängige Codeprüfung

Korrigiert wurden die Kombination aus nativer Standpunktverschiebung und zu großzügiger Sichttoleranz, ungebremste Wiederholungsversuche nach abgelehnten Schnitten, wiederholter erfolgloser Cinematic-Start sowie die Ablehnung eines eigentlich offenen schmalen Gangs. Die letzte gezielte Nachprüfung von Start-Reset, inneren Tiefenproben und vorausschauender Gang-Blickrichtung fand keine weiteren wesentlichen Fehler.

## Grenzen und Praxistest

- Kein Test auf CraftAttack oder einem anderen fremden Multiplayer-Server; das Testspiel läuft in Survival mit lokal entzogenen Client-Berechtigungen auf dem integrierten Testserver. Das ist keine Prüfung des konkreten Anti-Cheats oder eine Serverfreigabe.
- Kein vollständiger Test des Creator-Packs mit Shader, keine FPS-/Frametime-Zusage. Modifizierte Blockformen und Entity-Culling können eine zusätzliche Sichtprüfung im echten Pack benötigen.
- Die Bildprüfung ist eine geometrische Heuristik. Ein mehrere Blöcke entferntes, geometrisch zulässiges Motiv kann trotzdem langweilig sein. **Keine Garantie für durchgehend Apple-TV-artige Bildqualität.**
- Die Screenshots der künstlichen Stein-/Holz-Testwelt sind technische Nachweise, kein ästhetischer Stream-Showcase. FOV und der konservative Kameraradius können Totalansichten großer Anlagen verhindern; sehr enge Räume können ganz ohne freigegebene Kamerafahrt bleiben.
- Bauaktivität ist keine verlässliche Eigentumserkennung. Eine sehr spät vom Server abgelehnte Blockänderung kann zunächst mitgezählt werden. Alte/abgerissene Bauten oder Server-Weltresets benötigen gegebenenfalls `clear`.
- Die Mod verändert keine Spielerposition, fordert keine zusätzlichen Chunks an und sendet keine eigenen Netzwerkpakete. Trotzdem gelten Serverregeln für entfernte Kameras.

Nächster Praxistest: am realen Haus/Farmbereich normal spielen, an mehreren Orten verweilen, `/afkc start`; danach Gang/Ecke, Fenster, Regen-Sitzplatz und Rückkehr nach Relog prüfen. Mit dem tatsächlichen Shader und einer kurzen Stream-Aufzeichnung Bildausschnitt, Wiederholung, Kameratempo und Frametime beurteilen.

## Ausgelieferte Datei

Die öffentliche MIT-Fassung wurde am 10.10.2026 um etwa 22:55 Uhr Europe/Berlin mit `build --rerun-tasks` neu gebaut: **BUILD SUCCESSFUL**, Exitcode 0, alle **37 Unit-Tests** erneut ohne Fehler. Log: `.local/final-public-build.log`. Die 28 enthaltenen Programmklassen sind bytegleich zur zuvor vollständig mit dem Client getesteten Alpha.3; die Änderung betrifft Lizenz, NOTICE und Mod-Metadaten.

Datei: `dist/afkc-director-addon-0.1.0-alpha.3.jar` — **62.192 Bytes**.

SHA-256: `b992051f12427b9f2cde0535697e2b6de0414d1d81ec1f048c3cf91ec06d8e3a`.

Archiv geprüft: 43 Einträge, client-only, Version alpha.3, exakte AFKC-Abhängigkeit, MIT-LICENSE und NOTICE vorhanden; keine Original-/Minecraft-Klassen, Testklassen, eingebetteten Fremd-JARs oder Audioassets. Original-JAR unverändert: `3b4b244a9fb68087f1c7a275bd5b54c5e65c6d311c5efab15f250b885d37e8a7`.

Zwei weitere erfolgreiche isolierte Minecraft-Läufe erzeugten die [Video-Beispiele](video-comparison.md), einmal mit und einmal ohne geladenes Add-on. Beide fertigen MP4-Dateien wurden dekodiert und visuell anhand ihrer Bildfolgen kontrolliert: 24 Sekunden, 1280 × 720, H.264, 720 Frames, keine Audiospur. Das sind ausgewählte echte Aufnahmen, kein Nachweis einer allgemeinen ästhetischen Überlegenheit.

Die Original-JAR bleibt eine separate Abhängigkeit. Download über das [öffentliche GitHub-Alpha-Release](https://github.com/Lugesmuges/afkc-director-addon/releases/tag/v0.1.0-alpha.3). Keine öffentliche Modrinth-Veröffentlichung ausgeführt.
