package com.mhamauto.catalog;
import com.mhamauto.catalog.CatalogModels.Card;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class CatalogQueryTest {
    @Test void boundsAndSearchSafety() {
        assertThrows(IllegalArgumentException.class,()->CatalogQuery.parse("test",null,null,"foo",null));
        assertThrows(IllegalArgumentException.class,()->CatalogQuery.parse("test",null,null,null,"99999"));
        assertThrows(IllegalArgumentException.class,()->CatalogQuery.parse("test","0",null,null,null));
        assertEquals("+wheel* +hub*",CatalogQuery.parse("wheel hub",null,null,null,null).fullTextTerms());
    }
    @Test void preventsInventedCurrency() {
        assertEquals("Price pending verification",new Card(1,"x","x","v","c","s",null,null).getPriceLabel());
        assertEquals("KWD 8.500",new Card(1,"x","x","v","c","s",new BigDecimal("8.500"),null).getPriceLabel());
    }
    @Test void safeDescriptionAndUrl() {
        assertEquals("Hello world",DescriptionText.excerpt("<p>Hello <b>world</b></p>"));
        assertEquals("V!_%",CatalogQuery.parse("V_",null,null,null,null).skuPrefix());
        assertTrue(CatalogQuery.parse("test",null,null,null,"2").getPreviousHref().contains("page=1"));
    }
}
