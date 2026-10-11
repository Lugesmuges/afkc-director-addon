# AFKC Director Add-on — Alpha 0.1.0-alpha.6

Kleine Zusatz-JAR für **AFK Cinematics 1.0.6+26.3** von **spunkyinsaan**. Das Add-on merkt sich Baugebiete, Aufenthalte und wiederkehrende AFK-Plätze und berät die vorhandenen Kamerapresets. Seit Alpha.4 ersetzen langsame Heran-, Heraus-, Seitwärts- und kleine Aufwärtsfahrten das ursprüngliche Pendeln. Ein Motiv bleibt über die gesamte Einstellung fest. AFK Cinematics behält die Interpolation zwischen Renderbildern, AFK-Erkennung, schwarze Balken sowie Ein- und Ausstieg. Die „Made by“-Texteinblendung ist ausgeblendet, die Autorennennung in den Mod-Informationen bleibt erhalten.

**Keine zusätzlichen Sounds oder Musik.** AFKC-Musik wird unterdrückt, auch ausdrücklich gewählte Musik per Original-Befehl. Minecraft-, Regen-, Sound-Mod- und Voice-Chat-Audio werden nicht verändert. Die Original-JAR bleibt unverändert und muss separat installiert sein.

## Installation

**[Download der Alpha-JAR und Beispielvideos](https://github.com/Lugesmuges/afkc-director-addon/releases/tag/v0.1.0-alpha.6)** · **[Quellcode / Mitarbeit](https://github.com/Lugesmuges/afkc-director-addon)**

1. Minecraft **26.3**, Java **25**, Fabric Loader **0.19.5+** und Fabric API **0.162.0+26.3 oder neuer für 26.3** verwenden.
2. `afk-cinematics-1.0.6+26.3.jar` und `afkc-director-addon-0.1.0-alpha.6.jar` in den `mods`-Ordner des Profils legen. Eine ältere Director-Add-on-JAR ersetzen. Keine `-sources.jar` installieren.
3. Eine Welt oder einen Server betreten und bauen. Erfolgreiche Änderungen werden nach ungefähr einer Sekunde übernommen.
4. In der Nähe bleiben und `/afkc start` verwenden oder auf den normalen AFK-Start warten. Eingabe beendet die Aufnahme wie im Original.
5. Einstellungen über **Mod Menu → AFKC Director Add-on** oder `/afkdirector settings`. AFK-Zeit und Balken werden weiterhin in AFK Cinematics eingestellt. Die neuen ruhigen Fahrten sind bei aktiviertem Add-on automatisch aktiv; die ursprüngliche Shake-Stärke beeinflusst sie nicht.

Nur auf dem Client installieren. Kein Server-Plugin, keine eigenen Netzwerkpakete, keine Spielerbewegung. Serverregeln für entfernte Kameras gelten weiterhin.

**Survival ohne OP:** Kreativmodus, Cheats und Adminrechte sind keine Voraussetzungen. `/afkdirector` und der Originalbefehl `/afkc start` werden clientseitig verarbeitet. Das Add-on beobachtet vorhandene Blöcke und normale Spieleraktionen; es baut, fliegt oder teleportiert den Spieler nicht. Die Weltaufbau-Befehle im Testcode werden ausschließlich vom isolierten Testserver ausgeführt und stecken nicht in der ausgelieferten JAR.

## Intelligenteres Motiv und ruhige Kamerafahrten

- Eigene bestätigte Platzierungen und Abbauten sowie qualifizierte Aufenthalte bilden die Motiv-Erinnerung. Kurzes Durchlaufen zählt nicht wie Verweilen. Ein tatsächlicher Cinematic-Start merkt den Platz; die anschließende AFK-Dauer sammelt keine weiteren Aufenthaltspunkte.
- Aktuelle Aktivität verliert stufenlos Gewicht (Startwert: zehn Minuten Halbwertszeit, keine 30-Minuten-Abschneidegrenze). Bauprojekte bleiben separat gespeichert. Wiederholte AFK-Besuche zählen über Sessions hinweg; eine einzelne lange Abwesenheit zählt nicht als viele Besuche. Die Startwerte sind begründete Heuristiken: [Recherche und Schwellenwerte](docs/activity-research.md).
- Benachbarte Bauzellen werden zu größeren Projekten zusammengefasst. So bleibt ein über Stunden gebautes Haus ein Motiv; die Auswahl reduziert sich nicht auf die letzten Blöcke.
- Der Bildausschnitt wechselt zwischen dem zusammengefassten Projekt, einzelnen Bereichen und Umgebung. Original-Presets liefern unterschiedliche Ausgangspositionen und Höhen. Wenn möglich, wird der zuletzt benutzte Winkel vermieden. Auch gelegentliche Figurenaufnahmen erhalten die ruhige Bewegung.
- Normale Einstellungen dauern 18–26 Sekunden. Die Kamera fährt in einer durchgehenden Richtung mit sanftem Anfahren und Abbremsen; ihr Blick bleibt am gewählten Motiv. Heran- und Herausfahrten erzeugen einen Zoom-Eindruck mit räumlicher Tiefenwirkung, ohne den FOV pro Shot zu verändern. Kein künstliches Zittern, kein seitliches oder vertikales Pendeln.
- Jede kurze Fahrt wird vor dem Schnitt auf freien Weg, Sicht und Kameraradius geprüft und während der Bewegung erneut abgesichert. Bei zu wenig Platz bleibt ein akzeptiertes Bild ruhig stehen. Neue Hindernisse beenden die Fahrt; die Kamera versucht nicht, sich mitten im Shot durch seitliche Korrekturen zu retten.
- Sitzen oder ein geschützter Standort bei Regen kann die zuletzt gewählte Blickrichtung als zusätzliches Motiv in die Folge aufnehmen. Es gibt keinen eigenen starren Cozy-Kameramodus; Blickwinkel und Fahrtrichtung wechseln zwischen den Einstellungen.
- Das Add-on schlägt nur nahe, freie Standpunkte vor. Es prüft geladene Geometrie, Weg zum Standpunkt und Sicht aufs Motiv. Reine Himmelsbilder ohne Landschaft oder Bauwerk im inneren Bildbereich werden als Ersatzaufnahme verworfen. Jeder native Schnitt und die tatsächliche Blickrichtung werden zusätzlich auf nahe Hindernisse und sichtbare Figuren vor der Linse geprüft. Ein kleiner freier Spalt am Bildrand reicht nicht, wenn der Rest eine nahe Wand zeigt. Angepasste Bewegungen werden räumlich abgesichert. Bei ungeeigneter Sicht bleibt zunächst das letzte angenommene Bild stehen, während eine neue Aufnahme gesucht wird. Gibt es bereits zum Start keinen brauchbaren Standpunkt, bleibt die Spielansicht erhalten und ein neuer Versuch wartet die eingestellte AFK-Zeit ab.
- Kurze, beobachtete Laufwege können gelegentlich zu Detailfahrten werden, auch um eine Ecke. Maximal fünf Blöcke Weg, geladene und aktuell erneut geprüfte Geometrie, innerhalb des Kameraradius. Die Original-Glättung steuert die Bewegung; an engen Ecken darf die Blickdrehung erst aufholen. Bei fehlender sicherer Strecke bleiben normale Aufnahmen zuständig.
- Wenn kein geeignetes Bau-Motiv gefunden wird, liefert das Original-Preset das Motiv; auch dann wird die Bewegung beruhigt. Große Bauwerke können aus engen Räumen oder mit begrenztem Kameraradius nur in Teilansichten gezeigt werden.
- Freecam, Zoomify-Zoom, Flashback-Replay/-Export, fremde Kamera-Entity und Elytra-Flug haben Vorrang. Flashback-Aufnahme allein blockiert die Cinematic nicht.

## Cinematische Statistik-Karten

Ab sechs Sekunden Cinematic erscheint unten links jeweils ein echter Wert: etwa Spielzeit, gelaufene Strecke oder abgebaute Blöcke. Die Karte gleitet leicht ein und blendet weich ein/aus; es gibt keinen Ton. Eingabe und die Übergabe an eine andere Kamera-Mod beenden die Anzeige. Über Mod Menu können Karten vollständig ausgeschaltet werden.

Ab Alpha.6 sind die Karten um 28 % kleiner und immer deutsch beschriftet, auch bei englischer Spielsprache. Normale Fahrten dauern 18–26 Sekunden, geführte Gangfahrten 18 Sekunden. Auf- und Abwärtsfahrten sowie vier diagonale Varianten ergänzen Heran-, Heraus- und Seitenfahrten. Pro Einstellung bleibt der Blickpunkt fest. Zwischen Einstellungen liegt eine kurze Abblende (0,6 s) und sanfte Aufblende (1 s); das gilt auch nach passiven Server-Positionskorrekturen. Es wird keine zweite Szene für einen Crossfade gerendert.

Spieleraufnahmen wechseln bevorzugt zwischen Vorderseite, Seite und Rückseite. Gelegentlich sucht das Add-on eine Fensteransicht aus einem zuvor besuchten Innenraum. Klare und gefärbte Glasscheiben lassen den Blick durch, bleiben für die Kamera aber Hindernisse; getöntes Glas bleibt undurchsichtig. Ein aktuell freier Zugang über höchstens einen besuchten Wegpunkt muss nachweisbar sein. Ohne passenden Innenraum, Laufweg oder Fenster bleibt eine andere geprüfte Perspektive zuständig. Die Laufwegpunkte bestehen nur während der aktuellen Weltverbindung; Bau-Erinnerungen bleiben weiterhin über Sessions erhalten.

Minecraft liefert die normalen Statistiken des angemeldeten Spielers für die aktuelle Welt bzw. den Server. Dafür genügt die normale Statistikabfrage, wie im Minecraft-Statistikbildschirm; keine Adminrechte. Auf CastCrafters Client sind es seine Werte, auf einem anderen Client dessen eigene. Spielername und Quelle stehen auf jeder Karte.

Optional ergänzt das Add-on die öffentlichen Top-10-Listen von [craftattack.one](https://craftattack.one/stats/) über `/api/stats`. Automatisch geschieht das nur, wenn der gespeicherte Mehrspieler-Servername oder die Adresse „CraftAttack“ enthält. Andernfalls kann **CraftAttack-Webwerte: An** gezielt gewählt werden. **Aus** verhindert den Webabruf. Die Website wird höchstens alle fünf Minuten asynchron abgefragt. Es werden keine Spielerkoordinaten, Serveradressen oder Spielernamen übertragen; wie bei jedem HTTPS-Aufruf sieht der Webserver die IP-Adresse. Fehlende oder über 15 Minuten alte Websitewerte werden nicht erfunden und durch verfügbare Minecraft-Werte ergänzt. Die Website-Strecke wird neutral als „Strecke“ bezeichnet; Minecraft „zu Fuß“ zählt Gehen, Sprinten und Schleichen.

Die öffentliche Schnittstelle enthält derzeit keine vollständigen Einzelprofile. CastCrafter muss daher nicht in den Listen vorkommen. Eine Installation bei einem anderen Spieler kann seine fehlenden privaten Serverwerte nicht beschaffen.

## Befehle

| Befehl | Wirkung |
|---|---|
| `/afkdirector` | Status und Zahl gemerkter Zellen |
| `/afkdirector on` / `off` | Intelligente Motivberatung ein-/ausschalten |
| `/afkdirector remember` | Sichtbare Oberflächen im Blickbereich bis 40 Blöcke merken |
| `/afkdirector clear` | Gespeicherte Orte der aktuellen Welt/Dimension löschen |
| `/afkdirector settings` | Einstellungen öffnen |
| `/afkc start` | Aufnahme über AFK Cinematics starten |

**Schon fertiges Haus/Farm/Ölplattform:** Normales Bauen, Verweilen und AFK-Starts werden automatisch gemerkt; `/afkdirector remember` ist nicht nach jedem Login nötig. Für ein schon vor der Installation fertiges Bauwerk kann der Befehl zusätzlich sichtbare Oberflächen vormerken, gegebenenfalls von mehreren Seiten. Er rekonstruiert keine vollständige Gebäudegrenze.

**Gemütliche Ecke:** Hinsetzen, den gewünschten Blick auf Fenster/Regen/Umgebung wählen und die originale AFK-Cinematic starten lassen. Ein nachträgliches `/afkdirector remember` kann sichtbare Bauteile ergänzen. Die Geräuschkulisse kommt ausschließlich aus Minecraft und den vorhandenen Sound-Mods.

## Lokaler Speicher und Grenzen

Einstellungen: `config/afkc-director.properties`. Ortszusammenfassungen: `config/afkc-director-memory/`. Die Dateinamen enthalten einen Hash der Welt-/Serverkennung und Dimension, keine Klartext-Serveradresse. Keine Telemetrie; Bau-Erinnerungen bleiben lokal. Der unten beschriebene optionale Webabruf lädt ausschließlich öffentliche Statistiken. Speicherung etwa jede Minute, beim Cinematic-Start, Weltwechsel und Beenden; bei einem Absturz kann die letzte Minute fehlen. Alpha.2-Erinnerungen werden übernommen. Höchstens 512 Laufwegpunkte bleiben ausschließlich im Arbeitsspeicher und werden bei Weltwechsel verworfen.

Höchstens 2.048 Zellen je Welt/Server und Dimension. Bauaktivität verfällt nicht nach wenigen Stunden. Aktive Aufenthalte verlieren allmählich Gewicht. Zusammengefasste Projekte sind auf 96 Blöcke Ausdehnung begrenzt; größere Gebiete bleiben mehrere Motive. Ein Projekt wird nur berücksichtigt, solange der Spieler höchstens 24 Blöcke von seinem Gebiet entfernt ist. Detailaufnahmen bleiben höchstens 16 Blöcke vom Spieler entfernt (einstellbar: 8/12/16). Totalen besitzen einen eigenen Maximalabstand: standardmäßig 96, einstellbar 32/64/96/128 Blöcke. Tatsächlich verfügbare geladene Chunks, effektive Server-Sichtweite und freie Wege begrenzen ihn zusätzlich. Auch das Bauwerk selbst muss von der Kamera aus innerhalb der Sichtweite bleiben.

Das ist eine Heuristik für Bauaktivität, **keine verlässliche Eigentums- oder Bauwerkserkennung**. Zusammenhängende Arbeiten können zusammengefasst werden. Abgerissene Gebäude bleiben eventuell gespeichert; dafür gibt es `clear`. Ein Server-Weltreset unter derselben Adresse benötigt gegebenenfalls ebenfalls `clear`. Bei unbekannter Verbindungskennung bleibt die Erinnerung sitzungsbezogen.

Für Totalen berechnet das Add-on den Abstand aus der gemerkten Gebäudeausdehnung, Seitenverhältnis und den schwarzen Balken. Es bevorzugt den ursprünglichen 30°-Bildwinkel; nur falls nötig verwendet es pro Einstellung konstant 45°, 55°, 65° oder 70°. Der normale Spiel-Bildwinkel wird beim Beenden wiederhergestellt. Aus geschlossenen Räumen, bei unvollständiger Erinnerung oder zu wenig geladenem Gelände kann weiterhin keine vollständige Totale gelingen. Bobby-/Distant-Horizons-Ansichten erweitern die sichere Kamerareichweite nicht. Ein visueller Stream-Test mit dem tatsächlichen Creator-Modpack, Shader und Server bleibt erforderlich. Prüfstand: [docs/verification.md](docs/verification.md). Modliste: [docs/compatibility.md](docs/compatibility.md).

Zum Entfernen die Zusatz-JAR aus dem Profil nehmen. Die Original-Mod bleibt eigenständig nutzbar. Die beiden Konfigurationspfade können bei Bedarf ebenfalls entfernt werden.

## Selbst bauen

Java 25 installieren, die separat bezogene Original-JAR ins Projektverzeichnis legen und `gradlew.bat build` ausführen. Ausgabe: `build/libs/`. In der vorbereiteten Arbeitsumgebung auch `powershell -File tools/build.ps1`.

`gradlew.bat test` prüft die Logik. `gradlew.bat runClientGameTest` startet einen separaten Testclient mit temporärer Welt. Testklassen werden nicht in die Zusatz-JAR gepackt. `-Pcompat` ergänzt den lokal vorbereiteten Test-Modsatz aus `.local/compat-runtime`.

## Herkunft und Veröffentlichung

Original: [AFK Cinematics auf Modrinth](https://modrinth.com/mod/afk-cinematics), Autor **spunkyinsaan**. Inoffizielles Add-on, keine Veröffentlichung des Originalautors. Original-JAR und Analysedateien werden nicht mitverteilt.

Diese Testversion wurde überwiegend mit KI/Vibe Coding unter menschlicher Anleitung entwickelt. Die [Modrinth-Regeln, Abschnitt 6.2](https://modrinth.com/legal/rules) schließen eine öffentliche Veröffentlichung überwiegend KI-generierter Projekte derzeit aus. Deshalb wird diese Alpha über GitHub angeboten.

## Mitmachen

Der eigene Add-on-Code steht unter der [MIT-Lizenz](LICENSE). Änderungen, Forks und Pull Requests sind willkommen; die Rechte an AFK Cinematics und anderen Abhängigkeiten bleiben unberührt. Hinweise zur Mitarbeit: [CONTRIBUTING.md](CONTRIBUTING.md).

Hinter dem Projekt steht eine Zuschauer-Idee, kein erfahrener Mod-Entwickler. Die verfügbaren Testgeräte sind begrenzt. Tests auf weiteren Systemen, Feedback zu Kameraperspektiven und Verbesserungen an Performance oder Code helfen deshalb besonders. Bitte Minecraft-/Mod-Versionen und ein kurzes reproduzierbares Beispiel nennen; Logs vor dem Teilen von Serveradressen, Chat-Inhalten und persönlichen Daten bereinigen.
