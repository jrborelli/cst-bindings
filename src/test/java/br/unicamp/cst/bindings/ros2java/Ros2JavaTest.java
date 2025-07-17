/**
 * @author jrborelli
 */
package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import id.jrosmessages.std_msgs.StringMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Ros2JavaTest {

    private static Mind mind;

    @BeforeClass
    public static void setup() {
        mind = new Mind();
    }

    @AfterClass
    public static void cleanup() {
        if (mind != null) {
            mind.shutDown();
        }
    }

    @Test
    
    public void testRos2Topics() throws InterruptedException {
        
        RosTopicSubscriberCodelet<StringMessage> subscriber = new RosTopicSubscriberCodelet<>("chatter", StringMessage.class) {
            @Override
            public void fillMemoryWithReceivedMessage(StringMessage message, br.unicamp.cst.core.entities.Memory sensoryMemory) {
                sensoryMemory.setI(message.data);
                System.out.println("I heard: \"" + message.data + "\"");
            }
        };
        

        RosTopicPublisherCodelet<StringMessage> publisher = new RosTopicPublisherCodelet<>("chatter", StringMessage.class) {
            @Override
            protected StringMessage createNewMessage() {
                return new StringMessage();
            }
            @Override
            protected void fillMessageToBePublished(br.unicamp.cst.core.entities.Memory motorMemory, StringMessage message) {
                String data = (String) motorMemory.getI();
                if (data != null) {
                    message.data = data;
                }
            }
        };
        

        MemoryObject sensoryMemory = mind.createMemoryObject("chatter");
        subscriber.addOutput(sensoryMemory);

        MemoryObject motorMemory = mind.createMemoryObject("chatter");
        publisher.addInput(motorMemory);

        // Send the message
        String expectedMessage = "Hello World";
        motorMemory.setI(expectedMessage);

        mind.insertCodelet(subscriber);
        mind.insertCodelet(publisher);
        mind.start();

        Thread.sleep(4000); // allow some time for message exchange
         
        String actualMessage = (String) sensoryMemory.getI();
        assertEquals(expectedMessage, actualMessage);

        //Thread.sleep(500);
        
        mind.shutDown();
    }  
    

    @Test
    public void testRos2ServiceSync() throws InterruptedException, ExecutionException, TimeoutException {
        // You must already have the Python/C++ ROS 2 service running (troca_ros/AddTwoIntsService)

        // Start the client
        AddTwoIntsServiceClientSyncRos2 clientSync = new AddTwoIntsServiceClientSyncRos2("add_two_ints");
        clientSync.start();

        // First test
        long expectedSum1 = 5L;
        Object[] args1 = new Object[]{2L, 3L};
        AddTwoIntsResponseMessage response1 = clientSync.callService(args1);
        long actualSum1 = response1.sum;
        assertEquals(expectedSum1, actualSum1);

        // Second test
        long expectedSum2 = 6L;
        Object[] args2 = new Object[]{2L, 4L};
        AddTwoIntsResponseMessage response2 = clientSync.callService(args2);
        long actualSum2 = response2.sum;
        assertEquals(expectedSum2, actualSum2);

        clientSync.stop();
    }
}