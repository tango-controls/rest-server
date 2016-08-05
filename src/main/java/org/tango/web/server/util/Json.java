package org.tango.web.server.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.tango.web.server.command.CommandInfo;

/**
 * @author ingvord
 * @since 5/24/14@8:43 PM
 */
public class Json {
    private Json() {
    }

    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(CommandInfo.class, CommandInfo.jsonDeserializer())
            .create();
}
