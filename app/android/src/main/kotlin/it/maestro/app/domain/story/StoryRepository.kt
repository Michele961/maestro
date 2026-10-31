package it.maestro.app.domain.story

import kotlinx.coroutines.flow.Flow

/** Le scene già viste (F1G-16a): una scena compare una sola volta. Senza dati non ne è vista nessuna. */
public interface StoryRepository {
    /** Gli [StoryScene.id] delle scene viste (o saltate). */
    public fun observeSeen(): Flow<Set<String>>

    /** Segna vista la scena [sceneId]; chiamarla di nuovo non cambia nulla. */
    public suspend fun markSeen(sceneId: String)
}
