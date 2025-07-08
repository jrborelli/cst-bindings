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
import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientFactory;
import id.jrosclient.TopicSubscriber;
import id.jrosmessages.Message;

public abstract class RosTopicSubscriberCodelet<T extends Message> extends Codelet {

    protected String topic;
    protected Class<T> messageType;

    protected T latestMessage;
    protected Memory sensoryMemory;

    protected JRos2Client ros2Client;
    protected TopicSubscriber<T> subscriber;

    public RosTopicSubscriberCodelet(String topic, Class<T> messageType) {
        this.topic = topic;
        this.messageType = messageType;
        setName("Ros2Subscriber:" + topic);
    }

    @Override
    public synchronized void start() {
        ros2Client = new JRos2ClientFactory().createClient();

        subscriber = new TopicSubscriber<T>(messageType, topic) {
            @Override
            public void onNext(T item) {
                latestMessage = item;
                // request next message to keep receiving data
                getSubscription().get().request(1);
            }
        };

        ros2Client.subscribe(subscriber);
        super.start();
    }

    @Override
    public synchronized void stop() {
        try {
            //if (subscriber != null) subscriber.close();
            if (ros2Client != null) ros2Client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.stop();
    }

    @Override
    public void accessMemoryObjects() {
        if (sensoryMemory == null) {
            sensoryMemory = this.getOutput(topic, 0);
        }
    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(1.0); // ready to process incoming messages
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void proc() {
        if (latestMessage != null && sensoryMemory != null) {
            fillMemoryWithReceivedMessage(latestMessage, sensoryMemory);
            latestMessage = null; // consume message to avoid processing it multiple times
        }
    }

    /**
     * Fill the sensory memory with the latest received message.
     */
    public abstract void fillMemoryWithReceivedMessage(T message, Memory sensoryMemory);
}

/* Exemplo de uso:
import br.unicamp.cst.bindings.ros2java.RosTopicSubscriberCodelet;
import br.unicamp.cst.core.entities.Memory;
import id.jrosmessages.std_msgs.StringMessage;


 //CST Codelet subscribing to a ROS 2 topic and storing received StringMessage in memory.
 
public class HelloRosSubscriberCodelet extends RosTopicSubscriberCodelet<StringMessage> {

    public HelloRosSubscriberCodelet() {
        // topic name: /helloRos
        // message type: std_msgs/String
        super("/helloRos", StringMessage.class);
    }

    @Override
    public void fillMemoryWithReceivedMessage(StringMessage message, Memory sensoryMemory) {
        // Just store the string data inside memory
        System.out.println("Received from ROS: " + message.getData());
        sensoryMemory.setI(message.getData());
    }
}


import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Thing;
import br.unicamp.cst.core.entities.Codelet;

public class HelloRosMain {

    public static void main(String[] args) throws Exception {
        Thing agent = new Thing();

        Memory outputMemory = new Memory();
        agent.addOutput(outputMemory, "helloRos");

        Codelet subscriber = new HelloRosSubscriberCodelet();
        subscriber.addOutput(outputMemory);

        agent.insertCodelet(subscriber);

        // Starts the CST main loop
        agent.start();
    }
}

To test this, run a publisher in a separate terminal or Java process like:
ros2 topic pub /helloRos std_msgs/String "data: 'Hello from Python ROS 2!'"
*/