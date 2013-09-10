package com.zinc.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zinc.classes.data.SourceURL;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class ZincBaseTest {
    @Before
    public void setUpMocks() {
        MockitoAnnotations.initMocks(this);
    }

    protected Gson createGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().setVersion(1.0);

        gsonBuilder.registerTypeAdapter(SourceURL.class, new SourceURL.Serializer());
        gsonBuilder.registerTypeAdapter(SourceURL.class, new SourceURL.Deserializer());

        return gsonBuilder.create();
    }
}
