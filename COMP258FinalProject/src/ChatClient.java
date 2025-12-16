//The ChatClient class extends the AbstractClient class and provides features for interacting with the server
//It handles both plain and structured messages like Envelope
//processes commands from the user
// and handles communication between the ChatIF and the server  
import java.io.*;
import java.util.ArrayList;

/**
 * This class overrides some of the methods defined in the abstract superclass
 * in order to give more functionality to the client.
 */
public class ChatClient extends AbstractClient {
    //Instance variables **********************************************

    /**
     * The interface type variable. It allows the implementation of the display
     * method in the client.
     */
    ChatIF clientUI;

    //Constructors ****************************************************
    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @param clientUI The interface type variable.
     */
    public ChatClient(String host, int port, ChatIF clientUI)
            throws IOException {
        super(host, port); //Call the superclass constructor
        this.clientUI = clientUI;
        //openConnection();
    }

    //Instance methods ************************************************
    /**
     * This method handles all data that comes in from the server.vi
     *
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg) {
        if(msg instanceof Envelope)
        {
            Envelope env = (Envelope)msg;
            handleCommandFromServer(env);
        }
        else
        {
            clientUI.display(msg.toString());
        }
    }
    
    public void handleCommandFromServer(Envelope env)
    {
        //command: who
        //arg:
        //data: ArrayList<String> - list of all clients in the room
        if(env.getCommand().equals("who"))
        {
            ArrayList<String> clients = (ArrayList<String>)env.getData();
            
            System.out.println("--- Printing out all clients on The List ---");
            
            //loop through the array list
            for(int i = 0; i < clients.size(); i++)
            {
                //for an array list .get(i) works like [i] for an array
                System.out.println(clients.get(i));
            }
        }
    }
    
    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(String message) {

        //if first character is #, handle it as a command
        if (message.charAt(0) == '#') {

            handleClientCommand(message);

        } else {
            try {
                sendToServer(message);
            } catch (IOException e) {
                clientUI.display("Could not send message to server.  Terminating client.......");
                quit();
            }
        }
    }
    //The provided code block for handleMessageFromClientUI(Envelope envelope)
    //this is an overloaded method in my ChatClient class
    //It handles Envelope objects as input and sends them to the server
     public void handleMessageFromClientUI(Envelope envelope) {
        try {
            sendToServer(envelope); // Send the Envelope object to the server
        } catch (IOException e) {
            clientUI.display("Could not send envelope to server: " + e.getMessage());
        }
    }

    /**
     * This method terminates the client.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
        }
        System.exit(0);
    }

    public void connectionClosed() {

        System.out.println("Connection closed");

    }

    protected void connectionException(Exception exception) {

        System.out.println("Server has shut down");

    }

    /**
     * Extends the hook method from AbstractClient. Prints out a message when 
     * client successfully connects to server
     */
    protected void connectionEstablished(){
        System.out.println("Connected to server at "+ getHost() + " on port "+getPort());
    }
    
    public void handleClientCommand(String message) {

        if (message.equals("#quit")) {
            clientUI.display("Shutting Down Client");
            quit();

        }

        if (message.equals("#logoff")) {
            clientUI.display("Disconnecting from server");
            try {
                closeConnection();
            } catch (IOException e) {
            };

        }

        //#setHost localhost
        if (message.indexOf("#setHost") == 0) {

            if (isConnected()) {
                clientUI.display("Cannot change host while connected");
            } else {
                setHost(message.substring(9, message.length()));
            }

        }

        if (message.indexOf("#setPort") == 0) {

            if (isConnected()) {
                clientUI.display("Cannot change port while connected");
            } else {
                //setPort()
                //Integer.parseInt()
                //message.substring(9, message.length())
                //#setPort 5556
                //"5556"
                
                //String portNum = message.substring(8,message.length()).trim();
                //portNum = portNum.trim();
                setPort(Integer.parseInt(message.substring(9, message.length())));
            }

        }

        if (message.equals("#login")) {

            if (isConnected()) {
                clientUI.display("already connected");
            } else {
                try {
                    openConnection();
                } catch (IOException e) {
                    clientUI.display("failed to connect to server.");
                }
            }
        }
        
    
    }

}
//End of ChatClient class
