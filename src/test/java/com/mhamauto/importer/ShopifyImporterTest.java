package com.mhamauto.importer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ShopifyImporterTest {
    @TempDir Path temp;
    private final Path sample=Path.of("samples","product data sample.csv");

    @Test void fourRealSampleRowsAndRawSkuArePreserved() throws Exception {
        List<ShopifyRecord> rows=new ArrayList<>();
        long count=ShopifyCsvReader.forEach(sample,rows::add);
        assertEquals(4,count);
        assertEquals(4,rows.size());
        assertEquals("'8113206",rows.get(1).sku());
        assertEquals("'480022",rows.get(2).sku());
        assertTrue(rows.get(0).trim("Image Src").startsWith("https://cdn.shopify.com/"));
        assertNull(rows.get(0).kwdPrice("UNKNOWN"));
        assertEquals("69",rows.get(0).kwdPrice("KWD").toPlainString());
    }

    @Test void synthesisCreatesFiveHundredUniqueHandlesAndSkus() throws Exception {
        Path generated=temp.resolve("demo500.csv");
        DemoCsvGenerator.generate(sample,generated,500);
        List<String> handles=new ArrayList<>(),skus=new ArrayList<>();
        assertEquals(500,ShopifyCsvReader.forEach(generated,r -> {
            r.validate("UNKNOWN");
            handles.add(r.handle()); skus.add(r.sku());
            assertTrue(r.title().startsWith("[SYNTHETIC TEST"));
        }));
        assertEquals(500,handles.stream().distinct().count());
        assertEquals(500,skus.stream().distinct().count());
    }

    @Test void handlesMultilineQuotedDescription() throws Exception {
        Path f=temp.resolve("multiline.csv");
        String header="Handle,Title,Body (HTML),Vendor,Variant SKU,Variant Price,Image Src,Image Position,Image Alt Text,Variant Image,Status\n";
        Files.writeString(f,header+"part-x,Part X,\"<p>Line 1, comma\nLine 2</p>\",BRAND,X-1,2.001,,,,,active\n",StandardCharsets.UTF_8);
        List<ShopifyRecord> rows=new ArrayList<>();
        assertEquals(1,ShopifyCsvReader.forEach(f,rows::add));
        assertTrue(rows.get(0).field("Body (HTML)").contains("\nLine 2"));
    }

    @Test void invalidNumbersAndUnapprovedCurrencyRejected() throws Exception {
        ShopifyRecord r=new ShopifyRecord(1,Map.of("Handle","x","Variant SKU","x-1","Variant Price","3.9999"));
        assertThrows(IllegalArgumentException.class,()->r.validate("KWD"));
        assertEquals(4,CatalogImporter.dryRun(sample,"UNKNOWN").successful());
    }
}
