# Mazzo napoletano base — fonte

Le 40 carte `card_<seme>_<valore>.webp` vengono dal mazzo napoletano tradizionale su Wikimedia Commons
([Category:Naples deck](https://commons.wikimedia.org/wiki/Category:Naples_deck)), file
`01 Asso di denari.jpg` … `40 Dieci di Bastoni.jpg`, autore **Trocche100**, licenza **pubblico dominio**
(`PD-user-it`). Nei file, 8, 9 e 10 sono Fante, Cavallo e Re.

Per rigenerarle: scaricare i 40 file (es. `https://commons.wikimedia.org/wiki/Special:FilePath/<nome>?width=1000`)
in una cartella e lanciare `scripts/prepare_napoletane.py <cartella> app/android/src/main/res/drawable-nodpi`.
