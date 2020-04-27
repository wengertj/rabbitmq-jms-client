/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

import org.junit.jupiter.api.Test;

import javax.jms.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test
 */
public class SelectedTopicMessageIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.topic." + SelectedTopicMessageIT.class.getCanonicalName();

    @Test
    public void testSendAndReceiveTextMessages() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        TestConfiguration[] configurations = new TestConfiguration[]{
                new TestConfiguration(
                        topicSession.createSubscriber(topic, "boolProp", false),
                        "boolean property set to false",
                        new MessageConfigurator() {
                            @Override
                            public void configure(Message message) throws Exception {
                                message.setBooleanProperty("boolProp", true);
                            }
                        }
                ),
                new TestConfiguration(
                        topicSession.createSubscriber(topic, "not boolProp", false),
                        "boolean property set to true",
                        new MessageConfigurator() {
                            @Override
                            public void configure(Message message) throws Exception {
                                message.setBooleanProperty("boolProp", false);
                            }
                        }
                ),
                new TestConfiguration(
                        topicSession.createSubscriber(topic, "someField LIKE 'some%'", false),
                        "string property matching a like operator starting with 'some'",
                        new MessageConfigurator() {
                            @Override
                            public void configure(Message message) throws Exception {
                                message.setStringProperty("someField", "some");
                            }
                        }
                ),
                new TestConfiguration(
                        topicSession.createSubscriber(topic, "someField LIKE 'test%'", false),
                        "string property matching a like operator starting with 'test'",
                        new MessageConfigurator() {
                            @Override
                            public void configure(Message message) throws Exception {
                                message.setStringProperty("someField", "test");
                            }
                        }
                )
        };

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        for (TestConfiguration configuration : configurations) {
            TextMessage message = topicSession.createTextMessage(configuration.message);
            configuration.messageConfigurator.configure(message);
            sender.send(message);
        }

        for (TestConfiguration configuration : configurations) {
            TextMessage message = (TextMessage) configuration.subscriber.receive(1000);
            assertEquals(configuration.message, message.getText());
        }
    }

    private interface MessageConfigurator {

        void configure(Message message) throws Exception;

    }

    private static class TestConfiguration {

        TopicSubscriber subscriber;
        String message;
        MessageConfigurator messageConfigurator;

        public TestConfiguration(TopicSubscriber subscriber, String message, MessageConfigurator messageConfigurator) {
            this.subscriber = subscriber;
            this.message = message;
            this.messageConfigurator = messageConfigurator;
        }
    }
}
