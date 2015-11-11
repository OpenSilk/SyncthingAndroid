/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by drew on 11/10/15.
 */
public class VersioningTypeConverter implements JsonSerializer<Versioning>, JsonDeserializer<Versioning> {

    @Override
    public Versioning deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Element was not an object");
        }
        JsonObject obj = json.getAsJsonObject();
        VersioningType type = context.deserialize(obj.get("type"), VersioningType.class);
        JsonElement params = obj.get("params");
        if (type == null) {
            type = VersioningType.NONE;
        }
        switch (type) {
            case EXTERNAL:
                return new VersioningExternal(type, context.deserialize(params, VersioningExternal.Params.class));
            case SIMPLE:
                return new VersioningSimple(type, context.deserialize(params, VersioningSimple.Params.class));
            case STAGGERED:
                return new VersioningStaggered(type, context.deserialize(params, VersioningStaggered.Params.class));
            case TRASHCAN:
                return new VersioningTrashCan(type, context.deserialize(params, VersioningTrashCan.Params.class));
            case NONE:
            default:
                return new VersioningNone(type);
        }
    }

    @Override
    public JsonElement serialize(Versioning src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        JsonElement type = context.serialize(src.type, VersioningType.class);
        obj.add("type", type);
        JsonElement params;
        switch (src.type) {
            case EXTERNAL:
                params = context.serialize(src.params, VersioningExternal.Params.class);
                break;
            case SIMPLE:
                params = context.serialize(src.params, VersioningSimple.Params.class);
                break;
            case STAGGERED:
                params = context.serialize(src.params, VersioningStaggered.Params.class);
                break;
            case TRASHCAN:
                params = context.serialize(src.params, VersioningTrashCan.Params.class);
                break;
            case NONE:
            default:
                params = null;//context.serialize(src.params, VersioningNone.Params.class);
                break;
        }
        obj.add("params", params);
        return obj;
    }
}
