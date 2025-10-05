package net.savagedev.tpa.common.messaging.messages;

import java.util.UUID;

public class PlayerInfo {
    private final String name;
    private final UUID uuid;
    private final String server;

    public PlayerInfo(String name, UUID uuid, String server) {
        this.name = name;
        this.uuid = uuid;
        this.server = server;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getServer() {
        return this.server;
    }
} 