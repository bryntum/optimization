package com.optazen.skillmatch.websocket;

import com.optazen.skillmatch.rest.ApiResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/timefold")
@ApplicationScoped
public class TimefoldWebsocket {
    protected static final Logger logger = LoggerFactory.getLogger(TimefoldWebsocket.class);
    private static final ConcurrentHashMap<UUID, Set<Session>> uuidToSessionsMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, String> latestEvents = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TimefoldWebsocket() {
        // Schedule the task to run every 1 second
        scheduler.scheduleAtFixedRate(this::broadcastLatestEvent, 0, 1, TimeUnit.SECONDS);
    }

    @OnOpen
    private void onOpen(Session session) {
        UUID scheduleId;
        try {
            scheduleId = UUID.fromString(session.getRequestParameterMap().get("scheduleId").getFirst());
        } catch (Exception e) {
            logger.error("Invalid session id");
            scheduleId = ApiResource.defaultUUID;
        }
        // Add session to the UUID group
        uuidToSessionsMap.computeIfAbsent(scheduleId, key -> Collections.synchronizedSet(new HashSet<>())).add(session);
        logger.info("New websocket client connected. Total # websocket clients {} for uuid {}", uuidToSessionsMap.get(scheduleId).size(), scheduleId);
    }

    @OnMessage
    private void onMessage(String message) {
        logger.info("Received websocket message {}", message);
    }

    @OnClose
    public void onClose(Session session) {
        removeSession(session);
        logger.info("Websocket session closed");
    }

    private static void removeSession(Session session) {
        uuidToSessionsMap.forEach((uuid, sessions) -> {
            sessions.remove(session);
            // Clean up empty UUID groups
            if (sessions.isEmpty()) {
                uuidToSessionsMap.remove(uuid);
            }
        });
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        removeSession(session);
        logger.info("Websocket error {}", throwable.getMessage());
    }

    public void setLatestEvent(UUID scheduleId, String event) {
        latestEvents.put(scheduleId, event);
    }

    public void broadcast(UUID scheduleId, String message) {
        logger.info("Sending message %s for UUID %s".formatted(message, scheduleId));
        Set<Session> sessions = uuidToSessionsMap.get(scheduleId);
        if(sessions == null) {
            logger.warn("No sessions found for UUID {}", scheduleId);
            return;
        }
        sessions.forEach(s -> {
            s.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    private void broadcastLatestEvent() {
        latestEvents.forEach((uuid, message) -> {
                broadcast(uuid, message);
                latestEvents.remove(uuid);
        });
    }
}
