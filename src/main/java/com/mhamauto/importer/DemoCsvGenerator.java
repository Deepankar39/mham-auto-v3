package com.mhamauto.importer;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/** Synthesized records for database-load tests, NOT actual spare-part listings or verified data. */
public final class DemoCsvGenerator {
    private DemoCsvGenerator() { }
    public static void generate(Path source, Path destination, int count) throws Exception {
        if (count<1 || count>100_000) throw new IllegalArgumentException("count must be 1..100000");
        if (source.toAbsolutePath().normalize().equals(destination.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("Cannot overwrite source CSV");
        }
        List<String> headers;
        List<List<String>> templates=new ArrayList<>();
        try (Reader reader=Files.newBufferedReader(source,StandardCharsets.UTF_8);
             CSVParser parser=CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader)) {
            headers=new ArrayList<>(parser.getHeaderNames());
            if (headers.size()<20 || !headers.get(0).replace("\uFEFF", "").equals("Handle"))
                throw new IllegalArgumentException("Use the supplied Shopify-format sample");
            for(CSVRecord record:parser) {
                if(record.size()!=headers.size()) throw new IllegalArgumentException("Invalid source CSV record");
                List<String> template=new ArrayList<>();
                record.forEach(template::add);
                templates.add(template);
            }
        }
        if (templates.isEmpty()) throw new IllegalArgumentException("No source products to sample");
        if (headers.get(0).startsWith("\uFEFF")) headers.set(0,"Handle");
        try (Writer writer=Files.newBufferedWriter(destination,StandardCharsets.UTF_8);
             CSVPrinter out=new CSVPrinter(writer,CSVFormat.DEFAULT.builder().setHeader(headers.toArray(String[]::new)).get())) {
            int handleIdx=headers.indexOf("Handle"), titleIdx=headers.indexOf("Title"),skuIdx=headers.indexOf("Variant SKU"), tagsIdx=headers.indexOf("Tags");
            for(int i=1;i<=count;i++) {
                List<String> row=new ArrayList<>(templates.get((i-1)%templates.size()));
                String suffix=String.format("%06d",i);
                row.set(handleIdx,row.get(handleIdx)+"-synthetic-"+suffix);
                row.set(titleIdx,"[SYNTHETIC TEST " + suffix + "] " + row.get(titleIdx));
                row.set(skuIdx,row.get(skuIdx)+"-TEST-"+suffix);
                row.set(tagsIdx,row.get(tagsIdx)+",SYNTHETIC-TEST-DATA");
                out.printRecord(row);
            }
        }
    }
}
