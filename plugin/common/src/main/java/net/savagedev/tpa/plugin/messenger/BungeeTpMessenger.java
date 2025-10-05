package net.savagedev.tpa.plugin.messenger;

import com.google.gson.JsonObject;
import net.savagedev.tpa.common.messaging.AbstractMessenger;
import net.savagedev.tpa.common.messaging.messages.Message;
import net.savagedev.tpa.common.messaging.messages.MessageBasicServerInfoResponse;
import net.savagedev.tpa.common.messaging.messages.MessageCurrencyFormatResponse;
import net.savagedev.tpa.common.messaging.messages.MessageEconomyResponse;
import net.savagedev.tpa.common.messaging.messages.MessagePlayerInfo;
import net.savagedev.tpa.common.messaging.messages.MessageExecuteTeleport;
import net.savagedev.tpa.plugin.BungeeTpPlugin;
import net.savagedev.tpa.plugin.command.BungeeTpCommand;
import net.savagedev.tpa.plugin.model.economy.RemoteEconomyResponse;
import net.savagedev.tpa.plugin.model.server.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BungeeTpMessenger<T> extends AbstractMessenger<T> {
    private static final Map<String, Function<JsonObject, Message>> DECODER_FUNCTIONS = new HashMap<>();

    static {
        DECODER_FUNCTIONS.put(MessagePlayerInfo.class.getSimpleName(), MessagePlayerInfo::deserialize);
        DECODER_FUNCTIONS.put(MessageEconomyResponse.class.getSimpleName(), MessageEconomyResponse::deserialize);
        DECODER_FUNCTIONS.put(MessageBasicServerInfoResponse.class.getSimpleName(), MessageBasicServerInfoResponse::deserialize);
        DECODER_FUNCTIONS.put(MessageCurrencyFormatResponse.class.getSimpleName(), MessageCurrencyFormatResponse::deserialize);
        DECODER_FUNCTIONS.put(MessageExecuteTeleport.class.getSimpleName(), MessageExecuteTeleport::deserialize);
    }

    private final Map<String, Consumer<? extends Message>> consumers;
    private final BungeeTpPlugin plugin;

    public BungeeTpMessenger(BungeeTpPlugin plugin) {
        super(DECODER_FUNCTIONS);
        this.plugin = plugin;
        this.consumers = this.initializeConsumers();
    }

    private Map<String, Consumer<? extends Message>> initializeConsumers() {
        final Map<String, Consumer<? extends Message>> consumers = new HashMap<>();
        consumers.put(MessagePlayerInfo.class.getSimpleName(), new PlayerInfoConsumer());
        consumers.put(MessageEconomyResponse.class.getSimpleName(), new EconomyWithdrawResponseConsumer(this.plugin));
        consumers.put(MessageBasicServerInfoResponse.class.getSimpleName(), new ServerInfoConsumer(this.plugin));
        consumers.put(MessageCurrencyFormatResponse.class.getSimpleName(), new CurrencyFormatConsumer(this.plugin));
        consumers.put(MessageExecuteTeleport.class.getSimpleName(), new ExecuteTeleportConsumer(this.plugin));
        return consumers;
    }

    @Override
    public void handleIncomingMessage(Message message) {
        this.getConsumer(message.getClass().getSimpleName()).accept(message);
    }

    @SuppressWarnings("unchecked")
    private <M extends Message> Consumer<M> getConsumer(String messageId) {
        return (Consumer<M>) this.consumers.get(messageId);
    }

    private static final class PlayerInfoConsumer implements Consumer<MessagePlayerInfo> {
        @Override
        public void accept(MessagePlayerInfo message) {
            // This consumer does not require the plugin instance.
        }
    }

    private static final class ExecuteTeleportConsumer implements Consumer<MessageExecuteTeleport> {
        private final BungeeTpPlugin plugin;

        private ExecuteTeleportConsumer(BungeeTpPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void accept(MessageExecuteTeleport message) {
            this.plugin.getPlayer(message.getRequester())
                    .ifPresent(requester -> this.plugin.getPlayer(message.getTarget())
                            .ifPresent(target -> this.plugin.getTeleportManager().teleportAsync(requester, target)));
        }
    }

    private static final class EconomyWithdrawResponseConsumer implements Consumer<MessageEconomyResponse> {
        private final BungeeTpPlugin plugin;

        private EconomyWithdrawResponseConsumer(BungeeTpPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void accept(MessageEconomyResponse message) {
            final CompletableFuture<RemoteEconomyResponse> future = this.plugin.getPlayerManager().removePendingTransaction(message.getUniqueId());
            if (future != null) {
                future.complete(new RemoteEconomyResponse(message.getAmount(), message.getBalance(), message.getFormattedAmount(), message.getFormattedBalance(), message.wasSuccessful()));
            }
        }
    }

    private static final class ServerInfoConsumer implements Consumer<MessageBasicServerInfoResponse> {
        private final BungeeTpPlugin plugin;

        private ServerInfoConsumer(BungeeTpPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void accept(MessageBasicServerInfoResponse message) {
            this.plugin.getServerManager().get(message.getServerId()).ifPresent(server -> {
                server.setServerSoftware(message.getSoftwareName());
                server.setBridgeVersion(message.getBridgeVersion());
                server.setEconomySupport(message.hasEconomySupport());
            });
        }
    }

    private static final class CurrencyFormatConsumer implements Consumer<MessageCurrencyFormatResponse> {
        private final BungeeTpPlugin plugin;

        private CurrencyFormatConsumer(BungeeTpPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void accept(MessageCurrencyFormatResponse message) {
            final CompletableFuture<String> future = this.plugin.getServerManager().removeAwaitingCurrencyFormat(message.getServerId());
            if (future != null) {
                future.complete(message.getFormattedAmount());
            }
        }
    }
}
