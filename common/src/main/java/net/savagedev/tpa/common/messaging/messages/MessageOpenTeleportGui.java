package net.savagedev.tpa.common.messaging.messages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageOpenTeleportGui extends Message {
    public static MessageOpenTeleportGui deserialize(JsonObject object) {
        final UUID targetPlayer = UUID.fromString(object.get("target").getAsString());
        final List<PlayerInfo> players = new ArrayList<>();
        for (JsonElement element : object.get("players").getAsJsonArray()) {
            final JsonObject playerInfoObject = element.getAsJsonObject();
            players.add(new PlayerInfo(
                    playerInfoObject.get("name").getAsString(),
                    UUID.fromString(playerInfoObject.get("uuid").getAsString()),
                    playerInfoObject.get("server").getAsString()
            ));
        }
        return new MessageOpenTeleportGui(targetPlayer, players);
    }

    private final List<PlayerInfo> players;
    private final UUID target;

    public MessageOpenTeleportGui(UUID target, List<PlayerInfo> players) {
        this.target = target;
        this.players = players;
    }

    @Override
    protected JsonObject asJsonObject() {
        final JsonObject object = new JsonObject();

        final JsonArray playersArray = new JsonArray();
        for (PlayerInfo player : this.players) {
            final JsonObject playerInfoObject = new JsonObject();
            playerInfoObject.addProperty("name", player.getName());
            playerInfoObject.addProperty("uuid", player.getUuid().toString());
            playerInfoObject.addProperty("server", player.getServer());
            playersArray.add(playerInfoObject);
        }

        object.addProperty("target", this.target.toString());
        object.add("players", playersArray);
        return object;
    }

    public List<PlayerInfo> getPlayers() {
        return this.players;
    }

    public UUID getTarget() {
        return this.target;
    }
} 