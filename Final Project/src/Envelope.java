//The Envelope class is a serializable container 
//used to encapsulate a comman, argument (arg), and data
// to communicate between the client and server.


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.Serializable;
//Serializable is a interface in Java that enables an object to be converted into a stream of bytes
//which allows it to be sent over a network or saved to a file
//and later deserialized back into the same object.
/**
 *
 * @author Micha
 */
public class Envelope implements Serializable{ //by implementing serializable code enables envelope to be transmitted
    private String command; //the name of the command
    private String arg; //an argument needed to complete the command (optional)
    private Object data; //the data required for the command (optional)

    public Envelope() {
    }

    public Envelope(String command, String arg, Object data) {
        this.command = command;
        this.arg = arg;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    
    
}
