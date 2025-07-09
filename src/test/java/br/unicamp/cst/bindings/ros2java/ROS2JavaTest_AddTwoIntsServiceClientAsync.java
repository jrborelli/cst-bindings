/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.unicamp.cst.bindings.ros2java;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jrborelli
 */

public class ROS2JavaTest_AddTwoIntsServiceClientAsync extends RosServiceClientSync<AddTwoIntsRequestMessage, AddTwoIntsResponseMessage> {

    public ROS2JavaTest_AddTwoIntsServiceClientAsync(String serviceName) {
        super(serviceName, new AddTwoIntsServiceDefinition());
    }

    @Override
    public void formatServiceRequest(Object[] args, AddTwoIntsRequestMessage requestMessage) {
        requestMessage.a = (Integer) args[0];
        requestMessage.b = (Integer) args[1];
    }

    @Override
    protected AddTwoIntsRequestMessage createNewRequest() {
        return new AddTwoIntsRequestMessage();
    }

    // For async testing, just use `callService` but don't block in test
    
    
    @Test
    public void testRos2ServiceAsync() throws Exception {
        ROS2JavaTest_AddTwoIntsServiceClientAsync clientAsync = new ROS2JavaTest_AddTwoIntsServiceClientAsync("add_two_ints");
        clientAsync.start();

        Object[] args = {7, 8};
        CompletableFuture<AddTwoIntsResponseMessage> future = clientAsync.serviceClient.sendRequestAsync(
            new AddTwoIntsRequestMessage().withA(7).withB(8)
        );

        AddTwoIntsResponseMessage response = future.get(5, TimeUnit.SECONDS);
        assertEquals(15L, response.sum);

        clientAsync.stop();
    }
 
    
    /* RASCUNHO
    public void testRos2ServiceAsync() throws Exception {
        AddTwoIntsServiceDefinition service = new AddTwoIntsServiceDefinition();
        service.start(); // Only if you have a local node implementation running

        Thread.sleep(2000); // Let the service become available

        AddTwoIntsServiceClientAsync clientAsync = new AddTwoIntsServiceClientAsync("add_two_ints");
        clientAsync.start();

        Object[] args = {10, 20};
        CompletableFuture<AddTwoIntsResponseMessage> future = clientAsync.serviceClient.sendRequestAsync(
            new AddTwoIntsRequestMessage().withA(10).withB(20)
        );

        AddTwoIntsResponseMessage response = future.get(5, TimeUnit.SECONDS);
        assertEquals(30L, response.sum);

        clientAsync.stop();
        service.stop(); // if applicable
    }  */
}
