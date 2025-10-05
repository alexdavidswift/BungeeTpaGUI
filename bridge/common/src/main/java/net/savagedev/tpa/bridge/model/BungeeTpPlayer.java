package net.savagedev.tpa.bridge.model;

import net.savagedev.tpa.common.messaging.messages.Message;

import java.io.IOException;
import java.util.UUID;

public interface BungeeTpPlayer {
    UUID getUniqueId();

    String getName();

    void sendData(Message message) throws IOException;

    Object getHandle();

    void teleportTo(BungeeTpPlayer target);

    void sendMessage(String message);
}
