package com.optazen.skillmatch.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@ServerEndpoint("/timefold")
@ApplicationScoped
public class TimefoldWebsocket {
    protected static final Logger logger = LoggerFactory.getLogger(TimefoldWebsocket.class);

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    private final AtomicReference<String> latestEvent = new AtomicReference<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TimefoldWebsocket() {
        // Schedule the task to run every 1 second
        scheduler.scheduleAtFixedRate(this::broadcastLatestEvent, 0, 1, TimeUnit.SECONDS);
    }

    @OnOpen
    private void onOpen(Session session) {
        sessions.add(session);
        logger.info("New websocket client connected. Total # websocket clients %s".formatted(sessions.size()));
    }

    @OnMessage
    private void onMessage(String message) {
        logger.info("Received websocket message %s".formatted(message));
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        logger.info("Websocket session closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        logger.info("Websocket error");
    }

    public void setLatestEvent(String event) {
        latestEvent.set(event);
    }


    public void broadcast(String message) {
        logger.info("Sending message %s to %s clients".formatted(message, sessions.size()));
        sessions.forEach(s -> {
            s.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    private void broadcastLatestEvent() {
        String event = latestEvent.get();
        if (event != null) {
            this.broadcast(event);
            // Optionally reset the event to avoid sending the same event repeatedly
            latestEvent.set(null);
        }
    }
}
