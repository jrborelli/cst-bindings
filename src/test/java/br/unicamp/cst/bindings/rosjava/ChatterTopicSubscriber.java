/**
 * 
 */
package br.unicamp.cst.bindings.rosjava;

import br.unicamp.cst.core.entities.Memory;

import java.net.URI;

/**
 * @author andre
 *
 */
public class ChatterTopicSubscriber extends RosTopicSubscriberCodelet<std_msgs.String> {

	public ChatterTopicSubscriber(String host, URI masterURI) {
		super("ChatterTopicSubscriber", "chatter", std_msgs.String._TYPE, host, masterURI);
	}

	@Override
	public void fillMemoryWithReceivedMessage(std_msgs.String message, Memory sensoryMemory) {
		if(message == null) {
			sensoryMemory.setI(null);
			return;
		}
		
		String messageData = message.getData();
		
		if(messageData == null) {
			sensoryMemory.setI(null);
			return;
		}
		
		System.out.println("I heard: \"" + messageData + "\"");
		sensoryMemory.setI(messageData);
	}
}
