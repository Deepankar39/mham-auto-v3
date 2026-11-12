package com.mhamauto.media;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Validate references only; this class NEVER downloads URLs or image bytes. */
public final class ExternalImageUrlPolicy {
    private ExternalImageUrlPolicy() { }

    /** Shopify CDN is the only approved sample source for the current CSV import. */
    public static boolean isAllowed(String raw) {
        if (raw == null || raw.isBlank() || !raw.equals(raw.strip())) {
            return false;
        }
        try {
            URI uri = new URI(raw);
            if (!uri.isAbsolute() || !"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null
                    || (uri.getPort() != -1 && uri.getPort() != 443)) {
                return false;
            }
            String host = uri.getHost();
            String path = uri.getRawPath();
            return host != null && "cdn.shopify.com".equals(host.toLowerCase(Locale.ROOT))
                    && path != null && path.startsWith("/") && path.length() > 1;
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
