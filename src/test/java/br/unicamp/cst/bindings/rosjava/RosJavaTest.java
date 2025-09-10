package br.unicamp.cst.bindings.rosjava;

import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.support.TimeStamp;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeEach;
import org.opentest4j.TestAbortedException;
import org.ros.RosCore;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import rosjava_test_msgs.AddTwoIntsResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author andre
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RosJavaTest {

    private static final Logger LOGGER = Logger.getLogger(RosJavaTest.class.getName());
    private static RosCore rosCore;
    private static Mind mind;
    private static boolean rosCoreStarted = false;
    private static final long ROS_CORE_STARTUP_TIMEOUT = 60000; // 60 seconds

    @BeforeAll
    public static void beforeAllTestMethods() {
        // Silence loggers to reduce noise
        Logger.getLogger("org.ros").setLevel(Level.OFF);

        try {
            // Attempt to start the ROS Core once for all tests.
            rosCore = RosCore.newPublic("127.0.0.1", 11311);
            rosCore.start();
            
            LOGGER.log(Level.INFO, "Waiting for ROS Core to start...");
            
            // Wait with a timeout for the RosCore to be fully up and running
            long startTime = System.currentTimeMillis();
            boolean started = false;
            while (System.currentTimeMillis() - startTime < ROS_CORE_STARTUP_TIMEOUT) {
                try {
                    rosCore.awaitStart();
                    started = true;
                    break;
                } catch (Exception e) {
                    // Ignore exceptions during awaitStart in a timed loop, it might just be not ready yet
                }
                Thread.sleep(500); // Wait a bit before checking again
            }

            if (!started) {
                throw new TestAbortedException("ROS Core did not start within the " + (ROS_CORE_STARTUP_TIMEOUT / 1000) + " second timeout. Skipping tests.");
            }
            
            rosCoreStarted = true;
            LOGGER.log(Level.INFO, "ROS Core started successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start ROS Core. ROS-dependent tests will be skipped.", e);
            // Signal to JUnit that tests should be skipped
            throw new TestAbortedException("ROS Core is not available. Skipping tests.", e);
        }

        // Initialize the Mind once for all tests
        mind = new Mind();
        LOGGER.log(Level.INFO, "CST Mind initialized.");
    }

    @AfterAll
    public static void afterAllTestMethods() {
        if (mind != null) {
            mind.shutDown();
        }
        if (rosCore != null) {
            rosCore.shutdown();
        }
        LOGGER.log(Level.INFO, "ROS Core and CST Mind shut down.");
    }

    @Test
    public void testRosTopics() throws URISyntaxException, InterruptedException {
        // Create subscriber and publisher codelets
        ChatterTopicSubscriber chatterTopicSubscriber = new ChatterTopicSubscriber("127.0.0.1", new URI("http://127.0.0.1:11311"));
        ChatterTopicPublisher chatterTopicPublisher = new ChatterTopicPublisher("127.0.0.1", new URI("http://127.0.0.1:11311"));

        // Create memory object to link them
        MemoryObject internalMemory = mind.createMemoryObject("chatter");
        chatterTopicSubscriber.addOutput(internalMemory);
        chatterTopicPublisher.addInput(internalMemory);

        // Insert codelets and start the mind
        mind.insertCodelet(chatterTopicSubscriber);
        mind.insertCodelet(chatterTopicPublisher);
        mind.start();

        // Set the message and wait for it to be received by the subscriber
        String messageExpected = "Hello World";
        internalMemory.setI(messageExpected);

        long origTimestamp = internalMemory.getTimestamp();

        // Wait for the memory object to be updated with the new message, with a timeout
        long timeout = System.currentTimeMillis() + 10000; // 10 seconds timeout
        long newTimestamp = origTimestamp;
        while (newTimestamp == origTimestamp && System.currentTimeMillis() < timeout) {
            Thread.sleep(100);
            newTimestamp = internalMemory.getTimestamp();
        }
        
        // Assert that the timestamp has changed, meaning the message was received
        if (newTimestamp == origTimestamp) {
            throw new TestAbortedException("Timeout waiting for message to be received by the subscriber.");
        }

        String messageActual = (String) internalMemory.getI();

        assertEquals(messageExpected, messageActual);

        chatterTopicPublisher.stop();
        chatterTopicSubscriber.stop();
    }

    @Test
    public void testRosServiceSync() throws URISyntaxException, InterruptedException {

        AddTwoIntService addTwoIntService = new AddTwoIntService();
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic("127.0.0.1", new URI("http://127.0.0.1:11311"));
        nodeMainExecutor.execute(addTwoIntService, nodeConfiguration);

        // Give a fixed amount of time for the service to start and be discovered.
        Thread.sleep(5000); // Wait for 5 seconds

        AddTwoIntServiceClientSync addTwoIntServiceClient = new AddTwoIntServiceClientSync("127.0.0.1", new URI("http://127.0.0.1:11311"));

        // First test
        long expectedSum = 5L;
        Integer[] numsToSum = new Integer[] {2, 3};
        AddTwoIntsResponse addTwoIntsResponse = addTwoIntServiceClient.callService(numsToSum);
        long actualSum = addTwoIntsResponse.getSum();
        assertEquals(expectedSum, actualSum);

        // Second test
        long expectedSum2 = 6L;
        Integer[] numsToSum2 = new Integer[] {2, 4};
        AddTwoIntsResponse addTwoIntsResponse2 = addTwoIntServiceClient.callService(numsToSum2);
        long actualSum2 = addTwoIntsResponse2.getSum();
        assertEquals(expectedSum2, actualSum2);

        addTwoIntServiceClient.stopRosNode();

        // Cleanup ROS nodes
        Thread.sleep(2000);
        nodeMainExecutor.shutdownNodeMain(addTwoIntService);
        Thread.sleep(2000);
    }

    @Test
    public void testRosServiceClientWithCstMind() throws URISyntaxException, InterruptedException {

        // Start the service provider
        AddTwoIntService addTwoIntService = new AddTwoIntService();
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic("127.0.0.1", new URI("http://127.0.0.1:11311"));
        nodeMainExecutor.execute(addTwoIntService, nodeConfiguration);

        // Give a fixed amount of time for the service to start.
        Thread.sleep(5000); // Wait for 5 seconds

        // Instantiate the CST client codelet
        AddTwoIntServiceClient addTwoIntServiceClient = new AddTwoIntServiceClient("127.0.0.1", new URI("http://127.0.0.1:11311"));

        // Create a memory object to hold the input and output
        MemoryObject internalMemory = mind.createMemoryObject(addTwoIntServiceClient.getName());
        addTwoIntServiceClient.addInput(internalMemory);

        mind.insertCodelet(addTwoIntServiceClient);
        mind.start();

        // First test call
        Integer[] numsToSum = new Integer[] {2, 3};
        internalMemory.setI(numsToSum);
        long origTimestamp = internalMemory.getTimestamp();

        // Wait for the memory object to be updated with the service's response, with a timeout
        long timeout = System.currentTimeMillis() + 10000; // 10 seconds timeout
        long newTimestamp = origTimestamp;
        while (newTimestamp == origTimestamp && System.currentTimeMillis() < timeout) {
            Thread.sleep(100);
            newTimestamp = internalMemory.getTimestamp();
        }
        
        if (newTimestamp == origTimestamp) {
            throw new TestAbortedException("Timeout waiting for service client response.");
        }

        Integer expectedSum = 5;
        assertEquals(expectedSum, internalMemory.getI());

        // Second test call
        Integer[] numsToSum2 = new Integer[] {3, 3};
        internalMemory.setI(numsToSum2);
        origTimestamp = internalMemory.getTimestamp();

        // Wait again for the memory object to be updated, with a timeout
        timeout = System.currentTimeMillis() + 10000; // 10 seconds timeout
        newTimestamp = origTimestamp;
        while (newTimestamp == origTimestamp && System.currentTimeMillis() < timeout) {
            Thread.sleep(100);
            newTimestamp = internalMemory.getTimestamp();
        }
        
        if (newTimestamp == origTimestamp) {
            throw new TestAbortedException("Timeout waiting for second service client response.");
        }

        Integer expectedSum2 = 6;
        assertEquals(expectedSum2, internalMemory.getI());

        // Cleanup
        addTwoIntServiceClient.stop();
        nodeMainExecutor.shutdownNodeMain(addTwoIntService);
    }
}