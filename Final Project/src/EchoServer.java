import java.io.File;            // For file handling to creat and access file
import java.nio.file.Files;     // For working with file content like readAllBytes
import java.nio.file.Paths;     // For creating file paths
import java.io.IOException;     // For handling file related exceptions
import java.util.ArrayList;     // For handling the ArrayList of file names

public class EchoServer extends AbstractServer {
    //Class variables *************************************************

    /**
     * The default port to listen on.
     */
    final public static int DEFAULT_PORT = 5555;

    //Constructors ****************************************************
    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {

        super(port);

        try {
            this.listen(); //Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }

    }

    //Instance methods ************************************************
    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if(msg instanceof Envelope)
        {
            Envelope env = (Envelope) msg;
            //handle command
            handleCommandFromClient(env, client);
        }
        else
        {
            System.out.println("Message received: " + msg + " from " + client);
            
            
            //get the name of the room the sending client is in
            String room = (String)client.getInfo("room");
            
            //if the client has a user ID add it to their messages
            if(client.getInfo("UserId") != null)
            {
                this.sendToAllClientsInRoom(client.getInfo("UserId") + ": " +msg, room);
            }
            else
            {
                this.sendToAllClientsInRoom(msg,room);
            }
        }
    }
    
    public void handleCommandFromClient(Envelope env, ConnectionToClient client)
    {
        //command: setName
        //arg: 
        //data: String - new name for client
        if(env.getCommand().equals("setName"))
        {
            String userId = (String) env.getData();
            client.setInfo("UserId", userId);
        }
        
        //command: join
        //arg:
        //data: String - name for room the user wants to join
        if(env.getCommand().equals("join"))
        {
            String room = (String)env.getData();
            client.setInfo("room", room);
            if(client.getInfo("UserId") != null)
            {
                String UserId = (String)client.getInfo("UserId");
                System.out.println("<"+UserId+" has joined room "+room+">");
            }
            else
            {
                System.out.println("<User has joined room "+room+">");
            }
        }
        
        //command: pm
        //arg: target for the pm
        //data: String - text of the pm
        if(env.getCommand().equals("pm"))
        {
            String target = env.getArg();
            String text = (String)env.getData();
            
            sendToClientByUserId(text,target);
        }
        
        if(env.getCommand().equals("who"))
        {
            //find out what room the person who sent the command is in
            String room = (String)client.getInfo("room");
            
            //find all users in the same room and store in an ArrayList<String>
            ArrayList<String> clientList = getAllClientsInRoom(room);
            
            //create an envelope with an Id of "who"
            //add the arraylist as the data
            Envelope returnEnv = new Envelope();
            returnEnv.setCommand("who");
            returnEnv.setData(clientList);
            
            //use the sendToClient function from ConnectionToClient to send the envelope
            //back to the client who sent the command
            try{
                client.sendToClient(returnEnv);
            }
            catch(Exception e)
            {
                System.out.println("Something went wrong when trying to send who return envelope");
                e.printStackTrace();
            }
        }
        
        // this block handles the "#ftpUpload" command sent by the client
        // The client sends a file wrapped in an Envelope object with `#ftpUpload` as the command, 
        // the filename as argument, and the file's content as a byte array in the data field
        // The server saves the file into an "uploads" directory for future use  listing, downloading
        
        if (env.getCommand().equals("#ftpUpload")) //checks if the command in the Envelope object is #ftpUpload.
        {
            // Extract the filename from the envelope's "arg" field
            String filename = env.getArg();     //arg is a string argument often the filename

            // Extract file data
            //expected to be a byte[] from the "data" field of the envelope
            Object data = env.getData();

            // Validate the filename and data
            // Check if either the filename is null or the data is null or if the data is not a byte array
            if (filename == null || data == null || !(data instanceof byte[])) 
            {
                System.out.println("Invalid #ftpUpload command: Missing or incorrect arguments.");
                return; // If validation fails, print an error message and exit this block
            }

            // If validation passes, cast the Object `data` into a byte array
            byte[] fileBytes = (byte[]) data;
            
            // synchronized is a keyword in Java used to control thread access to blocks of code.
            //Synchronization ensures that multiple clients 
            //uploading files simultaneously won’t overwrite or interfere with each other's file operations.
            // Synchronize the file-writing process to avoid error during concurrent uploads
            synchronized (this) {
                try {
                    // Ensure the "uploads" directory exists. If not create it.
                    File uploadsDir = new File("uploads");
                    uploadsDir.mkdirs(); //  create the directory if it doesn't already exist
                    
                    //Converts the fileBytes into a physical file using the Files.write method.
                    //The file is saved in the uploads directory under the given filename.
                    // Write the received byte array data to a new file with the given filename in the "uploads" directory
                    Files.write(Paths.get("uploads/" + filename), fileBytes);

                    // Log a success message in the server's console
                    System.out.println("File uploaded successfully: " + filename);
                } catch (IOException ioException) {
                    // Handle any IO exceptions that occur during the file writing process
                    System.out.println("Error saving uploaded file: " + ioException.getMessage());
                }
            }
        }
        
        // The "#ftplist" command allows the client to request a list of files stored in the server's "uploads" directory
        //  The server responds by
        //// 1. Checking if the "uploads" directory exists, creating it if necessary
        //// 2. Listing all files inside the "uploads" directory
        //// 3. Sending the list of file names back to the client in an Envelope object

        if (env.getCommand().equals("#ftplist"))    //checks if the Envelope contains the #ftplist command from the client
        {
            // Create a reference to the "uploads" directory
            File uploadsDir = new File("uploads");

            // Check if the "uploads" directory exists; if not, create it to ensure it's ready for listing
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs(); // Create the directory (and parent directories if needed)
            }

            // List all files in the "uploads" directory
            File[] files = uploadsDir.listFiles();

            // Create an ArrayList to hold the file names
            ArrayList<String> fileNames = new ArrayList<>();

            // If the directory is not empty, retrieve the names of each file
            if (files != null) {
                for (File file : files) {
                    fileNames.add(file.getName()); // Add each file's name to the list
                }
            }

            // Package the file list into an Envelope object to send back to the client
            Envelope listResponse = new Envelope("#ftplist", null, fileNames);

            // Attempt to send the Envelope to the client
            try {
                client.sendToClient(listResponse); // Sends the file list back to the client
            } catch (IOException ioException) {
                // Handle errors that occur while attempting to send the response
                System.out.println("Error sending file list to client: " + ioException.getMessage());
            }
        }
        
        // The "#ftpget" command allows the client to download a specific file from the server's "uploads" directory
        //  The server performs the following:
        // 1. Extracts the filename from the Envelope's "arg" field
        // 2. Checks if the file exists in the "uploads" directory
        // 3. If the file exists, reads its content into a byte array and sends it in an Envelope to the client
        // 4. If the file doesn't exist, sends an error message to the client

        if (env.getCommand().equals("#ftpget"))         //Checks if the Envelope contains the #ftpget command from the client
        {
            // Extract the filename from the "arg" field in the envelope
            String filename = env.getArg();

            // Validate the filename, otherwiselog an error and stop processing
            if (filename == null) {
                System.out.println("Invalid #ftpget command: No filename provided.");
                return; // Exit if the filename is missing
            }

            // Create a reference to the file to be downloaded
            File file = new File("uploads/" + filename);

            // Check if the file exists in the "uploads" directory
            if (file.exists()) {
                try {
                    // Read the file's contents into a byte array
                    byte[] fileBytes = Files.readAllBytes(file.toPath());

                    // Package the file's content into an Envelope and send it to the client
                    Envelope fileResponse = new Envelope("#ftpget", filename, fileBytes);
                    client.sendToClient(fileResponse); // Send the file content

                } catch (IOException ioException) {
                    // Handle errors that occur while reading the file
                    System.out.println("Error reading file for download: " + ioException.getMessage());
                    try {
                        // Notify the client about the file reading error
                        client.sendToClient(new Envelope("#ftpget", filename, "Error reading file."));
                    } catch (IOException ex) {
                        System.out.println("Failed to notify client about file reading error: " + ex.getMessage());
                    }
                }
            } else {
                // If the file doesn't exist, notify the client
                try {
                    client.sendToClient(new Envelope("#ftpget", filename, "File not found"));
                } catch (IOException ioException) {
                    System.out.println("Error notifying client about missing file: " + ioException.getMessage());
                }
            }
        }        
    }

    public ArrayList<String> getAllClientsInRoom(String room)
    {
        ArrayList<String> result = new ArrayList<String>();
        
        //get array of all clients
        Thread[] clientThreadList = getClientConnections();

        //loop through all clients
        for (int i = 0; i < clientThreadList.length; i++) {
            //cast the client thread as a connectionToClient object
            ConnectionToClient currClient = ((ConnectionToClient) clientThreadList[i]);
            
            //before we add client to list, make sure it is in the specified room
            if(room.equals(currClient.getInfo("room")))
            {
                //check if the currClient has a UserId
                if(currClient.getInfo("UserId")!= null)
                {
                    //add user id to the list
                    result.add( (String)currClient.getInfo("UserId") );
                }
            }
        }
        return result;
    }
    
    /**
     * Send message to all clients in specified room
     * @param msg - The message to send
     * @param room - The room to send to
     */
    public void sendToAllClientsInRoom(Object msg, String room) {
        //get array of all clients
        Thread[] clientThreadList = getClientConnections();

        //loop through all clients
        for (int i = 0; i < clientThreadList.length; i++) {
            //cast the client thread as a connectionToClient object
            ConnectionToClient currClient = ((ConnectionToClient) clientThreadList[i]);
            
            //before we send to a client, make sure it is in the specified room
            if(room.equals(currClient.getInfo("room")))
            {
                try {
                    //send message to client
                    currClient.sendToClient(msg);
                } catch (Exception ex) {
                }
            }
        }
    }
    
    public void sendToClientByUserId(Object msg, String target) {
        //get array of all clients
        Thread[] clientThreadList = getClientConnections();

        //loop through all clients
        for (int i = 0; i < clientThreadList.length; i++) {
            //cast the client thread as a connectionToClient object
            ConnectionToClient currClient = ((ConnectionToClient) clientThreadList[i]);
            
            //make sure the client has a user id before trying to read it
            if(currClient.getInfo("UserId") != null)
            {
                //before we send to a client, make sure it is in the specified room
                if(target.equals(currClient.getInfo("UserId")))
                {
                    try {
                        //send message to client
                        currClient.sendToClient(msg);
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }
    
    /**
     * This method overrides the one in the superclass. Called when the server
     * starts listening for connections.
     */
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * stops listening for connections.
     */
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
    }

    //Class methods ***************************************************
    /**
     * This method is responsible for the creation of the server instance (there
     * is no UI in this phase).
     *
     * @param args[0] The port number to listen on. Defaults to 5555 if no
     * argument is entered.
     */
    public static void main(String[] args) {
        int port = 0; //Port to listen on
        
        try
        {
            port = Integer.parseInt(args[0]);
        }
        catch(ArrayIndexOutOfBoundsException aioobe)
        {
            port = DEFAULT_PORT; //Set port to 5555
        }
            
        EchoServer sv = new EchoServer(port);

        try {
            sv.listen(); //Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }

    }

    protected void clientConnected(ConnectionToClient client) {

        System.out.println("<Client Connected:" + client + ". Placing them in room commons>");
        client.setInfo("room", "commons");

    }
    
    synchronized protected void clientException(
            ConnectionToClient client, Throwable exception) {
        System.out.println("<Client has disconnected>");
    }
}
//End of EchoServer class
