package com.givinghand.service;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Receives GivingHandQueue messages and logs the standardized JSON notification payload to the console.
 * There is no direct REST endpoint for this MDB; it is triggered asynchronously by JMS producers in the business services.
 * Important notes: destinationLookup is configured for JBoss EAP 7.3 ActiveMQ Artemis compatibility.
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/GivingHandQueue") })
public class NotificationMDB implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                System.out.println("GivingHand notification received: " + ((TextMessage) message).getText());
            } else {
                System.out.println("GivingHand notification received: unsupported message type");
            }
        } catch (Exception e) {
            System.out.println("GivingHand notification processing failed: " + e.getMessage());
        }
    }
}
