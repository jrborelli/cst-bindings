/**
 * @author jrborelli
 */
package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.support.TimeStamp;
import id.jrosmessages.std_msgs.StringMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import troca_ros.AddTwoIntsResponseMessage;

public class Ros2JavaTest {

    private static Mind mind;

    @BeforeClass
    public static void setup() {
        SilenceLoggers();
        mind = new Mind();
    }

    @AfterClass
    public static void cleanup() {
        if (mind != null) {
            mind.shutDown();
        }
    }
    
    private static void SilenceLoggers() {
        Logger.getLogger("pinorobotics.rtpstalk").setLevel(Level.OFF);
        Logger.getLogger("id.jros2client").setLevel(Level.OFF);
    }

    @Test
    public void testRos2Topics() throws InterruptedException {
        
        setup();
        RosTopicSubscriberCodelet<StringMessage> subscriber = new RosTopicSubscriberCodelet<>("chatter", StringMessage.class) {
            public long lasttime = 0;
            @Override
            public void fillMemoryWithReceivedMessage(StringMessage message, br.unicamp.cst.core.entities.Memory sensoryMemory) {
                sensoryMemory.setI(message.data);
                lasttime = sensoryMemory.getTimestamp();
                System.out.println("I heard: \"" + message.data + "\" at "+TimeStamp.getStringTimeStamp(lasttime));                
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

        MemoryObject internalMemory = mind.createMemoryObject("chatter");
        subscriber.addOutput(internalMemory);
        publisher.addInput(internalMemory);

        // Send the message
        String expectedMessage = "Hello World";
        internalMemory.setI(expectedMessage);

        mind.insertCodelet(subscriber);
        mind.insertCodelet(publisher);
        long orig = internalMemory.getTimestamp();
        System.out.println("Starting: "+TimeStamp.getStringTimeStamp(orig));
        mind.start();
        long novo = internalMemory.getTimestamp();
        // Wait until a new info is actualized by the subscriber codelet (the Timestamp is changed)
        while(novo == orig) novo = internalMemory.getTimestamp();
        System.out.println("First message received by: "+TimeStamp.getStringTimeStamp(novo)+" ... it took "+TimeStamp.getStringTimeStamp(novo-orig,"ss.SSS")+" seconds");
        String actualMessage = (String) internalMemory.getI();
        assertEquals(expectedMessage, actualMessage);

        mind.shutDown();        
        publisher.stop();
        subscriber.stop();
        cleanup();
        System.out.println("Finished first test...");
    }  
    

    @Test
    public void testRos2ServiceSync() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Starting 2nd test...");
        TimeStamp.setStartTime();
        // Start the server
        AddTwoIntsServiceProvider serviceProvider = new AddTwoIntsServiceProvider();
        serviceProvider.start();
        
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
        serviceProvider.stop();
        System.out.println("It took "+TimeStamp.getDelaySinceStart());
    }
}