package net.savagedev.tpa.common.messaging.messages;

import com.google.gson.JsonObject;

import java.util.UUID;

public class MessageExecuteTeleport extends Message {
    public static MessageExecuteTeleport deserialize(JsonObject object) {
        return new MessageExecuteTeleport(
                UUID.fromString(object.get("requester").getAsString()),
                object.get("target").getAsString()
        );
    }

    private final UUID requester;
    private final String target;

    public MessageExecuteTeleport(UUID requester, String target) {
        this.requester = requester;
        this.target = target;
    }

    @Override
    protected JsonObject asJsonObject() {
        final JsonObject object = new JsonObject();
        object.addProperty("requester", this.requester.toString());
        object.addProperty("target", this.target);
        return object;
    }

    public UUID getRequester() {
        return this.requester;
    }

    public String getTarget() {
        return this.target;
    }
} 