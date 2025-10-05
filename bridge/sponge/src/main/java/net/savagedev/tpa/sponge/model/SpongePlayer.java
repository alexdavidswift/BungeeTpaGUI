package net.savagedev.tpa.sponge.model;

import net.savagedev.tpa.bridge.model.BungeeTpPlayer;
import net.savagedev.tpa.common.messaging.messages.Message;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import java.util.UUID;

public class SpongePlayer implements BungeeTpPlayer {
    private final ServerPlayer player;

    public SpongePlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return this.player.uniqueId();
    }

    @Override
    public String getName() {
        return this.player.name();
    }

    @Override
    public void sendData(Message message) {
        // Not implemented for Sponge yet.
    }

    @Override
    public Object getHandle() {
        return this.player;
    }

    @Override
    public void teleportTo(BungeeTpPlayer target) {
        // Not implemented for Sponge yet.
    }

    @Override
    public void sendMessage(String message) {
        // Not implemented for Sponge yet.
    }
}
