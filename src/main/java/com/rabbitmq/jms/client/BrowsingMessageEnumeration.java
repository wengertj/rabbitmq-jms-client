/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.JMSException;

import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.parse.sql.SqlEvaluator;

class BrowsingMessageEnumeration implements Enumeration<RMQMessage> {

    private static final int BROWSING_CONSUMER_TIMEOUT = 10000; // ms
    private final java.util.Queue<RMQMessage> msgQueue;

    private final Channel channel;
    private final SqlEvaluator evaluator;
    private final RMQSession session;
    private final RMQDestination dest;
    private final String queueName;

    private boolean populated;

    public BrowsingMessageEnumeration(RMQSession session, RMQDestination dest, Channel channel, SqlEvaluator evaluator) throws JMSException {
        this.msgQueue = new ConcurrentLinkedQueue<RMQMessage>();
        this.channel = channel;
        this.session = session;
        this.dest = dest;
        this.queueName = dest.getQueueName();
        this.evaluator = evaluator;
        this.populated = false;
    }

    private void populate() {
        populateQueue(this.msgQueue, this.channel, this.session, this.dest, this.queueName, this.evaluator);
        this.populated = true;
        this.session.closeBrowsingChannel(this.channel); // this should requeue all the messages browsed
    }

    private static void populateQueue(final java.util.Queue<RMQMessage> msgQueue, Channel channel, RMQSession session, RMQDestination dest, String queueName, SqlEvaluator evaluator) {
        try {
            Queue.DeclareOk qdec = channel.queueDeclarePassive(queueName);
            if (qdec.getMessageCount() > 0) {
                int messagesExpected = qdec.getMessageCount();
                BrowsingConsumer bc = new BrowsingConsumer(channel, session, dest, messagesExpected, msgQueue, evaluator);
                String consumerTag = channel.basicConsume(queueName, bc);
                if (bc.finishesInTime(BROWSING_CONSUMER_TIMEOUT))
                    return;
                else
                    channel.basicCancel(consumerTag);
            }
        } catch(Exception e) {
//            System.out.println(String.format(">> ERROR >> Failed to browse queue named [%s], exception [%s]", queueName, e));
//            e.printStackTrace();
//            Just return an empty enumeration: it is not an error to try to browse a non-existent queue
        }
    }

    @Override public boolean hasMoreElements() {
        if (!this.populated) this.populate();
        return !this.msgQueue.isEmpty();
        }

    @Override public RMQMessage nextElement() {
        if (!this.populated) this.populate();
        RMQMessage resp = this.msgQueue.poll();
        if (null==resp) throw new NoSuchElementException();
        return resp;
    }

    void clearQueue() {
        this.msgQueue.clear();
    }
}
