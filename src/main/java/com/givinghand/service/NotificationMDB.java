package com.givinghand.service;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


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
