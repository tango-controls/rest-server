package hzg.wpn.mtango.command;

import com.google.common.base.Objects;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Matches mTango/CommandIn
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.10.12
 */
public class CommandInfo {
    final String type;
    final String target;
    final JsonElement argin;

    final JsonDeserializationContext context;

    private CommandInfo(String type, String target, JsonElement argin, JsonDeserializationContext context) {
        this.type = type;
        this.target = target;
        this.argin = argin;
        this.context = context;
    }

    public <T> T convertArgin(Class<T> toType){
        return context.deserialize(argin,toType);
    }

    public static JsonDeserializer<CommandInfo> jsonDeserializer(){
        return new JsonDeserializer<CommandInfo>() {
            public CommandInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                String type = json.getAsJsonObject().get("type").getAsString();
                String target = json.getAsJsonObject().get("target").getAsString();
                //postpone argin conversion till we know its type
                JsonElement argin = json.getAsJsonObject().get("argin");
                return new CommandInfo(type, target, argin, context);
            }
        };
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("argin", argin)
                .add("target", target)
                .add("type", type)
                .toString();
    }

    //TODO ...
}
