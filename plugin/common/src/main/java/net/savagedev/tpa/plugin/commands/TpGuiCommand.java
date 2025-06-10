package net.savagedev.tpa.plugin.commands;

import net.savagedev.tpa.common.messaging.messages.MessageOpenTeleportGui;
import net.savagedev.tpa.plugin.BungeeTpPlugin;
import net.savagedev.tpa.plugin.command.BungeeTpCommand;
import net.savagedev.tpa.plugin.model.player.ProxyPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TpGuiCommand implements BungeeTpCommand {
    private final BungeeTpPlugin plugin;

    public TpGuiCommand(BungeeTpPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ProxyPlayer<?, ?> player, String[] args) {
        Map<UUID, String> players = this.plugin.getOnlinePlayers().stream()
                .filter(ProxyPlayer::notHidden)
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .collect(Collectors.toMap(ProxyPlayer::getUniqueId, ProxyPlayer::getName));
        this.plugin.getPlatform().getMessenger().sendData(player.getCurrentServer(),
                new MessageOpenTeleportGui(player.getUniqueId(), players));
    }
}
