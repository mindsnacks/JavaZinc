package com.zinc.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        return new GsonBuilder().setPrettyPrinting().serializeNulls().setVersion(1.0).create();
    }
}
