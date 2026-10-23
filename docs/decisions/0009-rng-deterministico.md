# ADR-0009 — Generatore casuale con seed proprio del progetto

Stato: accettato · 2026-10-23

## Contesto
I replay devono restare identici tra versioni di Kotlin e tra piattaforme. L'algoritmo di `kotlin.random.Random(seed)` non è un contratto del progetto.

## Decisione
`core:game` include un generatore proprio (`SeededRng`, algoritmo SplitMix64) con test a vettori fissi. Il mescolamento usa Fisher–Yates su `SeededRng`. Bot e analizzatore ricevono il proprio `SeededRng` derivato dal seed.

## Dettagli vincolanti

- SplitMix64 di riferimento (Vigna): `state += 0x9E3779B97F4A7C15`; `z = state`; `z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9`; `z = (z xor (z ushr 27)) * 0x94D049BB133111EB`; risultato `z xor (z ushr 31)`.
- `nextInt(bound)`: `x = nextLong() ushr 1`; si scarta e si ripete finché `x >= limite`, con `limite = 2^63 − (2^63 mod bound)`; risultato `x mod bound`.
- `fork()`: restituisce `SeededRng(nextLong())`.
- Fisher–Yates: per `i` da `n − 1` a `1`, `j = nextInt(i + 1)`, scambia `i` e `j`.
- Ordine di consumo in una partita: (1) `nextInt(2)` sceglie il primo mazziere (0 = P1); (2) mescolamento della prima smazzata; (3) eventuali rimescolamenti per la regola dei tre Re; (4) mescolamenti delle smazzate successive, in ordine.
- Lo stato del generatore fa parte di `ScopaState` e **non** compare mai in `ScopaView`.

## Conseguenze
Vietato usare `Random` senza seed o fonti di tempo nella logica. Cambiare l'algoritmo significa incrementare `RULES_VERSION`.
