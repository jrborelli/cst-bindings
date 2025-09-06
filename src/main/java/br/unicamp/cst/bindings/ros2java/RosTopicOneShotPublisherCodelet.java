/***********************************************************************************************
 * Copyright (c) 2012  DCA-FEEC-UNICAMP
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * K. Raizer, A. L. O. Paraense, E. M. Froes, R. R. Gudwin - initial API and implementation
 * jrborelli - ROS2
 ***********************************************************************************************/


package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import id.jrosmessages.Message;
import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientFactory;
import id.jrosclient.TopicSubmissionPublisher;

public abstract class RosTopicOneShotPublisherCodelet<T extends Message> extends Codelet {

    protected String topic;
    protected Class<T> messageType;
    protected Memory motorMemory;

    protected JRos2Client ros2Client;
    protected TopicSubmissionPublisher<T> publisher;

    private volatile boolean enabled = false;

    public RosTopicOneShotPublisherCodelet(String topic, Class<T> messageType) {
        this.topic = topic;
        this.messageType = messageType;
        setName("Ros2Publisher:" + topic);
    }

    @Override
    public synchronized void start() {
        try {
            ros2Client = new JRos2ClientFactory().createClient();
            publisher = new TopicSubmissionPublisher<>(messageType, topic);
            ros2Client.publish(publisher); // 🔧 REGISTER THE PUBLISHER
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ROS 2 publisher for topic: " + topic, e);
        }

        super.start();
    }

    @Override
    public synchronized void stop() {
        try {
            if (publisher != null) publisher.close();
            if (ros2Client != null) ros2Client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.stop();
    }

    @Override
    public void accessMemoryObjects() {
        if (motorMemory == null) {
            motorMemory = getInput(topic, 0);
        }
    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(enabled ? 1.0 : 0.0);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void proc() {
        if (!enabled || motorMemory == null) return;

        T message = createMessage();
        fillMessageToBePublished(motorMemory, message);
        publisher.submit(message); // 🔧 ACTUALLY PUBLISH THE MESSAGE

        enabled = false;
    }

    /**
     * Method to fill the message to be published.
     */
    public abstract void fillMessageToBePublished(Memory motorMemory, T message);

    /**
     * Create a new instance of the message.
     */
    public abstract T createMessage(); // 🔧 Needed to avoid null message usage

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}


