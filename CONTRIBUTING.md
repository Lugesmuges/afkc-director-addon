# Mitmachen / Contributing

Danke fürs Ausprobieren! Dieses Projekt entstand aus einer Zuschauer-Idee und überwiegend durch KI/Vibe Coding. Der Initiator ist kein erfahrener Mod-Entwickler und hat nur begrenzte Hardware zum Testen. Fachkundige Reviews, kleine nachvollziehbare Verbesserungen und ehrliche Praxistests sind sehr willkommen.

## Besonders hilfreich

- Kurze Beispiele für unpassende Bildausschnitte, Wiederholungen oder Kollisionen.
- Tests mit dem tatsächlich verwendeten Modpack und Shader; Versionen und Hardware angeben.
- Messungen zu FPS/Frametimes, sparsame Verbesserungen der Motivwahl und kompatible Kameraübergaben.
- Kleine Pull Requests mit Problem, Änderung und passendem Test. Bitte keine Features einschmuggeln, die Serverrechte, Netzwerkpakete oder Spielerbewegung benötigen.

## Lokal bauen

Java 25, Minecraft 26.3 und die separat heruntergeladene `afk-cinematics-1.0.6+26.3.jar` im Repository-Verzeichnis. Dann `gradlew.bat build` (Windows) oder `./gradlew build`. Die Original-Mod ist nicht in diesem Repository enthalten. Quelle und Autor: [AFK Cinematics von spunkyinsaan](https://modrinth.com/mod/afk-cinematics).

`test` prüft die reine Logik. `runClientGameTest` erzeugt einen isolierten Minecraft-Testclient. `-Pcompat` ergänzt lokal bereitgestellte Testmods aus `.local/compat-runtime`; deren Versionen stehen in `docs/compatibility-lock.json`. Test- und Aufnahmefixtures sind nicht Teil der ausgelieferten JAR.

## Offenheit

Der eigene Code steht unter MIT. Bitte KI-Unterstützung bei Beiträgen ebenfalls offenlegen und den Code vor dem Einreichen selbst prüfen. Drittcode nur mit passenden Rechten beitragen; keine Original-JARs oder dekompilierten Original-Klassen hochladen. Vor dem Teilen von Logs persönliche Daten und Serveradressen entfernen.

English: Contributions, forks, tests and code reviews are welcome under MIT. This is a primarily AI-assisted experimental client-only addon. Keep contributions small and reproducible; disclose AI assistance. Do not redistribute the original AFK Cinematics JAR or decompiled source. See the README and verification report for limitations.
