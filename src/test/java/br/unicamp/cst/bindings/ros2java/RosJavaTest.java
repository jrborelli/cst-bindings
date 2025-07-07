/**
 * 
 */
package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import id.jrosmessages.std_msgs.StringMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pinorobotics.jrosservices.msgs.ServiceDefinition;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class RosJavaTest {

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
        // Setup subscriber codelet for "chatter"
        RosTopicSubscriberCodelet<StringMessage> subscriber = new RosTopicSubscriberCodelet<>("chatter", StringMessage.class) {
            @Override
            public void fillMemoryWithReceivedMessage(StringMessage message, br.unicamp.cst.core.entities.Memory sensoryMemory) {
                sensoryMemory.setI(message.getData());
                System.out.println("I heard: \"" + message.getData() + "\"");
            }
        };
        MemoryObject sensoryMemory = mind.createMemoryObject(subscriber.getName());
        subscriber.addOutput(sensoryMemory);
        mind.insertCodelet(subscriber);

        // Setup publisher codelet for "chatter"
        RosTopicPublisherCodelet<StringMessage> publisher = new RosTopicPublisherCodelet<>("chatter", StringMessage.class) {
            @Override
            protected StringMessage createNewMessage() {
                return new StringMessage();
            }

            @Override
            protected void fillMessageToBePublished(br.unicamp.cst.core.entities.Memory motorMemory, StringMessage message) {
                String data = (String) motorMemory.getI();
                if (data != null) {
                    message.setData(data);
                }
            }
        };
        MemoryObject motorMemory = mind.createMemoryObject(publisher.getName());
        publisher.addInput(motorMemory);

        // Set the message to publish
        String expectedMessage = "Hello World";
        motorMemory.setI(expectedMessage);
        mind.insertCodelet(publisher);

        mind.start();

        // Wait some time to allow message flow
        Thread.sleep(3000);

        String actualMessage = (String) sensoryMemory.getI();

        assertEquals(expectedMessage, actualMessage);

        mind.shutDown();

        // Wait to let shutdown complete
        Thread.sleep(2000);
    }

    @Test
    public void testRos2ServiceSync() throws InterruptedException, ExecutionException, TimeoutException {
        // Start the service node in the background (you'll need to implement it separately)
        AddTwoIntServiceRos2 addTwoIntService = new AddTwoIntServiceRos2();
        addTwoIntService.start();

        Thread.sleep(2000); // Wait for service to be ready

        AddTwoIntServiceClientSyncRos2 clientSync = new AddTwoIntServiceClientSyncRos2("add_two_ints");
        clientSync.start();

        // Test call 1
        long expectedSum1 = 5L;
        Integer[] args1 = new Integer[] {2, 3};
        long actualSum1 = clientSync.callService(args1).getSum();
        assertEquals(expectedSum1, actualSum1);

        // Test call 2
        long expectedSum2 = 6L;
        Integer[] args2 = new Integer[] {2, 4};
        long actualSum2 = clientSync.callService(args2).getSum();
        assertEquals(expectedSum2, actualSum2);

        clientSync.stop();
        addTwoIntService.stop();
    }

    // You can add more tests for asynchronous clients similarly

}