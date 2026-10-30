# Testi del primo avvio e ordine di sblocco della Home — F1G-01, F1G-03

Stato: **approvata per delega** (vedi §4), scritta il 2026-10-30. Per delega di Michele (D16, 2026-10-30) la bozza passa a una revisione indipendente e poi viene approvata senza attendere lui; l'esito della revisione è in fondo. Michele può cambiare qualunque frase indicando la chiave.

Voce del Maestro: calda, breve, mai sarcastica con chi sta imparando; dà del «tu». Nessuna regola nuova: si parla solo di presa singola e di somma, già nel capitolo 1 dell'Accademia.

## 1. Primo avvio (F1G-01)

Schermata unica: ritratto del Maestro, saluto, richiesta del nome (riusa il dialogo del nome), due azioni.

- `first_launch_greeting`
  - it: Benvenuto al circolo. Io sono il Maestro: ti insegno la Scopa una mano alla volta.
  - en: Welcome to the club. I'm the Maestro: I'll teach you Scopa one hand at a time.
- `first_launch_ask_name`
  - it: Come ti chiami? Così so come chiamarti al tavolo.
  - en: What's your name? So I know what to call you at the table.
- `first_launch_name_skipped`
  - it: Nessun problema: giochi da ospite. Il nome puoi darlo quando vuoi.
  - en: No problem: you play as a guest. You can give your name whenever you like.
- `first_launch_name_given`
  - it: Piacere, %1$s. Cominciamo con una mano facile.
  - en: Pleased to meet you, %1$s. Let's start with an easy hand.
- `first_launch_start_button`
  - it: Prima mano
  - en: First hand
- `first_launch_skip_button`
  - it: So già giocare
  - en: I already know how to play

Regole di comportamento: «So già giocare» porta direttamente alla Home con la sola azione «Gioca» (nessuna mano guidata); chi aggiorna l'app con dati esistenti non vede mai questa schermata.

## 2. Ordine di sblocco della Home (F1G-03)

Principio: nessuna voce finta; una funzione compare quando serve, con una frase del Maestro la prima volta. Chi ha già giocato prima dell'aggiornamento vede tutto.

| # | Funzione | Si mostra dopo | Frase di presentazione |
| --- | --- | --- | --- |
| 0 | Gioca (Sparring) | subito | — |
| 1 | Sfida del giorno | la prima partita vinta | `home_unlock_daily_puzzle` |
| 2 | Partita del giorno | la seconda partita vinta | `home_unlock_daily_match` |
| 3 | Missioni | la prima Sfida del giorno | `home_unlock_missions` |
| 4 | Accademia | la prima prova (Check) | `home_unlock_academy` |
| 5 | Bottega e Profilo | la prima ricompensa ricevuta | `home_unlock_shop` |

- `home_unlock_daily_puzzle`
  - it: Hai vinto! Da domani ti propongo ogni giorno un problema di carte: la Sfida del giorno.
  - en: You won! From tomorrow I'll set you a card problem every day: the Daily puzzle.
- `home_unlock_daily_match`
  - it: Ti è piaciuta? C'è anche una partita uguale per tutti, ogni giorno: provala.
  - en: Did you like it? There's also a match that's the same for everyone, every day: try it.
- `home_unlock_missions`
  - it: Ho preparato qualche piccola missione: chi gioca bene viene premiato.
  - en: I've set a few small missions: good play gets rewarded.
- `home_unlock_academy`
  - it: Vuoi capire perché una giocata è buona? In Accademia lo vediamo insieme.
  - en: Want to understand why a play is good? In the Academy we look at it together.
- `home_unlock_shop`
  - it: Hai guadagnato i primi crediti: in Bottega puoi cambiare tavolo e dorso delle carte.
  - en: You've earned your first credits: in the Shop you can change the table and card backs.

## 3. Decisioni lasciate aperte (non bloccano le bozze)

- Il backlog rimanda a una «tabella nel documento di ricerca §6» che non esiste: l'ordine qui sopra è la proposta; la riga «Partita del giorno» è un'aggiunta di F1G-15.
- Nelle stringhe Android gli apostrofi vanno escapati (`I\'m`, `You\'ve`).
- Q5: la mano d'esordio è in Sparring, la prima partita vera in Classica (proposta già nel backlog).
- La Partita del giorno è già in Home (F1G-15): finché F1G-03 non è implementato resta sempre visibile.

## 4. Revisione e approvazione

- Revisione indipendente del 2026-10-30: tre correzioni applicate (nome «Bottega», trigger dell'Accademia allineato al backlog, sblocchi di Partita del giorno e Missioni sfalsati) e due ritocchi di tono (`first_launch_name_skipped`, `home_unlock_missions`).
- **Approvata** il 2026-10-30 per delega di Michele (D16: «scrivi la bozza, falla controllare, poi auto-approvala»). Resta modificabile da Michele in qualsiasi momento.
- **Confermata da Michele** il 2026-10-30 («Confermo i tuoi consigli»): ordine di sblocco della Home e proposta Q5 (mano d'esordio in Sparring, prima partita vera in Classica).
