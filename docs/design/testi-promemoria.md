# Testi del promemoria quotidiano — F1G-13

Stato: **approvato per delega (D16), 2026-10-31, revisione indipendente**. Scritta il 2026-10-31 dall'agente e rivista in modo indipendente (F1G-20): Michele ha delegato l'approvazione dei testi. Può comunque cambiare qualunque testo indicando la chiave.

Cosa ha cambiato la revisione (prima → dopo):

- `reminder_prompt_message`: «Posso ricordarti, una volta al giorno al massimo […] che il tavolo è pronto. […] Android ti chiederà di confermare.» → «Posso ricordarti che il tavolo è pronto: una notifica al giorno al massimo, e solo se non hai ancora giocato. […] Se dici di no, non te lo richiedo. Se serve, Android ti chiederà di confermare.» Motivi: la frase con l'inciso era contorta; il consenso ora dice anche che non si insiste (dopo un «No» non si richiede, vero come da backlog); il permesso di sistema compare solo da Android 13 (minSdk 26), quindi «Android ti chiederà» era falso su molti telefoni. Stesse modifiche in inglese («one notification a day at most», «If you say no, I won't ask again», «If needed, Android will ask you to confirm»).
- `reminder_notification_text`: «Il tavolo è pronto, quando ti va di fare una mano.» → «Il tavolo è pronto, quando ti va.» (en «…whenever you feel like a hand.» → «The table is ready, whenever you like.»). Motivi: più breve (una riga) e meno insistente; «a hand» era un calco poco naturale.
- Invariati: titolo del consenso, «Sì, grazie»/«No, grazie», titolo e canale della notifica, voci delle Impostazioni (corrette, in tono, parità it/en).

Voce del Maestro: calda, breve, mai sarcastica, dà del «tu». Qui in più: **nessuna pressione e nessuna colpa**. Niente «ti stiamo aspettando», niente «non perdere la serie», niente punti esclamativi, niente conteggi di giorni saltati. Il promemoria dice solo che il tavolo è pronto.

Regole di comportamento (già nel backlog, riepilogate): al massimo una notifica al giorno; mai se l'utente ha già giocato oggi; nessun dato lascia il telefono; il consenso si chiede una sola volta, dopo la prima vittoria; un rifiuto non si richiede; l'interruttore è nelle Impostazioni.

## 1. Richiesta di consenso (Home, dopo la prima vittoria)

Riquadro su pergamena con due scelte alla pari. Spiega prima di chiedere; il permesso di sistema di Android compare solo dopo il «sì».

- `reminder_prompt_title`
  - it: Vuoi un promemoria?
  - en: Would you like a reminder?
- `reminder_prompt_message`
  - it: Posso ricordarti che il tavolo è pronto: una notifica al giorno al massimo, e solo se non hai ancora giocato. Resta sul tuo telefono e lo spegni quando vuoi dalle Impostazioni. Se dici di no, non te lo richiedo. Se serve, Android ti chiederà di confermare.
  - en: I can let you know the table is ready: one notification a day at most, and only if you haven't played yet. It stays on your phone and you can turn it off any time in Settings. If you say no, I won't ask again. If needed, Android will ask you to confirm.
- `reminder_prompt_accept`
  - it: Sì, grazie
  - en: Yes, please
- `reminder_prompt_decline`
  - it: No, grazie
  - en: No, thanks

Chiudere il riquadro vale «No, grazie».

## 2. Notifica

Canale a bassa importanza: niente suono, niente vibrazione.

- `reminder_notification_title`
  - it: Il Maestro
  - en: The Maestro
- `reminder_notification_text`
  - it: Il tavolo è pronto, quando ti va.
  - en: The table is ready, whenever you like.
- `reminder_channel_name` (nome del canale nelle impostazioni di Android)
  - it: Promemoria
  - en: Reminder
- `reminder_channel_description`
  - it: Un messaggio del Maestro al giorno, al massimo.
  - en: At most one message a day from the Maestro.

## 3. Impostazioni

Compare solo dopo la prima vittoria (nessuna voce finta, come in F1G-03).

- `settings_reminder`
  - it: Promemoria quotidiano
  - en: Daily reminder
- `settings_reminder_subtitle`
  - it: Una notifica al giorno al massimo, mai se hai già giocato.
  - en: At most one notification a day, never if you've already played.

## 4. Da decidere (scelte dell'agente, non nel backlog)

1. **Orario**: 19:00 locali, con tolleranza di 3 ore (se il telefono era spento o in Doze l'allarme inesatto può arrivare tardi; oltre le 22:00 la notifica di quel giorno salta). Non c'è ancora una schermata per cambiarlo, ma l'ora è già salvata nello stato.
2. **Interruttore**: chi ha risposto «No, grazie» può accenderlo dalle Impostazioni; in quel caso Android mostra il suo permesso (è un gesto volontario, non un'insistenza).
