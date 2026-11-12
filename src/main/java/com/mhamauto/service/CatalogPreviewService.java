package com.mhamauto.service;

import com.mhamauto.model.CatalogPreviewCategory;
import java.util.List;

/** Static preview data only. JDBC-backed catalogue is planned for a later phase. */
public final class CatalogPreviewService {

    private static final List<CatalogPreviewCategory> CATEGORIES = List.of(
        new CatalogPreviewCategory("Engine Components", "Core engine components and supporting parts.", "engine.svg", "POWERTRAIN"),
        new CatalogPreviewCategory("Brake Systems", "Brake discs, pads and related components.", "brakes.svg", "SAFETY"),
        new CatalogPreviewCategory("Suspension & Steering", "Parts supporting control, comfort and handling.", "suspension.svg", "CHASSIS"),
        new CatalogPreviewCategory("Filters & Service", "Maintenance essentials for everyday driving.", "filters.svg", "MAINTENANCE"),
        new CatalogPreviewCategory("Electrical Parts", "Starting, charging and electrical components.", "electrical.svg", "ELECTRICAL"),
        new CatalogPreviewCategory("Cooling Systems", "Cooling components and thermal management.", "cooling.svg", "COOLING")
    );

    public List<CatalogPreviewCategory> getCategories() {
        return CATEGORIES;
    }

    /** Only these two hard-coded audience values are allowed; user text is never echoed into JSP. */
    public Audience parseAudience(String audience) {
        if (audience == null || audience.isBlank() || "retail".equalsIgnoreCase(audience)) {
            return Audience.RETAIL;
        }
        if ("trade".equalsIgnoreCase(audience)) {
            return Audience.TRADE;
        }
        throw new IllegalArgumentException("Unsupported audience selection");
    }

    public enum Audience {
        RETAIL("Retail selection", "Parts for your next drive.",
               "Explore core automotive categories with a storefront designed around everyday owners in Kuwait.",
               "page", "false"),
        TRADE("Trade selection", "Made for the workshop.",
              "A preview of the MHAM AUTO trade experience for garages, mechanics and business buyers. Wholesale functions are planned for later releases.",
              "false", "page");

        private final String label;
        private final String title;
        private final String description;
        private final String retailCurrent;
        private final String tradeCurrent;

        Audience(String label, String title, String description, String retailCurrent, String tradeCurrent) {
            this.label = label;
            this.title = title;
            this.description = description;
            this.retailCurrent = retailCurrent;
            this.tradeCurrent = tradeCurrent;
        }

        public String getLabel() { return label; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getRetailCurrent() { return retailCurrent; }
        public String getTradeCurrent() { return tradeCurrent; }
    }
}
