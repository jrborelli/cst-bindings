/**
 * @author jrborelli
 *
 */
package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.Memory;
import id.jrosmessages.std_msgs.StringMessage;

import java.net.URI;



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

