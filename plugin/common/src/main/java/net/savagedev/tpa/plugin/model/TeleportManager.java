package net.savagedev.tpa.plugin.model;

import net.savagedev.tpa.common.messaging.messages.MessageRequestTeleport;
import net.savagedev.tpa.common.messaging.messages.MessageRequestTeleport.Type;
import net.savagedev.tpa.plugin.BungeeTpPlatform;
import net.savagedev.tpa.plugin.config.Setting;
import net.savagedev.tpa.plugin.model.player.ProxyPlayer;
import net.savagedev.tpa.plugin.model.request.TeleportRequest;
import net.savagedev.tpa.plugin.model.request.TeleportRequest.Direction;
import net.savagedev.tpa.plugin.model.server.Server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TeleportManager {
    private final Map<UUID, Stack<TeleportRequest>> requestMap = new HashMap<>();

    private final BungeeTpPlatform platform;

    public TeleportManager(BungeeTpPlatform platform) {
        this.platform = platform;
    }

    public void shutdown() {
        // Make sure all players are refunded in the event of a server shutdown.
        for (Stack<TeleportRequest> requestStack : this.requestMap.values()) {
            for (TeleportRequest request : requestStack) {
                request.getSender().deposit(Setting.TELEPORT_COST.asFloat()).join();
            }
        }
        this.platform.getLogger().info("Refunded " + this.requestMap.size() + " player(s).");
        this.requestMap.clear();
    }

    public CompletableFuture<TeleportRequestResponse> acceptRequest(TeleportRequest request) {
        if (request == null) {
            return CompletableFuture.completedFuture(TeleportRequestResponse.SUCCESS);
        }
        if (request.getDirection() == Direction.TO_SENDER) {
            if (request.getReceiver().canBypassDelay()) {
                return this.teleportAsync(request.getReceiver(), request.getSender());
            } else {
                return this.teleportDelayedAsync(request.getReceiver(), request.getSender());
            }
        } else {
            if (request.getSender().canBypassDelay()) {
                return this.teleportAsync(request.getSender(), request.getReceiver());
            } else {
                return this.teleportDelayedAsync(request.getSender(), request.getReceiver());
            }
        }
    }

    private CompletableFuture<TeleportRequestResponse> teleportDelayedAsync(ProxyPlayer<?, ?> player, ProxyPlayer<?, ?> other) {
        final CompletableFuture<TeleportRequestResponse> future = new CompletableFuture<>();
        this.platform.scheduleTaskDelayed(() ->
                        future.complete(this.teleport(player, other)),
                Setting.DELAY.asLong() * 1000L);
        return future;
    }

    public CompletableFuture<TeleportRequestResponse> teleportAsync(ProxyPlayer<?, ?> player, ProxyPlayer<?, ?> other) {
        return CompletableFuture.supplyAsync(() -> this.teleport(player, other));
    }

    private TeleportRequestResponse teleport(ProxyPlayer<?, ?> player, ProxyPlayer<?, ?> other) {
        TeleportRequestResponse success = TeleportRequestResponse.SUCCESS;
        final Server<?> targetServer = other.getCurrentServer();

        final MessageRequestTeleport requestMessage = new MessageRequestTeleport(player.getUniqueId(), other.getUniqueId());
        if (player.getCurrentServer().equals(targetServer)) {
            requestMessage.setType(Type.INSTANT);
        } else {
            requestMessage.setType(Type.ON_JOIN);
            success = player.connect(targetServer) ? TeleportRequestResponse.SUCCESS : TeleportRequestResponse.NOT_WHITELISTED;
        }

        this.platform.getMessenger().sendData(other.getCurrentServer(), requestMessage);
        return success;
    }

    public TeleportRequestResponse pushRequest(TeleportRequest request) {
        this.requestMap.computeIfAbsent(request.getReceiver().getUniqueId(), k -> new Stack<>())
                .push(request);
        return TeleportRequestResponse.SUCCESS;
    }

    public Optional<TeleportRequest> popMostRecentRequest(ProxyPlayer<?, ?> player) {
        final Stack<TeleportRequest> requests = this.requestMap.get(player.getUniqueId());
        if (requests == null || requests.empty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(requests.pop());
    }

    public Optional<TeleportRequest> popRequestBySender(ProxyPlayer<?, ?> player, ProxyPlayer<?, ?> sender) {
        final Stack<TeleportRequest> requests = this.requestMap.get(player.getUniqueId());
        if (requests == null || requests.empty()) {
            return Optional.empty();
        }

        TeleportRequest request = null;
        for (TeleportRequest teleportRequest : requests) {
            if (teleportRequest.getSender().equals(sender)) {
                request = teleportRequest;
                break;
            }
        }

        if (request != null) {
            requests.remove(request);
        }

        return Optional.ofNullable(request);
    }

    public Stack<TeleportRequest> getRequestStack(ProxyPlayer<?, ?> player) {
        return this.requestMap.getOrDefault(player.getUniqueId(), new Stack<>());
    }

    public Collection<TeleportRequest> deleteRequestStack(ProxyPlayer<?, ?> player) {
        final Stack<TeleportRequest> requestStack = this.requestMap.remove(player.getUniqueId());
        return requestStack == null ? Collections.emptySet() : requestStack;
    }

    // For a future "denyall" command.
    public void clearRequestStack(ProxyPlayer<?, ?> player) {
        final Stack<TeleportRequest> requestStack = this.requestMap.get(player.getUniqueId());
        if (requestStack == null) {
            return;
        }
        requestStack.clear();
    }

    public Collection<TeleportRequest> aggregateRequests() {
        final Set<TeleportRequest> aggregatedRequests = new HashSet<>();
        for (Stack<TeleportRequest> requestStack : this.requestMap.values()) {
            aggregatedRequests.addAll(requestStack);
        }
        return aggregatedRequests;
    }

    public enum TeleportRequestResponse {
        SUCCESS,
        NOT_WHITELISTED,
        CANT_AFFORD;

        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
}
