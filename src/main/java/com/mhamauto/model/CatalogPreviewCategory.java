package com.mhamauto.model;

import java.util.Objects;

/** A read-only category for the Phase 1.3 servlet/JSP demonstration. */
public final class CatalogPreviewCategory {
    private final String name;
    private final String description;
    private final String imageFile;
    private final String badge;

    public CatalogPreviewCategory(String name, String description, String imageFile, String badge) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = Objects.requireNonNull(description, "description");
        this.imageFile = Objects.requireNonNull(imageFile, "imageFile");
        this.badge = Objects.requireNonNull(badge, "badge");
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageFile() { return imageFile; }
    public String getBadge() { return badge; }
}
