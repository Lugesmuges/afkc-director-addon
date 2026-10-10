# Kameraführung ab Alpha.4

Ziel nach dem visuellen Feedback zur Alpha.3: ruhige, lesbare Fahrten, die einem Bauwerk Zeit im Bild geben. Pro Einstellung gibt es ein festes Motiv und eine Bewegungsrichtung. Es wird nicht fortlaufend ein neues Motiv gesucht.

## Ursache der Unruhe

Bei der Untersuchung der lokalen AFKC-Referenz wurden zwei Arten additiver Bewegung gefunden: periodische Seiten-/Höhenbewegung und künstliches Zittern. Die Alpha.3 behielt diese bewusst bei; das entsprach zwar der damaligen Vorgabe, Originalbewegungen zu erhalten, aber nicht dem inzwischen konkretisierten visuellen Ziel.

Zusätzlich hielt die Bildprüfung nach einem verworfenen Schnitt das letzte Bild ohne Aktualisierung der Interpolations-Endpunkte. Der letzte kleine Bewegungsschritt konnte deshalb mit jedem Spieltick erneut abgespielt werden. Ein echter Client-Test reproduzierte diesen Fehler. Ab Alpha.4 werden bei einem verworfenen Schnitt und beim Anhalten beide Endpunkte auf den zuletzt angezeigten Zustand gesetzt.

## Gewählte Gestaltung

- Langsame Heran- und Herausfahrten, Seitwärtsfahrten in beide Richtungen, gelegentlich eine kleine Aufwärtsfahrt.
- Zehn bis vierzehn Sekunden für normale Einstellungen. Die kurzen, beobachteten Gangfahrten behalten ihre bisherige Wegführung und Dauer.
- Ein fester Blickpunkt und eine monotone Fortschrittskurve mit sanfter Beschleunigung und Verzögerung. Keine Richtungsumkehr oder Zufallsbewegung innerhalb der normalen Fahrt.
- Höchstens 2,8 Blöcke Weg, bei Aufwärtsfahrten höchstens 1,2. Bei nahen Motiven fällt der Weg entsprechend kürzer aus. Startwerte für diese Alpha, keine wissenschaftlich optimalen Filmregeln.
- Vollständige Wegprüfung vor dem Schnitt, erneute Prüfung während der Bewegung. Ein Hindernis führt zum Anhalten und einer neuen Einstellung, nicht zu einem seitlichen Ausweichruck. Bei fehlendem freiem Weg darf eine gültige Ansicht stehenbleiben.
- Große angeschnittene Figuren direkt im Vordergrund werden zusätzlich aussortiert. Dafür werden die sichtbaren Bounding-Boxen naher Spieler und Tiere in die Bildebene projiziert; vollständige Figuren bleiben erlaubt. Das ist eine begrenzte Kompositionshilfe, keine Bewertung des gesamten Bildinhalts.
- FOV, AFK-Erkennung, Eingabeabbruch, HUD-Rückkehr und schwarze Balken bleiben bei AFK Cinematics. Die Frame-Interpolation der Original-Kamera bleibt aktiv. Es wird keine zweite Renderkamera eingebaut.

Das Heranfahren ist eine Dolly-Fahrt: Die virtuelle Kamera bewegt sich durch den Raum. Ein optischer Zoom würde stattdessen den Bildwinkel verändern. Die räumliche Fahrt wurde hier bewusst gewählt; dadurch bleibt der FOV unverändert. [StudioBinder: Dolly-Fahrt und Unterschied zum Zoom](https://www.studiobinder.com/blog/dolly-shot-camera-movements/).

Die Umsetzung ist eigene Add-on-Logik für die Schnittstellen von AFKC. Originalquellcode wird nicht mitverteilt. Geometrische Tests ersetzen weiterhin keine ästhetische Beurteilung am tatsächlichen Bauwerk und Shader.
