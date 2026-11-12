package com.mhamauto.importer;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/** RFC4180-compatible streaming reader: supports quoted commas/newlines and a UTF-8 BOM. */
public final class ShopifyCsvReader {
    private static final Set<String> REQUIRED = Set.of("Handle","Title","Body (HTML)","Vendor",
        "Variant SKU","Variant Price","Image Src","Image Position","Image Alt Text","Variant Image","Status");
    private ShopifyCsvReader() { }

    public static long forEach(Path file, Consumer<ShopifyRecord> consumer) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true).get().parse(reader)) {
            List<String> headers = parser.getHeaderNames();
            if (headers.isEmpty() || !headers.get(0).replace("\uFEFF", "").equals("Handle")) {
                throw new IOException("Shopify CSV must start with a Handle header (UTF-8/UTF-8-BOM)");
            }
            if (new HashSet<>(headers).size() != headers.size()) throw new IOException("Duplicate CSV column header");
            Set<String> normalizedHeaders = new HashSet<>(headers);
            normalizedHeaders.remove("\uFEFFHandle");
            normalizedHeaders.add("Handle");
            if (!normalizedHeaders.containsAll(REQUIRED)) throw new IOException("CSV misses required Shopify columns: " + REQUIRED);
            long count = 0;
            for (CSVRecord record : parser) {
                if (record.size() != headers.size()) throw new IOException("Column count mismatch at record " + record.getRecordNumber());
                Map<String,String> values = new HashMap<>();
                for (int i=0; i<headers.size(); i++) {
                    String key = headers.get(i);
                    if (i==0 && key.startsWith("\uFEFF")) key = key.substring(1);
                    values.put(key, record.get(i));
                }
                consumer.accept(new ShopifyRecord(record.getRecordNumber(), values));
                count++;
            }
            return count;
        }
    }
}
