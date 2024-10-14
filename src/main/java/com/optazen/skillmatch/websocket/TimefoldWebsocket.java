package com.optazen.skillmatch.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/timefold")
@ApplicationScoped
public class TimefoldWebsocket {
    protected static final Logger logger = LoggerFactory.getLogger(TimefoldWebsocket.class);

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

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
}
