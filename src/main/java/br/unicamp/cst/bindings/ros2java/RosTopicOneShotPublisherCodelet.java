/***********************************************************************************************
 * Copyright (c) 2012  DCA-FEEC-UNICAMP
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * K. Raizer, A. L. O. Paraense, E. M. Froes, R. R. Gudwin - initial API and implementation
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


/* // Second Version, almost there, see reference.
package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import id.jrosmessages.Message;
import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientFactory;
import id.jros2client.qos.PublisherQos;
import id.jros2client.qos.SubscriberQos;
//import id.jros2client.publisher.JRos2Publisher;
import id.jrosclient.TopicSubmissionPublisher;

public abstract class RosTopicOneShotPublisherCodelet<T extends Message> extends Codelet {

    protected String topic;
    protected Class<T> messageType;
    protected Memory motorMemory;

    protected JRos2Client ros2Client;
    //protected JRos2Publisher<T> publisher;
    protected TopicSubmissionPublisher publisher;
    protected T message;

    private volatile boolean enabled = false;

    public RosTopicOneShotPublisherCodelet(String topic, Class<T> messageType) {
        this.topic = topic;
        this.messageType = messageType;
        setName("Ros2Publisher:" + topic);
    }

    @Override
    public synchronized void start() {
        
        //  Exemplo:
        //https://github.com/lambdaprime/jros2client/blob/main/jros2client.examples/generic/src/PublisherApp.java
        // var configBuilder = new JRos2ClientConfiguration.Builder();
        // use configBuilder to override default parameters (network interface, RTPS settings etc)
      //  var client = new JRos2ClientFactory().createClient(configBuilder.build());
       // String topicName = "/helloRos";
       // var publisher = new TopicSubmissionPublisher<>(StringMessage.class, topicName);
        // register a new publisher for a new topic with ROS
       // client.publish(publisher);
       // while (true) {
       //     publisher.submit(new StringMessage().withData("Hello ROS"));
        //    System.out.println("Published");
        //    Thread.sleep(1000);
        //}
        
       
        ros2Client = new JRos2ClientFactory().createClient();
        publisher = new TopicSubmissionPublisher<>(messageType, topic);
        
        //publisher = ros2Client.createPublisher(topic, messageType);    
        //message = publisher.newMessage();
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
        if (!enabled) return;
        if (motorMemory == null) return;

        fillMessageToBePublished(motorMemory, message);
        //publisher.publish(message);
        enabled = false; // publish only once per enable
    }


    public abstract void fillMessageToBePublished(Memory motorMemory, T message);

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}

*/

/*  //First Version:
package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import id.jrosmessages.Message;
import id.jrosmessages.MessageDescriptor;
import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientFactory;
import id.jros2client.publisher.JRos2Publisher;  // your publisher interface/class

import java.util.concurrent.atomic.AtomicBoolean;


 //ROS 2 Topic One-Shot Publisher Codelet that creates and holds a publisher instance.

 //@param <T> ROS 2 Message type extending Message
 
public abstract class RosTopicOneShotPublisherCodelet<T extends Message> extends Codelet {

    protected String topic;
    protected Class<T> messageTypeClass;   // the class of message type (for createPublisher)
    protected MessageDescriptor<T> messageDescriptor;  // optional, if your client uses descriptors

    protected Memory motorMemory;
    protected T message;

    protected JRos2Client ros2Client;
    protected JRos2Publisher<T> publisher;

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public RosTopicOneShotPublisherCodelet(String topic, Class<T> messageTypeClass, MessageDescriptor<T> messageDescriptor) {
        super();
        this.topic = topic;
        this.messageTypeClass = messageTypeClass;
        this.messageDescriptor = messageDescriptor;
        setName("Ros2Publisher:" + topic);
    }

    @Override
    public synchronized void start() {
        try {
            ros2Client = new JRos2ClientFactory().createClient();
            if (messageDescriptor != null) {
                publisher = ros2Client.createPublisher(topic, messageDescriptor);
            } else {
                publisher = ros2Client.createPublisher(topic, messageTypeClass);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ROS 2 client or create publisher", e);
        }
        super.start();
    }

    @Override
    public synchronized void stop() {
        try {
            if (ros2Client != null) ros2Client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.stop();
    }

    @Override
    public void accessMemoryObjects() {
        if (motorMemory == null) {
            motorMemory = this.getInput(topic, 0);
        }
    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(0.0d);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void proc() {
        if (!enabled.get()) return;
        if (motorMemory == null || motorMemory.getI() == null) return;

        if (message == null) {
            message = createNewMessage();
        }

        fillMessageToBePublished(motorMemory, message);

        try {
            if (messageDescriptor != null) {
                ros2Client.publish(topic, messageDescriptor, publisher);
            } else {
                ros2Client.publish(topic, messageTypeClass, publisher);
            }
        } catch (Exception e) {
            System.err.println("Failed to publish message on topic " + topic + ": " + e.getMessage());
        }

        enabled.set(false);  // one-shot disable after publish
    }

 
    protected abstract T createNewMessage();

 
    protected abstract void fillMessageToBePublished(Memory motorMemory, T message);

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
}

*/


