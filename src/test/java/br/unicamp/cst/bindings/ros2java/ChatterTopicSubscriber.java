/**
 * 
 */
package br.unicamp.cst.bindings.ros2java;


import br.unicamp.cst.core.entities.Memory;
import id.jrosmessages.std_msgs.StringMessage;

/**
 * @author andre
 *
 */


public class ChatterTopicSubscriber extends RosTopicSubscriberCodelet<StringMessage> {

    public ChatterTopicSubscriber(String topic) {
        super(topic, StringMessage.class);
    }

    @Override
    public void fillMemoryWithReceivedMessage(StringMessage message, Memory sensoryMemory) {
        if (message == null || sensoryMemory == null) return;

        String data = message.getData();
        sensoryMemory.setI(data);
        System.out.println("Received chatter message: " + data);
    }
}
