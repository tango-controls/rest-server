package org.tango.web.server.command;

import com.google.common.base.Objects;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Parameter object for Commands#createCommand
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.10.12
 */
public class CommandInfo {
    public String type;
    public String devname;
    public String target;
    public JsonElement argin;

    public static JsonDeserializer<CommandInfo> jsonDeserializer() {
        return new JsonDeserializer<CommandInfo>() {
            public CommandInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                CommandInfo result = new CommandInfo();
                result.target = json.getAsJsonObject().get("target").getAsString();
                result.devname = json.getAsJsonObject().get("dev").getAsString();
                //postpone argin conversion till we know its type
                result.argin = json.getAsJsonObject().get("argin");
                return result;
            }
        };
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("devname", devname + "/" + target)
                .add("argin", argin)
                .add("type", type)
                .toString();
    }
}
