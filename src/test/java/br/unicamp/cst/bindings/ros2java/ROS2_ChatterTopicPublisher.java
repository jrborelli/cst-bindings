/**
 * 
 */
package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.Memory;
import id.jrosmessages.std_msgs.StringMessage;

import java.net.URI;

/**
 * @author jrborelli
 *
 */


public class ROS2_ChatterTopicPublisher extends RosTopicPublisherCodelet<StringMessage> {

    public ROS2_ChatterTopicPublisher(String topic) {
        super(topic, StringMessage.class);
    }

    @Override
    protected StringMessage createNewMessage() {
        return new StringMessage();
    }

    @Override
    protected void fillMessageToBePublished(Memory motorMemory, StringMessage message) {
        Object data = motorMemory.getI();
        if (data instanceof String) {
            message.withData((String) data);
        } else {
            message.withData("");
        }
    }
}

/*
// Setup CST Mind and Memory:
Mind mind = new Mind();

ChatterTopicPublisher publisher = new ChatterTopicPublisher("chatter");
Memory motorMemory = mind.createMemoryObject("chatter");
publisher.addInput(motorMemory);

mind.insertCodelet(publisher);

// Set message to publish
motorMemory.setI("Hello ROS2 from CST!");

// Start mind, let publisher run for a few cycles
mind.start();

// Sleep a bit to allow publishing
Thread.sleep(2000);

// Then shutdown mind
mind.shutDown();
*/