/***********************************************************************************************
 * Copyright (c) 2012  DCA-FEEC-UNICAMP
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * K. Raizer, A. L. O. Paraense, E. M. Froes, R. R. Gudwin - initial API and implementation
 ***********************************************************************************************/
package br.unicamp.cst.bindings.ros2java;

import id.jrosmessages.Message;
import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientFactory;
import pinorobotics.jros2services.JRos2ServiceClient;
import pinorobotics.jros2services.JRos2ServicesFactory;
import pinorobotics.jrosservices.msgs.ServiceDefinition;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Synchronous ROS 2 service client wrapper.
 * Spin up JRos2Client and JRos2ServiceClient,
 * make synchronous service calls by blocking on CompletableFuture.get().
 */
public abstract class RosServiceClientSync<S extends Message, T extends Message> {

    protected String serviceName;
    protected ServiceDefinition<S, T> serviceDefinition;

    protected JRos2Client ros2Client;
    protected JRos2ServiceClient<S, T> serviceClient;

    protected S requestMessage;

    public RosServiceClientSync(String serviceName, ServiceDefinition<S, T> serviceDefinition) {
        this.serviceName = serviceName;
        this.serviceDefinition = serviceDefinition;
    }

    public void start() {
        ros2Client = new JRos2ClientFactory().createClient();
        serviceClient = new JRos2ServicesFactory().createClient(ros2Client, serviceDefinition, serviceName);
        requestMessage = createNewRequest();
    }

    public void stop() {
        if (serviceClient != null) serviceClient.close();
        if (ros2Client != null) ros2Client.close();
    }

    /**
     * Format the request message with input args.
     */
    public abstract void formatServiceRequest(Object[] args, S requestMessage);

    /**
     * Create a new empty request message instance.
     */
    protected abstract S createNewRequest();

    /**
     * Call the service synchronously.
     * @param args input parameters for the request message
     * @return response message from service
     * @throws InterruptedException if waiting is interrupted
     * @throws ExecutionException if service call fails
     * @throws TimeoutException if service call times out
     */
    public T callService(Object[] args) throws InterruptedException, ExecutionException, TimeoutException {
        formatServiceRequest(args, requestMessage);
        CompletableFuture<T> responseFuture = serviceClient.sendRequestAsync(requestMessage);
        // Wait for up to 5 seconds for a response, adjust as needed
        return responseFuture.get(5, TimeUnit.SECONDS);
    }
}