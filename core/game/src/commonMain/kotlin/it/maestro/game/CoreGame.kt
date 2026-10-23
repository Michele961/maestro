package it.maestro.game

/**
 * Modulo `core:game`: regole, stato e log delle partite.
 *
 * Responsabilità e vincoli: vedi `core/game/README.md` e `docs/architecture.md`.
 * Il modello di dominio viene creato dal task F0-03. Non aggiungere tipi qui fuori da un task.
 */
public object CoreGame {
    /** Versione delle regole serializzata nei log; va incrementata a ogni cambio di regole (ADR-0003). */
    public const val RULES_VERSION: Int = 0
}
