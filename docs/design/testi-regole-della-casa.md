# Testi delle regole della casa nella Scalata — F1G-09

Stato: **approvato per delega (D16), 2026-10-31, revisione indipendente**. Scritta il 2026-10-31 dall'agente e rivista in modo indipendente (F1G-20) contro `docs/rules/scopa.md` e `docs/rules/glossary.md`: Michele ha delegato l'approvazione dei testi. Può comunque cambiare ogni testo indicando la chiave.

Cosa ha cambiato la revisione (prima → dopo):

- `rule_lesson_re_bello`: «il Re di Denari vale un punto in più a chi lo prende» → «il Re di Denari, il Re bello, vale un punto a chi lo prende» (en: «the King of Coins, the Re bello, is worth a point…»; «It is no ordinary card» → «It's no ordinary card»). Motivi: il termine del glossario («Re bello») mancava; «in più» poteva far pensare a un bonus sopra un valore già dato, mentre la spec §6.2 assegna 1 punto a chi lo prende. «Non è una carta qualsiasi» è mantenuto (lo cerca il test di schermata).
- `rule_lesson_napola`: «Asso, due e tre di Denari valgono 3 punti, e uno in più per ogni Denaro di fila.» → «Chi prende Asso, due e tre di Denari fa la napola: 3 punti, più uno per ogni Denari che segue in fila (4, 5…).» (en: «Whoever takes the Ace, Two and Three of Coins makes a napola: 3 points, plus one for each Coin that follows in a row (4, 5…).»). Motivi (spec §6.4): la napola è di chi prende le tre carte nella smazzata e la sequenza parte dalle tre (4, 5, …), cosa che il vecchio testo non diceva; «Denaro» al singolare non è nel glossario; il nome «napola» non compariva.
- `rule_lesson_asso`: «L'Asso prende tutto il tavolo quando non ci sono altri Assi. Usalo con giudizio.» → «Se sul tavolo non ci sono Assi, il tuo Asso prende tutte le carte. Non conta come scopa.» (en: «If there are no Aces on the table, your Ace takes every card there. It doesn't count as a sweep.»). Motivi (spec §4.10): «altri Assi» era impreciso (si parla degli Assi sul tavolo); «non è una scopa» è una regola della spec utile a chi gioca; «Usalo con giudizio» era un consiglio non richiesto.
- `rule_lesson_redeal`: it invariato (corretto rispetto a §3.3); en «It is not a mistake: it is the rule.» → «That's not a mistake: it's the rule.» (forma naturale, come il resto dell'inglese dell'app).
- Invariati: «Il Maestro spiega» / «The Maestro explains» e «Sempre attiva in questa sfida» / «Always on in this challenge» (brevi, nel tono, corretti).

Voce del Maestro: calda, breve, mai sarcastica, dà del «tu». Ogni mini-lezione è di una o due frasi e **non aggiunge regole**: ripete soltanto ciò che dice `docs/rules/scopa.md` (§3.3, §4.10, §6.2, §6.4).

## Quando compaiono

Nel pre-partita, in Classica, la **prima volta** che una regola entra in vigore (contro Nonna Rosa: Re bello; Tonino: tre Re; Don Alfonso: napola; il Maestro: asso che taglia). Si considera «vista» quando la partita parte; non compare più, nemmeno contro gli avversari successivi. Non ci sono mini-lezioni in Accademia (fuori dal perimetro di F1G-09).

## Testi

- `pre_game_rule_always_on` (etichetta accanto a ogni regola fissa)
  - it: Sempre attiva in questa sfida
  - en: Always on in this challenge
- `rule_lesson_title`
  - it: Il Maestro spiega
  - en: The Maestro explains
- `rule_lesson_re_bello`
  - it: Da qui in poi il Re di Denari, il Re bello, vale un punto a chi lo prende. Non è una carta qualsiasi.
  - en: From here on, the King of Coins, the Re bello, is worth a point to whoever takes it. It's no ordinary card.
- `rule_lesson_redeal`
  - it: Se all'inizio sul tavolo ci sono tre o quattro Re, si rimescola e si ridistribuisce. Non è un errore: è la regola.
  - en: If the table opens with three or four Kings, the cards are reshuffled and dealt again. That's not a mistake: it's the rule.
- `rule_lesson_napola`
  - it: Chi prende Asso, due e tre di Denari fa la napola: 3 punti, più uno per ogni Denari che segue in fila (4, 5…). Tienili d'occhio.
  - en: Whoever takes the Ace, Two and Three of Coins makes a napola: 3 points, plus one for each Coin that follows in a row (4, 5…). Keep an eye on them.
- `rule_lesson_asso`
  - it: Se sul tavolo non ci sono Assi, il tuo Asso prende tutte le carte. Non conta come scopa.
  - en: If there are no Aces on the table, your Ace takes every card there. It doesn't count as a sweep.

Nelle stringhe Android l'apostrofo va escapato (`all\'inizio`, `d\'occhio`).

## Da confermare

1. I testi.
2. Il nome e la frase di `rule_lesson_title` («Il Maestro spiega»).
3. «Sempre attiva in questa sfida» come etichetta al posto dell'interruttore.
