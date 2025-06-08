package net.savagedev.tpa.common.messaging.messages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageOpenTeleportGui extends Message {
    public static MessageOpenTeleportGui deserialize(JsonObject object) {
        final long reqMsb = object.get("req_msb").getAsLong();
        final long reqLsb = object.get("req_lsb").getAsLong();
        UUID requester = new UUID(reqMsb, reqLsb);

        Map<UUID, String> players = new HashMap<>();
        JsonArray arr = object.getAsJsonArray("players");
        for (JsonElement element : arr) {
            JsonObject o = element.getAsJsonObject();
            long msb = o.get("msb").getAsLong();
            long lsb = o.get("lsb").getAsLong();
            String name = o.get("name").getAsString();
            players.put(new UUID(msb, lsb), name);
        }
        return new MessageOpenTeleportGui(requester, players);
    }

    private final UUID requester;
    private final Map<UUID, String> players = new HashMap<>();

    public MessageOpenTeleportGui(UUID requester, Map<UUID, String> players) {
        this.requester = requester;
        if (players != null) {
            this.players.putAll(players);
        }
    }

    public UUID getRequester() {
        return this.requester;
    }

    public Map<UUID, String> getPlayers() {
        return this.players;
    }

    @Override
    protected JsonObject asJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("req_msb", this.requester.getMostSignificantBits());
        object.addProperty("req_lsb", this.requester.getLeastSignificantBits());
        JsonArray arr = new JsonArray();
        for (Map.Entry<UUID, String> entry : this.players.entrySet()) {
            JsonObject o = new JsonObject();
            o.addProperty("msb", entry.getKey().getMostSignificantBits());
            o.addProperty("lsb", entry.getKey().getLeastSignificantBits());
            o.addProperty("name", entry.getValue());
            arr.add(o);
        }
        object.add("players", arr);
        return object;
    }
}
