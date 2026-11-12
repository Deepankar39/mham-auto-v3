package com.mhamauto.admin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

class AdminRulesTest {
    @Test void strictPageAndId(){assertEquals(1,AdminRules.page(null));assertEquals(50,AdminRules.page("50"));assertThrows(IllegalArgumentException.class,()->AdminRules.id("0"));assertThrows(IllegalArgumentException.class,()->AdminRules.page("999999"));}
    @Test void handleAndSku(){assertEquals("brake-pad-1",AdminRules.handle("BRAKE-PAD-1"));assertEquals("'480022",AdminRules.sku("'480022"));assertThrows(IllegalArgumentException.class,()->AdminRules.handle("ab/bad"));}
    @Test void samplePriceAndStock(){assertEquals(new BigDecimal("1.230"),AdminRules.demoPrice("1.23"));assertNull(AdminRules.demoPrice(""));assertNull(AdminRules.stock(""));assertEquals(Integer.valueOf(0),AdminRules.stock("0"));assertThrows(IllegalArgumentException.class,()->AdminRules.demoPrice("1.2345"));assertThrows(IllegalArgumentException.class,()->AdminRules.stock("-1"));}
    @Test void statusIsRestricted(){assertEquals("DEMO_REVIEWED",AdminRules.nextOrderStatus("DEMO_PLACED","DEMO_REVIEWED"));assertThrows(IllegalArgumentException.class,()->AdminRules.nextOrderStatus("DEMO_CANCELLED","DEMO_PLACED"));assertThrows(IllegalArgumentException.class,()->AdminRules.nextOrderStatus("DEMO_REVIEWED","DEMO_CANCELLED"));}
    @Test void descriptionEscapesMarkup(){String html=AdminRules.escapedDescription("<script>\n& hello");assertFalse(html.contains("<script>"));assertTrue(html.contains("&lt;script&gt;"));assertTrue(html.contains("&amp; hello"));}
}
