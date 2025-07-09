/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package id.jrosmessages.std_msgs;

import id.jrosmessages.Message;
import id.jrosmessages.MessageMetadata;
import id.jrosmessages.RosInterfaceType;

@MessageMetadata(name = "std_msgs/Float32", interfaceType = RosInterfaceType.MESSAGE, fields = { "data" })
public class Float32Message implements Message {
    public float data;

    public Float32Message() {
    }

    public Float32Message(float data) {
        this.data = data;
    }
}

/*
package id.jrosmessages.std_msgs;

import id.jrosmessages.Message;
import id.xfunction.XJsonStringBuilder;

public class Float32Message implements Message {
    public float data;

    public Float32Message() {}
    public Float32Message(float data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new XJsonStringBuilder("Float32Message")
            .append("data", data)
            .toString();
    }
}
*/