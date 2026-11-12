package com.mhamauto.catalog;

/** Display short plain-text excerpts; original Shopify HTML is never sent to the browser as markup. */
public final class DescriptionText {
    private DescriptionText() { }
    public static String excerpt(String html) {
        if (html == null || html.isBlank()) return "Description has not yet been verified.";
        String text = html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
             .replaceAll("(?is)<[^>]*>", " ")
             .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<")
             .replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'")
             .replaceAll("\\s+", " ").strip();
        if (text.isEmpty()) return "Description has not yet been verified.";
        return text.length() > 950 ? text.substring(0, 950) + "…" : text;
    }
}
