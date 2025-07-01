

package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import id.jrosmessages.Message;
import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientFactory;
import pinorobotics.jros2services.JRos2ServiceClient;
import pinorobotics.jros2services.JRos2ServicesFactory;
import pinorobotics.jrosservices.msgs.ServiceDefinition;

import java.util.concurrent.Semaphore;
import java.util.concurrent.CompletableFuture;

public abstract class RosServiceClientCodelet<S extends Message, T extends Message> extends Codelet {

    protected String serviceName;
    protected ServiceDefinition<S, T> serviceDefinition;
    protected Memory inputMemory;

    protected S requestMessage;
    protected JRos2ServiceClient<S, T> serviceClient;
    protected JRos2Client ros2Client;

    private final Semaphore callInProgressSemaphore = new Semaphore(1);

    public RosServiceClientCodelet(String serviceName, ServiceDefinition<S, T> serviceDefinition) {
        this.serviceName = serviceName;
        this.serviceDefinition = serviceDefinition;
        this.setName("Ros2Client:" + serviceName);
    }

    @Override
    public synchronized void start() {
        try {
            ros2Client = new JRos2ClientFactory().createClient();
            serviceClient = new JRos2ServicesFactory().createClient(ros2Client, serviceDefinition, serviceName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ROS 2 client for service: " + serviceName, e);
        }
        super.start();
    }

    @Override
    public synchronized void stop() {
        try {
            if (serviceClient != null) serviceClient.close();
            if (ros2Client != null) ros2Client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.stop();
    }

    @Override
    public void accessMemoryObjects() {
        if (inputMemory == null) {
            inputMemory = this.getInput(serviceName, 0);
        }
    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(1.0);  // always ready to run
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void proc() {
        if (inputMemory == null || inputMemory.getI() == null) return;

        if (!callInProgressSemaphore.tryAcquire()) {
            // Call in progress, skip this cycle
            return;
        }

        try {
            requestMessage = createNewRequest();
            if (!formatServiceRequest(inputMemory, requestMessage)) {
                callInProgressSemaphore.release();
                return; // no need to send request
            }
            
            CompletableFuture<T> responseFuture = serviceClient.sendRequestAsync(requestMessage);
            responseFuture.thenAccept(response -> {
                if (response != null) processServiceResponse(response);
                callInProgressSemaphore.release();
            }).exceptionally(ex -> {
                System.err.println("ROS 2 service call failed: " + ex.getMessage());
                callInProgressSemaphore.release();
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error in ROS 2 service call: " + e.getMessage());
            callInProgressSemaphore.release();
        }
    }

    /**
     * Create a new empty request message instance.
     * 
     */
    protected abstract S createNewRequest();
    /*@Override //exemplo:
    protected AddTwoIntsRequestMessage createNewRequest() {
        return new AddTwoIntsRequestMessage();
    } */
    
    

    /**
     * Fill the request message from input memory.
     * Return true if the request should be sent.
     */
    protected abstract boolean formatServiceRequest(Memory memory, S request);
    /*
    @Override  //exemplo:
    protected boolean formatServiceRequest(Memory memory, AddTwoIntsRequestMessage request) {
        Integer[] inputs = (Integer[]) memory.getI(); // example cast
        if (inputs == null || inputs.length < 2) return false;
        request.setA(inputs[0]);
        request.setB(inputs[1]);
        return true;
}
    */
    

    /**
     * Handle the response message.
     */
    protected abstract void processServiceResponse(T response);
    /*
    @Override
    protected void processServiceResponse(AddTwoIntsResponseMessage response) {
        int sum = response.getSum();
        System.out.println("Sum received: " + sum);
        // Update CST memory or state as needed
    }
    */   
}




/* Fisrt verstion...

package br.unicamp.cst.bindings.ros2java;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import id.jrosmessages.Message;
//import pinorobotics.jros2client.JRos2Client;
import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientFactory;
import pinorobotics.jros2services.JRos2ServiceClient;
//import pinorobotics.jrosservices.JRosServiceClient;
import pinorobotics.jros2services.JRos2ServicesFactory;

import java.util.concurrent.Semaphore;


public abstract class RosServiceClientCodelet<S extends Message, T extends Message> extends Codelet {

    protected String serviceName;
    protected Class<S> requestType = (Class<S>) AddTwoIntsRequestMessage.class;;
    protected Class<T> responseType = (Class<T>) AddTwoIntsResponseMessage.class;

    protected Memory inputMemory;

    protected S requestMessage;
    protected JRos2ServiceClient<S, T> serviceClient;
    protected JRos2Client ros2Client;

    private final Semaphore callInProgressSemaphore = new Semaphore(1);

    public RosServiceClientCodelet(String serviceName, Class<S> requestType, Class<T> responseType) {
        this.serviceName = serviceName;
        this.requestType = requestType;
        this.responseType = responseType;
        this.setName("Ros2Client:" + serviceName);
    }

    @Override
    public void start() {
        try {
             
            ros2Client = new JRos2ClientFactory().createClient();
            
            // Assuming you have a ServiceDefinition class for your service, e.g. AddTwoIntsServiceDefinition
            serviceClient = new JRos2ServicesFactory().createClient(ros2Client, new AddTwoIntsServiceDefinition(), serviceName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ROS 2 client for service: " + serviceName, e);
        }

        super.start();
    }

    @Override
    public void stop() {
        try {
            if (serviceClient != null) serviceClient.close();
            if (ros2Client != null) ros2Client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.stop();
    }

    @Override
    public void accessMemoryObjects() {
        if (inputMemory == null) {
            inputMemory = this.getInput(serviceName, 0);
        }
    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(1.0); // Always run if needed
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void proc() {
        if (inputMemory == null || inputMemory.getI() == null) return;

        try {
            requestMessage = createNewRequest();
            if (formatServiceRequest(inputMemory, requestMessage)) {
                callInProgressSemaphore.acquire();

                T response = serviceClient.sendRequestAsync(requestMessage).get();

                if (response != null) {
                    processServiceResponse(response);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ROS 2 service call: " + e.getMessage());
        } finally {
            callInProgressSemaphore.release();
        }
    }

    protected abstract S createNewRequest();


    protected abstract boolean formatServiceRequest(Memory memory, S request);

    protected abstract void processServiceResponse(T response);
}

*/