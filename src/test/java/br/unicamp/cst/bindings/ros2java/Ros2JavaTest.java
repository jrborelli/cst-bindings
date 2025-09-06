
package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Memory;
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
import troca_ros.*;

/**
 * @author jrborelli
 */
        
public class Ros2JavaTest {

    private static final Logger logger = Logger.getLogger(Ros2JavaTest.class.getName());
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
            public void fillMemoryWithReceivedMessage(StringMessage message, Memory sensoryMemory) {
                sensoryMemory.setI(message.data);
                lasttime = sensoryMemory.getTimestamp();
                logger.log(Level.INFO,"I heard: {0} at {1}", new Object[]{message.data , TimeStamp.getStringTimeStamp(lasttime)});                
            }
        };
        

        RosTopicPublisherCodelet<StringMessage> publisher = new RosTopicPublisherCodelet<>("chatter", StringMessage.class) {
            @Override
            protected StringMessage createNewMessage() {
                return new StringMessage();
            }
            @Override
            protected void fillMessageToBePublished(Memory motorMemory, StringMessage message) {
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
        logger.log(Level.INFO,"Starting: {0}",TimeStamp.getStringTimeStamp(orig));
        mind.start();
        long novo = internalMemory.getTimestamp();
        // Wait until a new info is actualized by the subscriber codelet (the Timestamp is changed)
        while(novo == orig) novo = internalMemory.getTimestamp();
        logger.log(Level.INFO, "First message received by: {0} ... it took {1} seconds", new Object[]{TimeStamp.getStringTimeStamp(novo), TimeStamp.getStringTimeStamp(novo - orig, "ss.SSS")});
        String actualMessage = (String) internalMemory.getI();
        assertEquals(expectedMessage, actualMessage);

        mind.shutDown();        
        publisher.stop();
        subscriber.stop();
        cleanup();
        logger.log(Level.INFO,"Finished first test...");
    }  
    
    @Test
    public void testChatterTopicSpecializedCodelets() throws InterruptedException {

        // Instantiate both specialized classes
        ROS2_ChatterTopicPublisher publisher = new ROS2_ChatterTopicPublisher("chatter");
        ROS2_ChatterTopicSubscriber subscriber = new ROS2_ChatterTopicSubscriber("chatter");

        // Create a memory object to link them
        MemoryObject internalMemory = mind.createMemoryObject("chatter");
        subscriber.addOutput(internalMemory);
        publisher.addInput(internalMemory);

        // Send a message
        String expectedMessage = "Hello from the CST Mind, this is a Specialized Topic Publisher Codelet!";
        internalMemory.setI(expectedMessage);

        mind.insertCodelet(subscriber);
        mind.insertCodelet(publisher);
        long orig = internalMemory.getTimestamp();
        logger.log(Level.INFO,"Starting: {0}" , TimeStamp.getStringTimeStamp(orig));
        mind.start();
        
        long novo = internalMemory.getTimestamp();
        // Wait until a new info is actualized by the subscriber codelet (the Timestamp is changed)
        while(novo == orig) {
            Thread.sleep(100);
            novo = internalMemory.getTimestamp();
        }
        
        logger.log(Level.INFO, "First message received by: {0} ... it took {1} seconds", new Object[]{TimeStamp.getStringTimeStamp(novo), TimeStamp.getStringTimeStamp(novo - orig, "ss.SSS")});
        String actualMessage = (String) internalMemory.getI();

        // Assert that the received message is the same as the sent one
        assertEquals(expectedMessage, actualMessage);

        // Cleanup
        mind.shutDown();
        publisher.stop();
        subscriber.stop();
        cleanup();
        logger.log(Level.INFO,"Finished testChatterTopicIntegration...");
    }

    
    @Test
    public void testOneShotPublisher() throws InterruptedException {

        // Instantiate the one-shot publisher
        RosTopicOneShotPublisherCodelet<StringMessage> oneShotPublisher = new RosTopicOneShotPublisherCodelet<>("one_shot_chatter", StringMessage.class) {
            @Override
            public void fillMessageToBePublished(Memory motorMemory, StringMessage message) {
                Object data = motorMemory.getI();
                if (data instanceof String) {
                    message.withData((String) data);
                } else {
                    message.withData("");
                }
            }

            @Override
            public StringMessage createMessage() {
                return new StringMessage();
            }
        };

        // Instantiate the subscriber
        ROS2_ChatterTopicSubscriber subscriber = new ROS2_ChatterTopicSubscriber("one_shot_chatter");

        // Create a memory object to link them
        MemoryObject internalMemory = mind.createMemoryObject("one_shot_chatter");
        subscriber.addOutput(internalMemory);
        oneShotPublisher.addInput(internalMemory);

        mind.insertCodelet(subscriber);
        mind.insertCodelet(oneShotPublisher);
        
        mind.start();
        
        // Wait for mind to stabilize
        Thread.sleep(500);

        // Set the message and enable the publisher
        String expectedMessage = "This message should be sent only once!";
        internalMemory.setI(expectedMessage);
        oneShotPublisher.setEnabled(true);
        
        long orig = internalMemory.getTimestamp();
        logger.log(Level.INFO,"Starting one-shot test: " , TimeStamp.getStringTimeStamp(orig));

        // Wait until a new info is actualized by the subscriber codelet
        long novo = internalMemory.getTimestamp();
        while(novo == orig) {
            Thread.sleep(100);
            novo = internalMemory.getTimestamp();
        }
        
        logger.log(Level.INFO, "One-shot message received at: {0} ... it took {1} seconds", new Object[]{TimeStamp.getStringTimeStamp(novo), TimeStamp.getStringTimeStamp(novo - orig, "ss.SSS")});
        String actualMessage = (String) internalMemory.getI();

        // Assert that the received message is the same as the sent one
        assertEquals(expectedMessage, actualMessage);

        // Cleanup
        mind.shutDown();
        oneShotPublisher.stop();
        subscriber.stop();
        cleanup();
        logger.log(Level.INFO,"Finished testOneShotPublisher...");
    }
    
    @Test
    public void testRosServiceClientCodeletAsync() throws InterruptedException {
        logger.log(Level.INFO,"Starting ROS service client test...");

        // Create a mock service provider for the test
        AddTwoIntsServiceProvider serviceProvider = new AddTwoIntsServiceProvider();
        serviceProvider.start();
        Thread.sleep(500); // Allow time for service to be discovered

        // Instantiate the specialized client codelet
        RosServiceClientCodelet<AddTwoIntsRequestMessage, AddTwoIntsResponseMessage> clientCodelet = new RosServiceClientCodelet<>("add_two_ints", new AddTwoIntsServiceDefinition()) {
            @Override
            protected AddTwoIntsRequestMessage createNewRequest() {
                return new AddTwoIntsRequestMessage();
            }

            @Override
            protected boolean formatServiceRequest(Memory inputMemoryObject, AddTwoIntsRequestMessage request) {
                Long[] inputs = (Long[]) inputMemoryObject.getI();
                if (inputs == null || inputs.length < 2) return false;
                request.withA(inputs[0]);
                request.withB(inputs[1]);
                return true;
            }

            @Override
            protected void processServiceResponse(AddTwoIntsResponseMessage response) {
                if (response != null) {
                    logger.log(Level.INFO,"Sum received from service: {0}" , response.sum);
                    // Update the input memory with the response for verification
                    this.inputMemory.setI(response.sum);
                }
            }
        };

        // Create a memory object to hold the input and output
        MemoryObject internalMemory = mind.createMemoryObject("add_two_ints");
        clientCodelet.addInput(internalMemory);

        // Set the input data and run the codelet
        Long[] inputs = new Long[]{10L, 20L};
        internalMemory.setI(inputs);

        mind.insertCodelet(clientCodelet);
        mind.start();
        
        long orig = internalMemory.getTimestamp();
        logger.log(Level.INFO,"Starting service request at: {0}" , TimeStamp.getStringTimeStamp(orig));

        long novo = internalMemory.getTimestamp();
        // Wait until the memory object is updated with the service response
        while(novo == orig) {
            Thread.sleep(100);
            novo = internalMemory.getTimestamp();
        }

        logger.log(Level.INFO, "Service response received at: {0}", TimeStamp.getStringTimeStamp(novo));
        Long actualSum = (Long) internalMemory.getI();

        // Assert that the sum is correct
        assertEquals(Long.valueOf(30L), actualSum);

        // Cleanup
        mind.shutDown();
        clientCodelet.stop();
        serviceProvider.stop();
        cleanup();
        logger.log(Level.INFO, "Finished testRosServiceClientCodelet...");
    }

    @Test
    public void testRos2ServiceSync() throws InterruptedException, ExecutionException, TimeoutException {
        logger.log(Level.INFO, "Starting 2nd test...");
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
        //logger.info("It took "+TimeStamp.getDelaySinceStart());
        logger.log(Level.INFO, "It took {0}", TimeStamp.getDelaySinceStart());
    }
}


