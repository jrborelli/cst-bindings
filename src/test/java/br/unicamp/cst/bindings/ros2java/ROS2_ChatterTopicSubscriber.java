package br.unicamp.cst.bindings.ros2java;


import br.unicamp.cst.core.entities.Memory;
import id.jrosmessages.std_msgs.StringMessage;

/**
 * @author jrborelli
 *
 */


public class ROS2_ChatterTopicSubscriber extends RosTopicSubscriberCodelet<StringMessage> {

    public ROS2_ChatterTopicSubscriber(String topic) {
        super(topic, StringMessage.class);
    }

    @Override
    public void fillMemoryWithReceivedMessage(StringMessage message, Memory sensoryMemory) {
        if (message == null || sensoryMemory == null) return;

        String data = message.data;
        sensoryMemory.setI(data);
        System.out.println("Received chatter message: " + data);
    }
}
