# Stagione «Scalata dei Circoli» — scene a battute, versione 3 (finale: storia + guida)

Stato: **approvata per delega** il 2026-10-31 (vedi in fondo). Sostituisce le scene brevi di `docs/design/stagione-storia.md` (che resta come riferimento di trama e limiti). Le chiavi `story_*` e `guide_*` entrano in `strings.xml` con F1G-16, mai prima; Michele può cambiare qualunque frase indicando la chiave.

## Sinossi (5 righe)
1. Il circolo ha una sedia all'Olimpo dei Maestri, vuota da anni: se nessuno la occupa, la sala chiude.
2. Il Maestro ti prende come apprendista e ti porta di tavolo in tavolo, dal Bar all'Olimpo.
3. Il suo taccuino ha la prima pagina strappata: i quattro pezzi, scritti da lui, sono affidati a Gigi, Rosa, Tonino e Don Alfonso.
4. Cap. 5: Rosa rivela che il taccuino passa da maestro ad allievo. Cap. 9: il Maestro confessa che Don Alfonso lo batté e che la sedia vuota è la sua.
5. Rimessa insieme, la frase dice «Chi insegna impara due volte»: batti il Maestro, ricevi il taccuino, e il primo a cui insegni è Gigi.

## Obiettivo e posta in gioco
- **Obiettivo (nel benvenuto):** occupare la sedia del circolo all'Olimpo dei Maestri.
- **Posta in gioco:** la sala del circolo non chiude (voluta da Michele). Nessuna scadenza: la sedia resta vuota finché non sali.
- **Perché:** è concreta, senza timer e senza violenza, e fa sentire utile al luogo che ti accoglie. Il colpo di scena del cap. 9 la rende personale: la sedia era del Maestro.
- **Indicatore** (ogni capitolo): «Ancora N tavoli all'Olimpo dei Maestri», poi «Sei all'Olimpo dei Maestri!».

## Mistero a puntate
Frase: «Chi insegna impara due volte» (en: «Whoever teaches learns twice»), scritta dal Maestro. Pezzi: «insegna» (Gigi, cap. 2), «Chi» (Rosa, cap. 5, che riconosce la grafia di lui), «impara» (Tonino, cap. 8), «due volte» (Don Alfonso, cap. 10). Il Maestro li affidò a loro dopo aver perso, per non rileggerli (cap. 10). Rivelazione a metà: cap. 5. Colpo di scena: cap. 9. La «pagina col tuo nome» (cap. 6-7) è l'ultima, bianca: si chiude nell'epilogo. Il Circolo reagisce: Gigi tifa, Rosa si confida, Tonino diventa alleato, Don Alfonso parla.

## Regole del file
Battute ≤100 caratteri, pulsanti ≤18; aperture 3-5 battute, chiusure 2-3; un parlante per battuta (maestro, gigi, rosa, tonino, alfonso). Chiavi: `story_chNN_sceneM_speaker/_text`, `story_chNN_next_text/_button`, `story_chNN_close_sceneM_speaker/_text`; cap. 9 e 10 hanno anche `mid_scene` e `next2` (un passo per volta). Forme neutre. **Nome del giocatore:** `%1$s` solo nelle 3 varianti `_text_named` (benvenuto, cap. 10, epilogo), ognuna con la versione senza nome per l'ospite. Altri `%1$s`: carta (`guide_single_instr`) e nome del prossimo passo (`guide_return_scene2_text_one`); `%1$d` numeri. Il Maestro dice il 57% delle battute di storia. Le chiavi entrano in `strings.xml` solo dopo l'approvazione.

## Testi della storia
### Benvenuto alla Scalata (prima apertura)

- `story_welcome_scene1_speaker`: maestro
- `story_welcome_scene1_text`
  - it: Vedi quella targa «Olimpo» sul muro? Segna la nostra sedia, vuota da anni.
  - en: See that «Olympus» plaque on the wall? It marks our chair, empty for years.
- `story_welcome_scene2_speaker`: maestro
- `story_welcome_scene2_text`
  - it: Se nessuno la occupa, il circolo perde il suo posto. E questa sala chiude.
  - en: If nobody sits in it, the club loses its place. And this hall closes.
- `story_welcome_scene3_speaker`: maestro
- `story_welcome_scene3_text`
  - it: Cerco chi la riempia. Un tavolo alla volta, ti porto fino all'Olimpo dei Maestri. Ci stai?
  - en: Looking for someone to fill it. One table at a time, I'll take you to the Olympus of Masters. In?
- `story_welcome_scene3_text_named`
  - it: Cerco chi la riempia. Un tavolo alla volta, ti porto fino all'Olimpo dei Maestri. Ci stai, %1$s?
  - en: Looking for someone to fill it. One table at a time, to the Olympus of Masters. In, %1$s?
- `story_welcome_scene4_speaker`: maestro
- `story_welcome_scene4_text`
  - it: Il taccuino terrà il conto: dieci tavoli, e la prima sera comincia ora.
  - en: The notebook will keep count: ten tables, and the first evening starts now.
- `story_welcome_next_button`
  - it: Ci sto!
  - en: I'm in!

### Cap. 1 — La prima sera

**Circolo:** Tavolino del Bar · **Azione:** Sparring contro Gigi, con i consigli del Maestro (nascosto finché non c'è il segnale della mano d'esordio, F1G-02) · **Avanzamento:** «Ancora 10 tavoli all'Olimpo dei Maestri»
**Indizio:** La prima pagina del taccuino è strappata. · **Gancio:** Gigi vuole la partita vera. · **Cambia:** Gigi: impaziente, si affeziona.

- `story_ch01_scene1_speaker`: maestro
- `story_ch01_scene1_text`
  - it: Prima sera al circolo: si parte dal Tavolino del Bar.
  - en: First evening at the club: we start at the Bar Table.
- `story_ch01_scene2_speaker`: gigi
- `story_ch01_scene2_text`
  - it: Io sono Gigi! Ho imparato ieri, ma ho una gran voglia di giocare.
  - en: I'm Gigi! I only learned yesterday, but I'm dying to play.
- `story_ch01_scene3_speaker`: maestro
- `story_ch01_scene3_text`
  - it: Una partita di Sparring per scaldarsi. Se esiti, chiedimi: ti do un indizio.
  - en: A Sparring game to warm up. If you hesitate, ask me: I'll give you a hint.
- `story_ch01_scene4_speaker`: gigi
- `story_ch01_scene4_text`
  - it: E se sbaglio, fate finta di non vedere, eh?
  - en: And if I slip up, you'll pretend not to see, right?
- `story_ch01_next_text`
  - it: Gioca in Sparring contro Gigi. Se esiti, premi «Chiedi al Maestro».
  - en: Play Sparring against Gigi. If you hesitate, press «Ask the Maestro».
- `story_ch01_next_button`
  - it: Gioca con Gigi
  - en: Play with Gigi
- `story_ch01_close_scene1_speaker`: maestro
- `story_ch01_close_scene1_text`
  - it: Bene così. Scrivo la prima riga nel taccuino.
  - en: Nicely done. I'll write the first line in the notebook.
- `story_ch01_close_scene2_speaker`: maestro
- `story_ch01_close_scene2_text`
  - it: Guarda: la prima pagina è strappata. Chi sarà stato? Un giorno te lo racconto.
  - en: Look: the first page is torn out. Who did it? One day I'll tell you.
- `story_ch01_close_scene3_speaker`: gigi
- `story_ch01_close_scene3_text`
  - it: Ancora! Ma stavolta senza indizi: voglio giocare alla pari.
  - en: Again! But no hints this time: I want to play on equal terms.

### Cap. 2 — La prima partita vera

**Circolo:** Tavolino del Bar · **Azione:** Classica contro Gigi · **Avanzamento:** «Ancora 9 tavoli all'Olimpo dei Maestri»
**Indizio:** Gigi trova un angolo della pagina: «insegna». · **Gancio:** Presto Nonna Rosa, ma prima una prova. · **Cambia:** Gigi: rispetto, diventa tifoso.

- `story_ch02_scene1_speaker`: gigi
- `story_ch02_scene1_text`
  - it: Partita vera, stavolta! Niente indizi, niente retrobottega.
  - en: A real game this time! No hints, no back room.
- `story_ch02_scene2_speaker`: maestro
- `story_ch02_scene2_text`
  - it: Io guardo in silenzio. La partita è tua.
  - en: I'll watch in silence. The game is yours.
- `story_ch02_scene3_speaker`: gigi
- `story_ch02_scene3_text`
  - it: Mi tremano le mani. Ho imparato ieri, ricordi?
  - en: My hands are shaking. I learned yesterday, remember?
- `story_ch02_next_text`
  - it: Gioca una partita Classica contro Gigi e battilo.
  - en: Play a Classic game against Gigi and beat him.
- `story_ch02_next_button`
  - it: Sfida Gigi
  - en: Challenge Gigi
- `story_ch02_close_scene1_speaker`: gigi
- `story_ch02_close_scene1_text`
  - it: Ieri ne sapevo meno. Oggi ho perso meglio! Sei forte, sai?
  - en: Yesterday I knew less. Today I lost better! You're good, you know?
- `story_ch02_close_scene2_speaker`: gigi
- `story_ch02_close_scene2_text`
  - it: Sotto il bancone ho trovato questo pezzo di carta. Non è mio: tieni.
  - en: Under the counter I found this scrap of paper. It isn't mine: here.
- `story_ch02_close_scene3_speaker`: maestro
- `story_ch02_close_scene3_text`
  - it: «Insegna»! Un angolo della pagina. Presto incontrerai Nonna Rosa, ma prima una prova: seguimi.
  - en: «Teaches»! A corner of the page. You'll soon meet Nonna Rosa, but first a test: follow me.

### Cap. 3 — Il Maestro ti mette alla prova

**Circolo:** Tavolino del Bar · **Azione:** Sparring con Check sui Sette · **Avanzamento:** «Ancora 8 tavoli all'Olimpo dei Maestri»
**Indizio:** Il Maestro sfugge sul pezzo di carta. · **Gancio:** Gigi segnala il foglio «R» in bacheca. · **Cambia:** Gigi: tifa dal fondo.

- `story_ch03_scene1_speaker`: maestro
- `story_ch03_scene1_text`
  - it: Nel retrobottega ti faccio una prova di attenzione.
  - en: In the back room I'll give you an attention test.
- `story_ch03_scene2_speaker`: maestro
- `story_ch03_scene2_text`
  - it: I Sette sono preziosi: tieni il conto di quelli usciti.
  - en: The Sevens are precious: keep count of the ones that come out.
- `story_ch03_scene3_speaker`: gigi
- `story_ch03_scene3_text`
  - it: Posso guardare? Prometto di non fiatare!
  - en: Can I watch? I promise not to breathe!
- `story_ch03_scene4_speaker`: maestro
- `story_ch03_scene4_text`
  - it: Sì, Gigi. Ma zitto: ci vuole concentrazione.
  - en: Yes, Gigi. But quiet: this takes concentration.
- `story_ch03_next_text`
  - it: Gioca una partita di Sparring e rispondi alla prova sui Sette usciti.
  - en: Play a Sparring game and answer the test on the Sevens played.
- `story_ch03_next_button`
  - it: Prova dei Sette
  - en: Sevens test
- `story_ch03_close_scene1_speaker`: gigi
- `story_ch03_close_scene1_text`
  - it: Ho contato anch'io! Ne ho trovati tre. O quattro. O forse sette.
  - en: I counted too! I found three. Or four. Or maybe seven.
- `story_ch03_close_scene2_speaker`: maestro
- `story_ch03_close_scene2_text`
  - it: Prova fatta! Scrivo anche questa nel taccuino. Quel pezzo di carta? Mah, sarà un caso.
  - en: Test done! I'll write this one in the notebook too. That scrap of paper? Oh, just chance.
- `story_ch03_close_scene3_speaker`: gigi
- `story_ch03_close_scene3_text`
  - it: Al Circolo Rionale, in bacheca, c'è un foglio firmato «R». Andiamo a vedere?
  - en: At the Neighborhood Club there's a sheet signed «R» on the board. Shall we go look?

### Cap. 4 — Il problema in bacheca

**Circolo:** Circolo Rionale · **Azione:** Sfida del giorno · **Avanzamento:** «Ancora 7 tavoli all'Olimpo dei Maestri»
**Indizio:** Il problema in bacheca è firmato con una «R». · **Gancio:** La firma è di Nonna Rosa. · **Cambia:** Rosa: prima curiosa, poi sfida.

- `story_ch04_scene1_speaker`: maestro
- `story_ch04_scene1_text`
  - it: Il Circolo Rionale! Qui i problemi di carte stanno appesi in bacheca.
  - en: The Neighborhood Club! Card problems are pinned to the board here.
- `story_ch04_scene2_speaker`: gigi
- `story_ch04_scene2_text`
  - it: Eccolo, il foglio firmato «R»! Chissà chi è.
  - en: There it is, the sheet signed «R»! Who could it be?
- `story_ch04_scene3_speaker`: maestro
- `story_ch04_scene3_text`
  - it: Un problema di carte, come promesso. Pensaci con calma: la fretta è nemica delle carte.
  - en: A card problem, as promised. Take your time: haste is the enemy of cards.
- `story_ch04_next_text`
  - it: Apri la Sfida del giorno e trova la mossa giusta.
  - en: Open the Daily puzzle and find the right move.
- `story_ch04_next_button`
  - it: Vai alla Sfida
  - en: Go to the puzzle
- `story_ch04_close_scene1_speaker`: maestro
- `story_ch04_close_scene1_text`
  - it: Hai risolto il problema in bacheca: ragioni meglio di ieri!
  - en: You solved the problem on the board: you reason better than yesterday!
- `story_ch04_close_scene2_speaker`: rosa
- `story_ch04_close_scene2_text`
  - it: Bella testa. La «R» sono io: il mio pianerottolo è libero, quando vuoi.
  - en: Nice mind. The «R» is me: my landing is free whenever you like.
- `story_ch04_close_scene3_speaker`: gigi
- `story_ch04_close_scene3_text`
  - it: Le nonne fanno un po' paura, ma con affetto. Io tifo da qui!
  - en: Grandmas are a little scary, but lovingly so. I'll cheer from here!

### Cap. 5 — Nonna Rosa e il suo pianerottolo

**Circolo:** Circolo Rionale · **Azione:** Classica contro Nonna Rosa · **Avanzamento:** «Ancora 6 tavoli all'Olimpo dei Maestri»
**Indizio:** Rosa rivela che il taccuino passa da maestro ad allievo; dà il pezzo «Chi». · **Gancio:** Il Maestro non ha detto tutto: dalla lavagna. · **Cambia:** Rosa: confidenza, racconta il segreto.

- `story_ch05_scene1_speaker`: rosa
- `story_ch05_scene1_text`
  - it: Questo è il mio pianerottolo, anche qui: una sedia, un tavolino, carte consumate.
  - en: This is my landing, even here: a chair, a little table, worn cards.
- `story_ch05_scene2_speaker`: maestro
- `story_ch05_scene2_text`
  - it: Nonna Rosa gioca da una vita. Guarda bene come scarta.
  - en: Nonna Rosa has played all her life. Watch how she discards.
- `story_ch05_scene3_speaker`: rosa
- `story_ch05_scene3_text`
  - it: Niente occhiolini da dietro la sedia, Maestro: ti vedo.
  - en: No winking from behind the chair, Maestro: I can see you.
- `story_ch05_scene4_speaker`: maestro
- `story_ch05_scene4_text`
  - it: Io? Ero solo affacciato.
  - en: Me? I was just leaning over.
- `story_ch05_next_text`
  - it: Gioca una partita Classica contro Nonna Rosa e battila.
  - en: Play a Classic game against Nonna Rosa and beat her.
- `story_ch05_next_button`
  - it: Sfida Rosa
  - en: Challenge Rosa
- `story_ch05_close_scene1_speaker`: rosa
- `story_ch05_close_scene1_text`
  - it: Ben fatto! Ti dico un segreto: quel taccuino passa da maestro ad allievo.
  - en: Well done! A secret: that notebook goes from master to pupil.
- `story_ch05_close_scene2_speaker`: rosa
- `story_ch05_close_scene2_text`
  - it: Il mio pezzo: «Chi». È la tua grafia, Maestro: me lo affidasti tu, tanto tempo fa.
  - en: My piece: «Whoever». It's your handwriting, Maestro: you entrusted it to me long ago.
- `story_ch05_close_scene3_speaker`: maestro
- `story_ch05_close_scene3_text`
  - it: Volevo dirtelo io! Rosa non sa tenere un segreto. Basi, adesso: alla lavagna.
  - en: I meant to tell you! Rosa can't keep a secret. Basics now: to the blackboard.

### Cap. 6 — Le regole del circolo

**Circolo:** Circolo Rionale · **Azione:** Accademia, capitolo 1 · **Avanzamento:** «Ancora 5 tavoli all'Olimpo dei Maestri»
**Indizio:** Il taccuino si consegna solo a chi conosce le basi. · **Gancio:** Una pagina col tuo nome ti aspetta. · **Cambia:** Rosa: tifa per te.

- `story_ch06_scene1_speaker`: maestro
- `story_ch06_scene1_text`
  - it: La lavagna del circolo. Poche regole ben capite valgono più di mille trucchi.
  - en: The club blackboard. A few well-understood rules beat a thousand tricks.
- `story_ch06_scene2_speaker`: rosa
- `story_ch06_scene2_text`
  - it: Io le so a memoria. Ma ripassarle fa sempre bene.
  - en: I know them by heart. But reviewing them never hurts.
- `story_ch06_scene3_speaker`: maestro
- `story_ch06_scene3_text`
  - it: Il taccuino va a chi conosce le basi. Ripassiamole insieme.
  - en: The notebook goes to those who know the basics. Let's review them together.
- `story_ch06_next_text`
  - it: Apri l'Accademia e completa il capitolo 1.
  - en: Open the Academy and complete chapter 1.
- `story_ch06_next_button`
  - it: Vai all'Accademia
  - en: Go to the Academy
- `story_ch06_close_scene1_speaker`: maestro
- `story_ch06_close_scene1_text`
  - it: Capitolo 1 fatto: le basi sono tue, e nel taccuino.
  - en: Chapter 1 done: the basics are yours, and in the notebook.
- `story_ch06_close_scene2_speaker`: rosa
- `story_ch06_close_scene2_text`
  - it: Adesso sì che puoi salire in Salotto. Fai bella figura!
  - en: Now you can go up to the Lounge. Make a good impression!
- `story_ch06_close_scene3_speaker`: maestro
- `story_ch06_close_scene3_text`
  - it: Lassù c'è una pagina del taccuino con il tuo nome. Vuoi leggerla?
  - en: Up there a notebook page has your name on it. Want to read it?

### Cap. 7 — Il taccuino del Maestro

**Circolo:** Salotto Nobile · **Azione:** Rivedere l'ultima partita (Rivedi analisi nel Profilo); capitolo nascosto finché non c'è il segnale, saltato se non c'è una partita · **Avanzamento:** «Ancora 4 tavoli all'Olimpo dei Maestri»
**Indizio:** Il Maestro ammette di aver perso un tavolo importante, senza dire quale. · **Gancio:** Tonino si fa avanti. · **Cambia:** Il Maestro: si confida a metà.

- `story_ch07_scene1_speaker`: maestro
- `story_ch07_scene1_text`
  - it: Il Salotto Nobile: tavoli di velluto e voci basse. Qui si rilegge.
  - en: The Noble Lounge: velvet tables and hushed voices. Here we reread.
- `story_ch07_scene2_speaker`: tonino
- `story_ch07_scene2_text`
  - it: Silenzio, che qui si pensa! Io, per esempio, penso a come vincere.
  - en: Quiet, we're thinking here! Me, for one, I'm thinking about how to win.
- `story_ch07_scene3_speaker`: maestro
- `story_ch07_scene3_text`
  - it: Ecco il taccuino: ha una pagina col tuo nome. Rileggiamo l'ultima partita.
  - en: Here's the notebook: a page with your name. Let's reread your last game.
- `story_ch07_next_text`
  - it: Apri il Profilo e premi «Rivedi analisi» sull'ultima partita.
  - en: Open Profile and press «Review analysis» on your last game.
- `story_ch07_next_button`
  - it: Rivedi partita
  - en: Review a game
- `story_ch07_close_scene1_speaker`: maestro
- `story_ch07_close_scene1_text`
  - it: Hai riletto la partita a testa alta: ogni mossa è una riga che impari.
  - en: You reread the game head held high: every move is a line you learn.
- `story_ch07_close_scene2_speaker`: maestro
- `story_ch07_close_scene2_text`
  - it: Anch'io ho perso un tavolo importante, una volta. Prima o poi te lo racconto.
  - en: I lost an important table once too. Sooner or later I'll tell you.
- `story_ch07_close_scene3_speaker`: tonino
- `story_ch07_close_scene3_text`
  - it: Parlate di perdere? Io non so cos'è! Vieni, siediti qui.
  - en: Talking about losing? I don't know what that is! Come on, sit here.

### Cap. 8 — Tonino

**Circolo:** Salotto Nobile · **Azione:** Classica contro Tonino · **Avanzamento:** «Ancora 3 tavoli all'Olimpo dei Maestri»
**Indizio:** Tonino ha il terzo pezzo: «impara». · **Gancio:** La frase è quasi completa. · **Cambia:** Tonino: alleanza, orgoglio addolcito.

- `story_ch08_scene1_speaker`: tonino
- `story_ch08_scene1_text`
  - it: Qui nessuno mi batte! Neanche in Salotto. Il bar l'ho portato con me.
  - en: Nobody beats me here! Not even in the Lounge. I brought the bar along.
- `story_ch08_scene2_speaker`: maestro
- `story_ch08_scene2_text`
  - it: Tonino è il campione del bar. Ha un'idea fissa: non perdere mai.
  - en: Tonino is the bar champion. He has one fixed idea: never lose.
- `story_ch08_scene3_speaker`: tonino
- `story_ch08_scene3_text`
  - it: Ho una fama da difendere! E una tazzina da finire.
  - en: I have a reputation to defend! And a little cup to finish.
- `story_ch08_scene4_speaker`: maestro
- `story_ch08_scene4_text`
  - it: Mostragli cosa hai fatto: i Sette, il problema, le basi, la partita.
  - en: Show him what you've done: the Sevens, the puzzle, the basics, the game.
- `story_ch08_next_text`
  - it: Gioca una partita Classica contro Tonino e battilo.
  - en: Play a Classic game against Tonino and beat him.
- `story_ch08_next_button`
  - it: Sfida Tonino
  - en: Challenge Tonino
- `story_ch08_close_scene1_speaker`: tonino
- `story_ch08_close_scene1_text`
  - it: Mi hai battuto! Il caffè lo offro io. La fama me la tengo lo stesso.
  - en: You beat me! The coffee's on me. I'm keeping my reputation anyway.
- `story_ch08_close_scene2_speaker`: tonino
- `story_ch08_close_scene2_text`
  - it: Questo stava nel cassetto del bancone: «impara». Serve a qualcuno?
  - en: This was in the counter drawer: «learns». Does anyone need it?
- `story_ch08_close_scene3_speaker`: maestro
- `story_ch08_close_scene3_text`
  - it: Annoto: «Chi... insegna... impara...». Manca un pezzo, e credo di sapere dove.
  - en: Noting it down: «Whoever... teaches... learns...». One piece is missing, and I think I know where.

### Cap. 9 — La scienza della primiera

**Circolo:** Gran Casinò · **Azione:** Accademia capitolo 2, poi Sparring con Check Primiera · **Avanzamento:** «Ancora 2 tavoli all'Olimpo dei Maestri»
**Indizio:** Il Maestro entra al Casinò a malincuore. · **Gancio:** La confessione: Don Alfonso lo ha battuto. · **Cambia:** Il Maestro: si apre e chiede aiuto.

- `story_ch09_scene1_speaker`: maestro
- `story_ch09_scene1_text`
  - it: Gran Casinò: luci alte, mani veloci. Qui ogni punto pesa.
  - en: Grand Casino: bright lights, quick hands. Every point counts here.
- `story_ch09_scene2_speaker`: alfonso
- `story_ch09_scene2_text`
  - it: Mh.
  - en: Hm.
- `story_ch09_scene3_speaker`: maestro
- `story_ch09_scene3_text`
  - it: Quello è Don Alfonso: parla pochissimo. Prima di sederti, impariamo la Primiera.
  - en: That's Don Alfonso: he speaks very little. Before you sit down, let's learn Primiera.
- `story_ch09_next_text`
  - it: Apri l'Accademia e completa il capitolo 2.
  - en: Open the Academy and complete chapter 2.
- `story_ch09_next_button`
  - it: Vai all'Accademia
  - en: Go to the Academy
- `story_ch09_mid_scene1_speaker`: maestro
- `story_ch09_mid_scene1_text`
  - it: Lezione fatta. Adesso la prova: tocca a te.
  - en: Lesson done. Now the test: your turn.
- `story_ch09_next2_text`
  - it: Gioca in Sparring: dì chi è in vantaggio alla Primiera.
  - en: Play Sparring: say who is ahead in Primiera.
- `story_ch09_next2_button`
  - it: Prova Primiera
  - en: Primiera test
- `story_ch09_close_scene1_speaker`: maestro
- `story_ch09_close_scene1_text`
  - it: Lezione e prova fatte. Ti devo una confessione: anni fa Don Alfonso mi batté qui.
  - en: Lesson and test done. I owe you a confession: years ago Don Alfonso beat me here.
- `story_ch09_close_scene2_speaker`: maestro
- `story_ch09_close_scene2_text`
  - it: Da allora la mia sedia è vuota. Mi riporti a sedere?
  - en: Since then my chair has been empty. Will you take me back to it?
- `story_ch09_close_scene3_speaker`: alfonso
- `story_ch09_close_scene3_text`
  - it: Quando vuoi.
  - en: Whenever you're ready.

### Cap. 10 — Don Alfonso, poi il Maestro

**Circolo:** Gran Casinò, poi Olimpo dei Maestri · **Azione:** Classica contro Don Alfonso (passo 1), poi Classica contro il Maestro (passo 2) · **Avanzamento:** «Ancora 1 tavolo all'Olimpo dei Maestri»
**Indizio:** Alfonso consegna l'ultimo pezzo: «due volte». · **Gancio:** La frase completa e il colpo di scena finale. · **Cambia:** Alfonso: rispetto, e parla.

- `story_ch10_scene1_speaker`: alfonso
- `story_ch10_scene1_text`
  - it: Distribuisci.
  - en: Deal.
- `story_ch10_scene2_speaker`: maestro
- `story_ch10_scene2_text`
  - it: Una parola sola: da lui è quasi un abbraccio.
  - en: One word only: from him that's almost a hug.
- `story_ch10_scene3_speaker`: alfonso
- `story_ch10_scene3_text`
  - it: Gioca.
  - en: Play.
- `story_ch10_scene4_speaker`: maestro
- `story_ch10_scene4_text`
  - it: Dietro di lui c'è la scala dell'Olimpo dei Maestri. Una carta alla volta.
  - en: Behind him is the stairway to the Olympus of Masters. One card at a time.
- `story_ch10_next_text`
  - it: Gioca una partita Classica contro Don Alfonso e battilo.
  - en: Play a Classic game against Don Alfonso and beat him.
- `story_ch10_next_button`
  - it: Sfida Alfonso
  - en: Challenge Alfonso
- `story_ch10_mid_scene1_speaker`: alfonso
- `story_ch10_mid_scene1_text`
  - it: Tieni.
  - en: Here.
- `story_ch10_mid_scene2_speaker`: maestro
- `story_ch10_mid_scene2_text`
  - it: «Due volte»! Ora la frase è intera: «Chi insegna impara due volte».
  - en: «Twice»! The sentence is whole: «Whoever teaches learns twice».
- `story_ch10_mid_scene3_speaker`: maestro
- `story_ch10_mid_scene3_text`
  - it: La strappai io dopo aver perso. Vi affidai i pezzi per non rileggerli: credevo fosse una vergogna.
  - en: I tore it out after losing and gave you the pieces so I'd never reread them. I felt ashamed.
- `story_ch10_mid_scene4_speaker`: maestro
- `story_ch10_mid_scene4_text`
  - it: Ti aspetto in cima alla scala. Stavolta nessun indizio.
  - en: I'll wait at the top of the stairs. No hints this time.
- `story_ch10_next2_text`
  - it: Gioca una partita Classica contro il Maestro, all'Olimpo dei Maestri.
  - en: Play a Classic game against the Maestro, at the Olympus of Masters.
- `story_ch10_next2_button`
  - it: Sfida il Maestro
  - en: Challenge Maestro
- `story_ch10_close_scene1_speaker`: maestro
- `story_ch10_close_scene1_text`
  - it: Eccoti. Dove ti volevo.
  - en: There you are. Right where I wanted you.
- `story_ch10_close_scene1_text_named`
  - it: Eccoti, %1$s. Dove ti volevo.
  - en: There you are, %1$s. Right where I wanted you.
- `story_ch10_close_scene2_speaker`: maestro
- `story_ch10_close_scene2_text`
  - it: Mi hai battuto, e ho imparato ancora. Non cercavo un campione: cercavo chi prendesse il taccuino.
  - en: You beat me, and I learned again. I wasn't seeking a champion, but someone to take the notebook.
- `story_ch10_close_scene3_speaker`: gigi
- `story_ch10_close_scene3_text`
  - it: Hai vinto! Aspetta, il Maestro ha qualcosa per te.
  - en: You won! Wait, the Maestro has something for you.

### Epilogo

- `story_epilogue_scene1_speaker`: maestro
- `story_epilogue_scene1_text`
  - it: La targa sul muro ora ha un nome: il tuo.
  - en: The plaque on the wall now has a name: yours.
- `story_epilogue_scene2_speaker`: maestro
- `story_epilogue_scene2_text`
  - it: Tieni il taccuino: dentro c'è tutto ciò che vale la pena sapere, anche le tue cose.
  - en: Take the notebook: it holds everything worth knowing, yours included.
- `story_epilogue_scene3_speaker`: maestro
- `story_epilogue_scene3_text`
  - it: La pagina col tuo nome, ricordi? È l'ultima, ed è bianca. Scrivila tu.
  - en: The page with your name, remember? It's the last one, and it's blank. Write it yourself.
- `story_epilogue_scene3_text_named`
  - it: La pagina col tuo nome, ricordi? È l'ultima, ed è bianca. Scrivila tu, %1$s.
  - en: The page with your name, remember? It's the last one, and it's blank. Write it yourself, %1$s.
- `story_epilogue_scene4_speaker`: gigi
- `story_epilogue_scene4_text`
  - it: Ho imparato ieri... da te! Mi spieghi la Primiera?
  - en: I learned yesterday... from you! Can you explain Primiera?
- `story_epilogue_scene5_speaker`: rosa
- `story_epilogue_scene5_text`
  - it: Il pianerottolo ha sempre un posto per te.
  - en: The landing always has a spot for you.
- `story_epilogue_scene6_speaker`: tonino
- `story_epilogue_scene6_text`
  - it: Al bar il caffè lo offri tu, stavolta!
  - en: At the bar, the coffee's on you this time!
- `story_epilogue_scene7_speaker`: alfonso
- `story_epilogue_scene7_text`
  - it: Bella partita. Siediti con noi.
  - en: Fine game. Sit down with us.
- `story_epilogue_next_text`
  - it: Continua a giocare: Sparring, Accademia e Sfida del giorno restano aperti.
  - en: Keep playing: Sparring, Academy and Daily puzzle stay open.
- `story_epilogue_next_button`
  - it: Al circolo
  - en: To the club

### Indicatore di avanzamento

- `story_progress_many`
  - it: Ancora %1$d tavoli all'Olimpo dei Maestri
  - en: %1$d tables to go to the Olympus of Masters
- `story_progress_one`
  - it: Ancora 1 tavolo all'Olimpo dei Maestri
  - en: 1 table to go to the Olympus of Masters
- `story_progress_done`
  - it: Sei all'Olimpo dei Maestri!
  - en: You're at the Olympus of Masters!

### Casi limite

- `story_ch04_scene3_text_done`
  - it: Il problema di oggi l'hai già risolto! Resta la firma «R»: di chi sarà?
  - en: You've already solved today's problem! The «R» signature remains: whose is it?
- `story_ch04_next_text_done`
  - it: Il problema è già risolto: segno il passo nel taccuino e andiamo avanti.
  - en: The problem is already solved: I'll note the step in the notebook and we move on.
- `story_ch04_next_button_done`
  - it: Avanti
  - en: Onward
- `story_edge_done_elsewhere_text`
  - it: Questo passo l'avevi già fatto: lo segno nel taccuino. Avanti!
  - en: You'd already done this step: I'll note it in the notebook. Onward!
- `story_edge_done_elsewhere_button`
  - it: Avanti
  - en: Onward
- `story_edge_review_empty_text`
  - it: Non c'è ancora una partita da rivedere. Giochiamone una, poi la rileggiamo.
  - en: There's no game to review yet. Let's play one, then we'll reread it.
- `story_edge_review_empty_button`
  - it: Gioca
  - en: Play
- `story_ch04_unavailable_text`
  - it: Il foglio di oggi non c'è ancora: gioca intanto in Sparring, poi torna in bacheca.
  - en: Today's sheet isn't up yet: play Sparring for now, then come back to the board.
- `story_ch04_unavailable_button`
  - it: Gioca
  - en: Play
- `story_ch09_next_text_reverse`
  - it: La prova l'hai già fatta! Apri l'Accademia e completa il capitolo 2.
  - en: You've already taken the test! Open the Academy and complete chapter 2.
- `story_ch09_next_button_reverse`
  - it: Vai all'Accademia
  - en: Go to the Academy
- `story_edge_lost_text`
  - it: Non è andata, ma qualcosa hai imparato. Riprova: il tavolo ti aspetta.
  - en: It didn't go well, but you learned something. Try again: the table is waiting.
- `story_edge_lost_button`
  - it: Riprova
  - en: Try again

## Guida del Maestro (tutorial)

### Mano d'esordio guidata (primo avvio, seme fisso)

Per ogni passo: istruzione (`_instr`), lode se la mossa è giusta (`_ok`), suggerimento gentile se sbaglia (`_wrong`).

- `guide_intro_1_speaker`: maestro
- `guide_intro_1_text`
  - it: Prima sera: una mano facile, solo per capirci. Puoi saltare quando vuoi.
  - en: First evening: an easy hand, just to get acquainted. You can skip any time.
- `guide_skip_button`
  - it: Salta la guida
  - en: Skip the guide

**Passo — Scegli una carta**

- `guide_pick_instr`
  - it: Questa è la tua mano. Trascina una carta sul tavolo per giocarla.
  - en: This is your hand. Drag a card onto the table to play it.
- `guide_pick_ok`
  - it: Ecco: una carta alla volta.
  - en: That's it: one card at a time.
- `guide_pick_wrong`: nessuna (non c'è mossa sbagliata)

**Passo — Presa singola**

- `guide_single_instr`
  - it: Sul tavolo c'è un %1$s e in mano ne hai uno uguale. Trascinalo sul tavolo.
  - en: There's a %1$s on the table and you hold a matching one. Drag it onto the table.
- `guide_single_ok`
  - it: Presa pulita! La carta uguale prende sempre.
  - en: Clean capture! The matching card always takes.
- `guide_single_wrong`
  - it: Quasi! Cerca in mano la carta uguale a quella sul tavolo.
  - en: Almost! Look in your hand for the card that matches the one on the table.

**Passo — Presa per somma**

- `guide_sum_instr`
  - it: Nessuna carta uguale, ma due a terra sommano come la tua. Giocala e, se serve, scegli la presa.
  - en: No matching card, but two on the table add up like yours. Play it and, if needed, pick the capture.
- `guide_sum_ok`
  - it: Somma giusta! Due carte che sommano come la tua si prendono insieme.
  - en: Right sum! Two cards adding up like yours are taken together.
- `guide_sum_wrong`
  - it: Non proprio: cerca due carte a terra che sommate valgano come la tua.
  - en: Not quite: look for two cards on the table that add up to your card.

**Passo — Posare una carta**

- `guide_lay_instr`
  - it: Stavolta non puoi prendere nulla. Niente paura: posa una carta sul tavolo.
  - en: This time you can't take anything. No worries: lay a card on the table.
- `guide_lay_ok`
  - it: Fatto! Quando non prendi, la carta resta sul tavolo.
  - en: Done! When you can't take, the card stays on the table.
- `guide_lay_wrong`: nessuna (non c'è mossa sbagliata)

**Passo — Cos'è una scopa**

- `guide_scopa_instr`
  - it: Se prendi tutte le carte del tavolo fai Scopa: un punto in più. Prova!
  - en: If you take every card on the table you make a Scopa: an extra point. Try it!
- `guide_scopa_ok`
  - it: Scopa! Tavolo pulito, un punto per te.
  - en: Scopa! Table cleared, a point for you.
- `guide_scopa_wrong`
  - it: Peccato, quel tavolo non si pulisce così. Riprova con un'altra carta.
  - en: Too bad, that table can't be cleared that way. Try another card.

**Passo — Il Settebello**

- `guide_sette_instr`
  - it: Quel sette di Denari è il Settebello: un punto tutto suo. Prendilo!
  - en: That seven of Coins is the Settebello: a point all its own. Take it!
- `guide_sette_ok`
  - it: Settebello preso! Un punto, un denaro e un sette per la Primiera.
  - en: Settebello taken! A point, a coin and a seven for Primiera.
- `guide_sette_wrong`
  - it: Il Settebello è a terra, e col tuo sette puoi prenderlo. Riprova!
  - en: The Settebello is on the table, and your seven can take it. Try again!

**Passo — Fine mano**

- `guide_end_instr`
  - it: Mano finita! Ora si contano i punti: te li mostro.
  - en: Hand over! Now we count the points: let me show you.
- `guide_end_ok`
  - it: Ecco come si decide una mano: ora conosci i punti in gioco.
  - en: That's how a hand is decided: now you know the points at stake.
- `guide_end_wrong`: nessuna (non c'è mossa sbagliata)

- `guide_end_1_speaker`: maestro
- `guide_end_1_text`
  - it: Adesso Gigi ti aspetta per una partita di Sparring.
  - en: Now Gigi is waiting for a Sparring game.
- `guide_end_button`
  - it: Vai da Gigi
  - en: Go to Gigi

### Suggerimenti in Home («prossimo passo»)

- `guide_home_new`
  - it: Si comincia! Premi «Gioca» per una mano facile.
  - en: Let's begin! Press «Play» for an easy hand.
- `guide_home_first_win`
  - it: Hai vinto! Premi «Sfida del giorno»: ti aspetta un problema di carte.
  - en: You won! Press «Daily puzzle»: a card problem awaits you.
- `guide_home_chapter_ready`
  - it: Il prossimo tavolo è pronto: apri «La Scalata» e premi il pulsante del capitolo.
  - en: The next table is ready: open «The Climb» and press the chapter button.
- `guide_home_daily_open`
  - it: Il problema di oggi ti aspetta: premi «Sfida del giorno».
  - en: Today's problem awaits: press «Daily puzzle».
- `guide_home_all_done`
  - it: Per oggi hai fatto tutto. Vuoi tenerti in forma? Premi «Gioca» per una mano di Sparring.
  - en: You've done everything today. Want to stay sharp? Press «Play» for a Sparring hand.

### Saluto di rientro (dopo giorni di assenza)

- `guide_return_scene1_speaker`: maestro
- `guide_return_scene1_text`
  - it: Che piacere rivederti! Il taccuino ti ha aspettato, e io pure.
  - en: So good to see you again! The notebook waited for you, and so did I.
- `guide_return_scene2_speaker`: maestro
- `guide_return_scene2_text`
  - it: Mancano %1$d tavoli all'Olimpo dei Maestri. Il prossimo: %2$s.
  - en: %1$d tables to go to the Olympus of Masters. The next one: %2$s.
- `guide_return_scene3_speaker`: maestro
- `guide_return_scene3_text`
  - it: Quando vuoi, apri «La Scalata» e si riparte da lì.
  - en: Whenever you like, open «The Climb» and we pick up from there.
- `guide_return_scene2_text_one`
  - it: Manca un solo tavolo all'Olimpo dei Maestri: %1$s. Ci siamo quasi!
  - en: One table left to the Olympus of Masters: %1$s. Almost there!

### «So già giocare» (salto del tutorial)

- `guide_skip_scene1_speaker`: maestro
- `guide_skip_scene1_text`
  - it: Le carte le sai già maneggiare, vedo! Allora salti la prima sera.
  - en: I see you already know your way around cards! Then you skip the first evening.
- `guide_skip_scene2_speaker`: maestro
- `guide_skip_scene2_text`
  - it: Ti spiego il punto: la nostra sedia all'Olimpo dei Maestri è vuota. Così, il circolo chiude.
  - en: Here's the point: our chair at the Olympus of Masters is empty. Like this, the club closes.
- `guide_skip_scene3_speaker`: maestro
- `guide_skip_scene3_text`
  - it: Un tavolo alla volta ti porto lassù. Si comincia da Gigi, al Tavolino del Bar.
  - en: One table at a time, I'll take you up there. We start with Gigi, at the Bar Table.
- `guide_skip_scene_button`
  - it: Vai da Gigi
  - en: Go to Gigi

## Segnalazioni
1. **Cap. 1:** la mano guidata non esiste (F1G-02). Il passo è Sparring contro Gigi con «Chiedi al Maestro»; il capitolo è nascosto finché non c'è il segnale della mano d'esordio.
2. **Cap. 7:** apre la revisione dell'ultima partita («Rivedi Partita» a fine partita, «Rivedi analisi» nel Profilo). Nascosto finché non c'è il segnale; saltato se non c'è partita (`story_edge_review_empty_*`). Il testo non parla di «errori».
3. **Cap. 4:** Sfida già risolta oggi → varianti `_done` (conta come fatto, da confermare). Sfida non disponibile → `story_ch04_unavailable_*`. La Sfida non conserva lo storico.
4. **Cap. 3 e 9:** i Check contano il giocato, non il superato. Il Check Primiera misura solo «chi è in vantaggio». Cap. 9 con prova fatta prima della lezione → `story_ch09_next_*_reverse`.
5. **Cap. 5:** in Classica non ci sono consigli del Maestro; Rosa lo dice con ironia.
6. **Passo già fatto altrove:** `story_edge_done_elsewhere_*`. **Sconfitta:** `story_edge_lost_*`.
7. Pezzi di pagina, targa, pianerottolo e taccuino sono solo racconto. Nessuna regola nuova. L'inglese di «La Scalata» («The Climb») è una proposta.

## Note per l'implementazione
- **Indicatore:** N = 10 − (capitoli completati). I capitoli nascosti (1 e 7) contano nel totale di 10; un capitolo saltato (cap. 7 senza partite, cap. 1 con «So già giocare» o «Salta la guida») conta come completato. N = 1 usa `story_progress_one`; N = 0 usa `story_progress_done`. Il numero non dipende dall'ordine in cui compaiono i capitoli. Se il totale visibile cambia (capitoli aggiunti o tolti), N = totale − completati.
- **«Salta la guida» e «So già giocare»** segnano il cap. 1 come fatto e portano alla scena `guide_skip_scene` e poi a Gigi.
- **Nome della funzione:** «La Scalata» in Home e nelle frasi `guide_*`.
- **Varianti:** ogni testo `_named` si usa solo se il giocatore ha un nome; per l'ospite si usa la versione base.

## Dipendenze dall'interfaccia (parole da usare, dalle stringhe it)
| Testo della guida | Elemento reale |
| --- | --- |
| «Trascina … sul tavolo» | `game_card_drag_hint` «Trascina sul tavolo» (`game_card_release_to_play`; conferma `game_card_play_confirm`). L'Accademia usa «tocca la carta» (`academy_card_action_hint`). |
| «scegli la presa» | `game_dialog_capture_title` «Scegli la presa» |
| «Chiedi al Maestro» | `game_hint_button` |
| «So già giocare» | `first_launch_skip_button` (esiste). **«Salta la guida»** è un pulsante NUOVO |
| «Gioca» / «Gioca ora» | `home_play` / `home_play_now` |
| «Sfida del giorno» | `home_daily_puzzle_button` |
| «Rivedi analisi» / «Rivedi Partita» | `profile_history_review_button` / `post_match_review_button` |
| «Accademia» | `academy_nav_title` |
| «La Scalata» | nome deciso da Michele; stringa nuova |
Dipendono dall'interfaccia: `guide_pick`, `guide_single`, `guide_sum`, `guide_lay`, `guide_scopa`, `guide_sette`, `guide_end`, tutti i `guide_home_*` e il «prossimo passo» di ogni capitolo. I testi non nominano carte precise: l'app le inserisce con `%1$s`.

## Guida: note
- Raccontata come «la prima sera al circolo», saltabile sempre.
- Si usano solo lezioni del cap. 1 dell'Accademia e regole esistenti (presa per somma e scopa sono regole esistenti, non insegnate dal cap. 1).
- Dove la mossa non può essere sbagliata (scelta, posare, fine mano) non c'è reazione all'errore.


## Revisione e approvazione

- Tre giri di revisione indipendente il 2026-10-31 (ritmo e chiarezza: 7,5 → 8/10; coerenza con funzioni reali, trama e personaggi: 11 → 14 rilievi, tutti applicati). Corretti: istruzioni di «prossimo passo» concrete, forme neutre, grafia dei pezzi e filo del taccuino, inglese più naturale, casi limite (ospite senza nome, Sfida già fatta, niente da rivedere, prove in ordine inverso), parole dei pulsanti reali.
- Voluti da Michele (2026-10-31): una storia più romanzata con obiettivo e posta in gioco (la sedia vuota, la sala che chiude), dialoghi che appaiono in app e un tutorial guidato e saltabile.
- **Approvata** per delega di Michele (D16: «scrivi la bozza, falla controllare, poi auto-approvala»). «The Climb» come traduzione di «La Scalata» è una proposta.

## Titoli e nomi in inglese (revisione indipendente 2026-10-31, approvata per delega, D16)

Revisione critica dei titoli en dei capitoli, del nome della Scalata e delle stringhe en brevi della schermata (non dei dialoghi lunghi). Criteri: naturalezza per un lettore inglese, niente calchi, tono del Maestro (affettuoso, sobrio), coerenza con le stringhe reali dell'app e con i nomi propri. I testi it non sono toccati.

| Cap. | it | en (approvato) |
| --- | --- | --- |
| 1 | La prima sera | The First Evening |
| 2 | La prima partita vera | The First Real Game |
| 3 | Il Maestro ti mette alla prova | The Maestro's Test |
| 4 | Il problema in bacheca | The Puzzle on the Bulletin Board |
| 5 | Nonna Rosa e il suo pianerottolo | Nonna Rosa and Her Stair Landing |
| 6 | Le regole del circolo | The Club Rules |
| 7 | Il taccuino del Maestro | The Maestro's Notebook |
| 8 | Tonino | Tonino |
| 9 | La scienza della primiera | The Science of Primiera |
| 10 | Don Alfonso, poi il Maestro | Don Alfonso, Then the Maestro |

Nome della funzione: «La Scalata» = **The Climb** (confermato); titolo schermata «La Scalata dei Circoli» = **The Climb Through the Clubs**.

Cosa è cambiato e perché (prima → dopo):
- Cap. 3 «The Maestro Tests You» → «The Maestro's Test»: titolo più sobrio, meno minaccioso.
- Cap. 4 «The Problem on the Board» → «The Puzzle on the Bulletin Board»: «board» in un gioco di carte è il tavolo; «bacheca» è la bacheca, e l'app chiama «puzzle» la Sfida del giorno.
- Cap. 5 «Nonna Rosa and Her Front Hall» → «Nonna Rosa and Her Stair Landing»: il pianerottolo non è l'ingresso di casa (errore di senso); coerente con «Stairwell Champion».
- Schermata «Climbing the Circles» → «The Climb Through the Clubs»: «circles» era un calco di «circoli»; il nome della funzione ora coincide con «The Climb».
- Obiettivi (`season_goal_*`): «Beat in Classic: %1$s» → «Beat %1$s in Classic»; «Play a Sparring with the Check» → «Play a Sparring game with the Check»; «Solve the Daily Challenge» → «Solve the Daily puzzle» (come il pulsante reale `home_daily_puzzle_button`).
- Indicatore e rientro: «N tables to go to the Olympus of Masters» → «N more tables to the Olympus of Masters» (e «1 more table…»); «The next one:» → «Next up:».
- Cap. 1, prossimo passo: «Ask the Maestro» → «Ask the Master», come il pulsante reale `game_hint_button`.
- Cap. 3, prossimo passo: «answer the test on the Sevens played» (poco chiaro) → «answer the Sevens test: how many have come out?».
- `guide_skip_scene2_text`: «Like this, the club closes» (calco di «Così») → «As it stands, the club closes».

Invariati dopo la revisione: titoli dei capitoli 1, 2, 6, 7, 8, 9, 10; `story_circle_*`; «The Climb» in Home; pulsanti di fine scena.

Lunghezze: i titoli dei capitoli 4 e 5 hanno 32 caratteri, come l'it del cap. 5 (31); la schermata non limita le righe, quindi a font 1.3 vanno a capo senza troncarsi. Il titolo schermata passa da 20 a 27 caratteri e a capo su telefono stretto con font grande.

Dubbio residuo (fuori perimetro): `game_hint_button` en dice «Ask the Master» mentre il resto dell'app en scrive «Maestro»; da uniformare in un task sull'interfaccia, se Michele vuole.
