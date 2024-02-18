package net.savagedev.tpa.bridge.listener;

import net.savagedev.tpa.bridge.BungeeTpBridgePlatform;
import net.savagedev.tpa.bridge.model.BungeeTpPlayer;
import net.savagedev.tpa.common.messaging.messages.MessagePlayerInfo;

import java.util.UUID;

public class AbstractConnectionListener {
    private final BungeeTpBridgePlatform platform;

    public AbstractConnectionListener(BungeeTpBridgePlatform platform) {
        this.platform = platform;
    }

    protected void handleJoinEvent(BungeeTpPlayer player) {
        this.platform.delay(() -> this.platform.getVanishProvider().ifPresent(provider ->
                        this.platform.getMessenger().sendData(player, new MessagePlayerInfo(player.getUniqueId(), provider.isVanished(player)))),
                250L);

        final UUID targetId = this.platform.getTpCache().remove(player.getUniqueId());

        if (targetId == null) {
            return;
        }

        final BungeeTpPlayer target = this.platform.getBungeeTpPlayer(targetId);

        if (target == null) {
            player.sendMessage("&c" + this.platform.getOfflineUsername(targetId) + " is no longer online.");
            return;
        }

        this.platform.delay(() -> player.teleportTo(target), 250L);
    }
}
