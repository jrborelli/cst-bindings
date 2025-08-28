/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.unicamp.cst.bindings.ros2java;
import troca_ros.AddTwoIntsRequestMessage;
import troca_ros.AddTwoIntsResponseMessage;
import troca_ros.AddTwoIntsServiceDefinition;

/**
 *
 * @author jrborelli
 */


public class AddTwoIntsServiceClientSyncRos2 extends RosServiceClientSync<AddTwoIntsRequestMessage, AddTwoIntsResponseMessage> {

    public AddTwoIntsServiceClientSyncRos2(String serviceName) {
        super(serviceName, new AddTwoIntsServiceDefinition());
    }

    @Override
    public void formatServiceRequest(Object[] args, AddTwoIntsRequestMessage requestMessage) {
        requestMessage.withA((Long) args[0]).withB((Long) args[1]);
    }

    @Override
    protected AddTwoIntsRequestMessage createNewRequest() {
        return new AddTwoIntsRequestMessage();
    }
}
