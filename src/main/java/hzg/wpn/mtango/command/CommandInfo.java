package hzg.wpn.mtango.command;

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
    public final String type;
    public final String target;
    public final JsonElement argin;

    private CommandInfo(String type, String target, JsonElement argin) {
        this.type = type;
        this.target = target;
        this.argin = argin;
    }

    public static JsonDeserializer<CommandInfo> jsonDeserializer() {
        return new JsonDeserializer<CommandInfo>() {
            public CommandInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                String type = json.getAsJsonObject().get("type").getAsString();
                String target = json.getAsJsonObject().get("target").getAsString();
                //postpone argin conversion till we know its type
                JsonElement argin = json.getAsJsonObject().get("argin");
                return new CommandInfo(type, target, argin);
            }
        };
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("argin", argin)
                .add("type", type)
                .toString();
    }

    //TODO ...
}
