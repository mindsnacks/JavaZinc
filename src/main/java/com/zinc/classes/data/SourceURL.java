package com.zinc.classes.data;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/9/13
 */
public class SourceURL {
    private static final String CATALOG_FILENAME = "catalog.json";
    private static final String ARCHIVES_FOLDER = "archives";
    private static final String MANIFESTS_FOLDER = "manifests";
    private static final String FLAVOR_SEPARATOR = "~";
    private static final String ARCHIVES_FORMAT = "tar";
    private static final String MANIFESTS_FORMAT = "json";

    private final URL mUrl;
    private transient final String mCatalogID;

    public SourceURL(final URL host, final String catalogID) throws MalformedURLException {
        mUrl = new URL(host, catalogID + "/");
        mCatalogID = catalogID;
    }

    public SourceURL(final URL sourceURL) throws MalformedURLException {
        mUrl = sourceURL;
        mCatalogID = extractCatalogID(sourceURL);
    }

    public String getCatalogID() {
        return mCatalogID;
    }

    public URL getUrl() {
        return mUrl;
    }

    public URL getCatalogFileURL() throws MalformedURLException {
        return new URL(getUrl(), CATALOG_FILENAME);
    }

    public URL getArchiveURL(final String bundleName, final int version, final String flavorName) throws MalformedURLException {
        return new URL(getUrl(), String.format("%s/%s-%d%s%s.%s", ARCHIVES_FOLDER, bundleName, version, FLAVOR_SEPARATOR, flavorName, ARCHIVES_FORMAT));
    }

    public URL getManifestFileURL(final String bundleName, final int version) throws MalformedURLException {
        return new URL(getUrl(), String.format("%s/%s-%d.%s", MANIFESTS_FOLDER, bundleName, version, MANIFESTS_FORMAT));
    }

    @Override
    public String toString() {
        return getUrl().toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return mUrl.equals(((SourceURL) o).getUrl());
    }

    @Override
    public int hashCode() {
        return mUrl.hashCode();
    }

    private static String extractCatalogID(final URL sourceURL) {
        final String path = sourceURL.getPath().replaceAll("/$", "");
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static class Serializer implements JsonSerializer<SourceURL> {
        @Override
        public JsonElement serialize(final SourceURL sourceURL,
                                     final Type type,
                                     final JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(sourceURL.getUrl().toString());
        }
    }

    public static class Deserializer implements JsonDeserializer<SourceURL> {
        @Override
        public SourceURL deserialize(final JsonElement jsonElement,
                                     final Type type,
                                     final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            final String url = jsonElement.getAsJsonPrimitive().getAsString();

            try {
                return new SourceURL(new URL(url));
            } catch (MalformedURLException e) {
                throw new JsonParseException("Invalid url: " + url, e);
            }
        }
    }
}
