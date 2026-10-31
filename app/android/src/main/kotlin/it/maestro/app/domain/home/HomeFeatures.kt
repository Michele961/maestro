package it.maestro.app.domain.home

/**
 * Le funzioni che la Home mostra a mano a mano che servono (task F1G-03,
 * `docs/design/testi-primo-avvio-e-home.md` §2). «Gioca» c'è sempre e non è in questo elenco.
 *
 * L'ordine dichiarato è l'ordine in cui il Maestro le presenta quando più d'una si accende insieme.
 */
public enum class HomeFeature(
    /** `true` se il Maestro la presenta con una frase la prima volta che compare. */
    public val hasIntroduction: Boolean = true,
) {
    /** Sfida del giorno: dopo la prima partita vinta. */
    DAILY_PUZZLE,

    /** Partita del giorno: dopo la seconda partita vinta. */
    DAILY_MATCH,

    /** Missioni: dopo la prima Sfida del giorno risolta. */
    MISSIONS,

    /** Accademia: dopo la prima prova (Check) giocata. */
    ACADEMY,

    /** Bottega e Profilo, con i crediti: dopo la prima ricompensa ricevuta. */
    SHOP,

    /** Le tessere dei giochi (Scopa, Briscola): dopo la prima partita terminata. Nessuna frase. */
    GAME_TILES(hasIntroduction = false),

    /** La Scalata (F1G-07): dopo la prima partita terminata. Nessuna frase. */
    SEASON(hasIntroduction = false),
    ;

    public companion object {
        /** Tutte le funzioni: ciò che vede chi ha già giocato. */
        public val ALL: Set<HomeFeature> = entries.toSet()
    }
}

/**
 * Segnali già esistenti nell'app da cui si ricavano gli sblocchi: nessun contatore nuovo.
 *
 * @property matchesPlayed partite terminate (profilo di Maestria).
 * @property matchesWon partite vinte nella cronologia recente.
 * @property checkPlayed almeno una partita di Sparring (dove si giocano i Check) registrata.
 * @property dailyPuzzleSolved la Sfida del giorno è stata risolta almeno una volta.
 * @property rewardReceived almeno un accredito del registro dei crediti, escluso il bonus di benvenuto.
 * @property opponentsDefeated avversari battuti sulla scala.
 * @property bestStreak serie più lunga di giorni di gioco.
 * @property academyLessonsDone lezioni dell'Accademia completate.
 */
public data class HomeSignals(
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val checkPlayed: Boolean = false,
    val dailyPuzzleSolved: Boolean = false,
    val rewardReceived: Boolean = false,
    val opponentsDefeated: Int = 0,
    val bestStreak: Int = 0,
    val academyLessonsDone: Int = 0,
) {
    /** Ha già giocato prima dell'aggiornamento: qualunque traccia di attività. */
    public val hasActivity: Boolean
        get() = matchesPlayed > 0 || matchesWon > 0 || checkPlayed || dailyPuzzleSolved || rewardReceived ||
            opponentsDefeated > 0 || bestStreak > 0 || academyLessonsDone > 0
}

/**
 * Stato «funzioni presentate», salvato in DataStore.
 *
 * @property initialized la prima valutazione è già avvenuta: distingue chi aggiorna l'app con dati esistenti
 *   (vede tutto) da chi la installa ora. Il default è sicuro: chi non ha scritto nulla viene valutato una volta.
 * @property revealed le funzioni mostrate nella Home; una funzione mostrata non si nasconde più.
 * @property introduced le funzioni la cui frase di presentazione è già stata detta.
 */
public data class PresentedFeatures(
    val initialized: Boolean = false,
    val revealed: Set<HomeFeature> = emptySet(),
    val introduced: Set<HomeFeature> = emptySet(),
) {
    /** La prossima frase da dire: la prima funzione mostrata, con frase, non ancora presentata. */
    public val pendingIntroduction: HomeFeature?
        get() = HomeFeature.entries.firstOrNull { it.hasIntroduction && it in revealed && it !in introduced }

    /** Segna come detta la frase di [feature]. */
    public fun withIntroduced(feature: HomeFeature): PresentedFeatures = copy(introduced = introduced + feature)
}

/** Regole di sblocco della Home: dominio puro, senza orologio né I/O. */
public object HomeUnlocks {

    /** Le funzioni che i [signals] sbloccano oggi (senza memoria di ciò che era già stato mostrato). */
    public fun unlocked(signals: HomeSignals): Set<HomeFeature> = buildSet {
        if (signals.matchesWon >= 1) add(HomeFeature.DAILY_PUZZLE)
        if (signals.matchesWon >= 2) add(HomeFeature.DAILY_MATCH)
        if (signals.dailyPuzzleSolved) add(HomeFeature.MISSIONS)
        if (signals.checkPlayed) add(HomeFeature.ACADEMY)
        if (signals.rewardReceived) add(HomeFeature.SHOP)
        if (signals.matchesPlayed >= 1) {
            add(HomeFeature.GAME_TILES)
            add(HomeFeature.SEASON)
        }
    }

    /**
     * Aggiorna lo stato salvato con i [signals] correnti. La prima volta chi ha già giocato vede tutto e non
     * sente nessuna frase di presentazione; chi è nuovo parte da zero. Poi gli sblocchi si aggiungono e restano.
     */
    public fun advance(stored: PresentedFeatures, signals: HomeSignals): PresentedFeatures = when {
        !stored.initialized && signals.hasActivity ->
            PresentedFeatures(initialized = true, revealed = HomeFeature.ALL, introduced = HomeFeature.ALL)
        !stored.initialized -> PresentedFeatures(initialized = true, revealed = unlocked(signals))
        else -> stored.copy(revealed = stored.revealed + unlocked(signals))
    }
}
