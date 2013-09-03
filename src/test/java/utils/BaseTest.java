package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class BaseTest {
    protected Gson createGson() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().setVersion(1.0).create();
    }
}
