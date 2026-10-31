package it.maestro.app.domain.home

/** Persistenza dello stato «funzioni presentate» della Home (task F1G-03). */
public interface HomeFeaturesRepository {
    /** Lo stato salvato; senza dati è [PresentedFeatures] di default. */
    public suspend fun load(): PresentedFeatures

    /** Salva [state] sostituendo il precedente. */
    public suspend fun save(state: PresentedFeatures)
}
