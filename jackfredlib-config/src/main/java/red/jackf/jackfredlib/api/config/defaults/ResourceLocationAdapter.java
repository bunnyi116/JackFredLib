package red.jackf.jackfredlib.api.config.defaults;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.Marshaller;
import net.minecraft.resources.Identifier;

/**
 * A [de]serializer for Minecraft Resource Locations. This converts ResLocs to and from a string, of the form "namespace:path".
 */
public class ResourceLocationAdapter {
    /**
     * Deserializer for Resource Locations.
     * @param primitive Primitive to deserialize from.
     * @param marshaller Jankson marshaller, ignored.
     * @return A deserialized resource location, if valid.
     * @throws DeserializationException If the JSON was an invalid Resource Location or not a string
     */
    public static Identifier deserializer(JsonPrimitive primitive, Marshaller marshaller) throws DeserializationException {
        if (primitive.getValue() instanceof String str) {
            Identifier result = Identifier.tryParse(str);
            if (result == null) throw new DeserializationException("Invalid Resource Location: " + str);
            return result;
        } else {
            throw new DeserializationException("Resource Location cannot be deserialized from a non-string");
        }
    }

    /**
     * Serializer for Resource Locations. Calls {@link Identifier#toString()}.
     * @param location Resource Location to serialize.
     * @param marshaller Jankson marshaller, ignored.
     * @return A serialized resource location.
     */
    public static JsonElement serializer(Identifier location, Marshaller marshaller) {
        return new JsonPrimitive(location.toString());
    }
}
