
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
     * This method handles all data that comes in from the server.
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
