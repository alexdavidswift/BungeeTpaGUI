package net.savagedev.tpa.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.savagedev.tpa.common.messaging.messages.MessageOpenTeleportGui;
import net.savagedev.tpa.common.messaging.messages.PlayerInfo;
import net.savagedev.tpa.velocity.BungeeTpVelocityPlugin;

import java.util.stream.Collectors;

public class TpaGuiCommand implements SimpleCommand {
    private final BungeeTpVelocityPlugin plugin;

    public TpaGuiCommand(BungeeTpVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            return;
        }

        final Player player = (Player) invocation.source();

        this.plugin.getPlugin().getPlayerManager().get(player.getUniqueId())
                .ifPresent(proxyPlayer -> this.plugin.getPlugin().getPlatform().getMessenger().sendData(
                        proxyPlayer.getCurrentServer(),
                        new MessageOpenTeleportGui(
                                player.getUniqueId(),
                                this.plugin.getServer().getAllPlayers().stream()
                                        .map(p -> new PlayerInfo(
                                                p.getUsername(),
                                                p.getUniqueId(),
                                                p.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown")
                                        ))
                                        .collect(Collectors.toList())
                        )
                ));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("bungeetpa.gui");
    }
} 