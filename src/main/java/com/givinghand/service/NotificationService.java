package com.givinghand.service;

import java.time.Instant;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.JMSContext;
import javax.jms.Queue;
//import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.givinghand.model.NotificationEvent;
import com.givinghand.model.NotificationEventType;


@Stateless
@javax.jms.JMSDestinationDefinition(name = "java:/jms/queue/GivingHandQueue", interfaceName = "javax.jms.Queue", destinationName = "GivingHandQueue")
public class NotificationService {

    @PersistenceContext(unitName = "givinghandPU")
    private EntityManager em;

    @javax.inject.Inject
    private JMSContext jmsContext;

    @Resource(lookup = "java:/jms/queue/GivingHandQueue")
    private Queue queue;

    public NotificationEvent sendNotification(NotificationEventType eventType, String message) {
        NotificationEvent event = new NotificationEvent();
        event.setEventType(eventType.name());
        event.setMessage(message);
        event.setTimestamp(Instant.now().toString());
        em.persist(event);
        em.flush();

        jmsContext.createProducer().send(queue, buildJson(event));
        return event;
    }

    public List<NotificationEvent> getNotifications() {
        return em.createQuery("SELECT n FROM NotificationEvent n ORDER BY n.id", NotificationEvent.class).getResultList();
    }

    private String buildJson(NotificationEvent event) {
        return "{\"event_type\":\"" + escapeJson(event.getEventType()) + "\",\"message\":\""
                + escapeJson(event.getMessage()) + "\",\"timestamp\":\"" + escapeJson(event.getTimestamp()) + "\"}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
