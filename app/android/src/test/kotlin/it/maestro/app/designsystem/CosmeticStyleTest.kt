package it.maestro.app.designsystem

import it.maestro.app.designsystem.cosmetics.CosmeticStyle
import it.maestro.app.domain.cosmetics.CosmeticCatalog
import it.maestro.app.domain.cosmetics.CosmeticCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CosmeticStyleTest {

    @Test
    fun `ogni cosmetico del catalogo ha testi e anteprima propri`() {
        CosmeticCatalog.ALL_ITEMS.forEach { item ->
            val texts = CosmeticStyle.textsOf(item)
            assertTrue(texts.nameRes != 0 && texts.descriptionRes != 0 && texts.previewDrawableRes != 0, item.id)
        }
        val names = CosmeticCatalog.ALL_ITEMS.map { CosmeticStyle.textsOf(it).nameRes }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `i cosmetici a pagamento si distinguono dal predefinito`() {
        val defaultTable = CosmeticStyle.tableStyle("felt_classic_green")
        CosmeticCatalog.TABLES.filterNot { it.isDefault }.forEach { item ->
            val style = CosmeticStyle.tableStyle(item.id)
            assertTrue(style.background != defaultTable.background, item.id)
        }
        CosmeticCatalog.CARD_BACKS.filterNot { it.isDefault }.forEach { item ->
            assertNotNull(CosmeticStyle.cardBackStyle(item.id).border, item.id)
        }
        CosmeticCatalog.AVATARS.filterNot { it.isDefault }.forEach { item ->
            assertTrue(CosmeticStyle.avatarDrawable(item.id) != CosmeticStyle.avatarDrawable("avatar_default"), item.id)
        }
    }

    @Test
    fun `un id sconosciuto ricade sul predefinito`() {
        assertNull(CosmeticStyle.cardBackStyle("sconosciuto").tint)
        assertNull(CosmeticStyle.tableStyle("sconosciuto").feltTint)
        assertEquals(CosmeticStyle.avatarDrawable("avatar_default"), CosmeticStyle.avatarDrawable("sconosciuto"))
    }

    @Test
    fun `il catalogo ha prezzi e un predefinito gratuito per categoria`() {
        CosmeticCategory.entries.forEach { category ->
            val items = CosmeticCatalog.ALL_ITEMS.filter { it.category == category }
            assertTrue(items.any { it.isDefault && it.isFree }, category.name)
        }
    }
}
