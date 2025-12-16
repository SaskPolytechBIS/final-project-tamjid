/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.io.File;                        //added import
import java.util.ArrayList;                 //added import
import java.nio.file.Files;                 //added import

/**
 *
 * @author Instructor
 */
public class ClientGUI extends JFrame implements ChatIF {

    /**
     * The instance of the client that handles the communication for the GUI
     */
    ChatClient client;
    
    /**
     * The default port to connect on.
     */
    final public static int DEFAULT_PORT = 5555;
    
    /**
     * JFrame Buttons.
     */
    private JButton logoffB = new JButton("Logoff");
    private JButton loginB = new JButton("Login");
    private JButton sendB = new JButton("Send");
    private JButton quitB = new JButton("Quit");
    // Add new buttons
    private JButton browseB = new JButton("Browse");
    private JButton saveB = new JButton("Save");
    private JButton downloadB = new JButton("Download");
    
    // Add a JComboBox to display the list of files on the server
    //JcomboBox is another swing GUI component in java 
    // it provides a drop-down menu that lets user choose one time from a list
    private JComboBox<String> fileComboBox = new JComboBox<>();
    
    // variable to hold the selected file
    //this line declares a private instance variable named selectedFile of type File.
    //an instance variable is a variable that belongs to an object which is an instance of a class.
    private File selectedFile = null;

    /**
     * JFrame Text Fields.
     */
    private JTextField portTxF = new JTextField("5555");
    private JTextField hostTxF = new JTextField("127.0.0.1");
    private JTextField userIdTxF = new JTextField("");
    private JTextField messageTxF = new JTextField("");

    /**
     * JFrame Labels.
     */
    private JLabel portLB = new JLabel("Port: ", JLabel.RIGHT);
    private JLabel hostLB = new JLabel("Host: ", JLabel.RIGHT);
    private JLabel userIdLB = new JLabel("UserId: ", JLabel.RIGHT);
    private JLabel messageLB = new JLabel("Message: ", JLabel.RIGHT);

    /**
     * The main text area where messages are displayed
     */
    private JTextArea messageList = new JTextArea();

    public static void main(String[] args)
    {
        String host = "localhost";
        int port = DEFAULT_PORT;  //The port number
        
        //create and display the GUI using the default values for host and port as placeholders we created an instance of this class
        ClientGUI chat = new ClientGUI(host, port);
    }
    
    public ClientGUI(String host, int port) {

        //create the GUI Window
        super("Simple Chat GUI");
        setSize(400, 500);              //widht400 ; height 500

        //setup the JFrame layout
        setLayout(new BorderLayout(5, 5));

        //Because only one item can go in each area of a JFrame layout, we create
        //the JPanel Bottom and add our elements into that, then add that to the
        //JFrame layout
        JPanel bottom = new JPanel();
        add("Center", messageList);
        add("South", bottom);

        //Set the Layout of the buttons to be a grid
        //see changing what each of this values does 
        bottom.setLayout(new GridLayout(9, 2, 5, 5));
        bottom.add(hostLB); //how does these button gets ordered as we create item it will get ordered by the gridlayout order
        bottom.add(hostTxF); //so host lb will first populate the 
        bottom.add(portLB);
        bottom.add(portTxF);
        bottom.add(userIdLB);
        bottom.add(userIdTxF);
        bottom.add(messageLB);
        bottom.add(messageTxF);
        bottom.add(new JLabel("File List: ", JLabel.RIGHT));
        bottom.add(fileComboBox);               // ComboBox to display the list of files
        bottom.add(loginB);
        bottom.add(sendB);
        bottom.add(logoffB);
        bottom.add(quitB);
        // Add new components to the bottom JPanel
        bottom.add(browseB);                    // Button to select a file to upload
        bottom.add(saveB);                      // Button to upload the selected file

        bottom.add(downloadB);                  // Button to download the selected file

        //an isntance of a command 
        
        //what happens when you hit the button
        //the only thing we are going to change is what goes in button
        loginB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String userId = userIdTxF.getText();  //what ever chages we will do it will happen here
                
                if(userId.equals(""))
                {
                   display("You must enter a User Id to log in");
                }
                else
                {
                    display("Logging in as "+ userId);
                    send("#login");
                    send("#setName "+userId);
                }
                
            }
        });
        
        sendB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                // send is our core function
                send(messageTxF.getText());
            }
        });
        
        logoffB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display("Logging off server");
                send("#logoff");
            }
        });
      
        // This code block opens a JFileChooser dialog allowing the user to browse and select a file for uploading
        // The selected file is stored in 'selectedFile', and its path is displayed in the message list
        browseB.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                // Open a JFileChooser dialog for selecting a file
                JFileChooser fileChooser = new JFileChooser();

                // Show the dialog and store the user's action (e.g., approve or cancel)
                int result = fileChooser.showOpenDialog(ClientGUI.this);

                // If the user selected a file and clicked "Open"
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile(); // Store the selected file in the variable
                    display("Selected file: " + selectedFile.getAbsolutePath()); // Display the file path to the user
                } else {
                    // If the user canceled the selection
                    display("File selection cancelled.");
                }
            }
        });
        // the saveB button uploads a file to the server
        //It checks if a file has been selected 
        //If valid the file is read as a byte array
        //An Envelope is created containing the #ftpUpload command, file name, and file data
        //The Envelope is sent to the server using handleMessageFromClientUI
        
        
        // This code block uploads the selected file to the server when the Save button is clicked
        // It reads the file's contents, wraps it in an Envelope, and sends it to the server
        saveB.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                // Check if a file has been selected
                if (selectedFile != null) {
                    try {
                        // Read the file contents into a byte array
                        byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());

                        // Create an Envelope with the #ftpUpload command, file name, and file data
                        Envelope env = new Envelope("#ftpUpload", selectedFile.getName(), fileBytes);

                        // Send the Envelope to the server
                        client.handleMessageFromClientUI(env);

                        // Display a success message to the user
                        display("File uploaded: " + selectedFile.getName());
                    } catch (IOException ioException) {
                        // Handle any errors that occur during file reading or upload
                        display("Error reading or uploading file: " + ioException.getMessage());
                    }
                } else {
                    // No file selected: Notify the user
                    display("No file selected to upload.");
                }
            }
        });
        
        // This code block handles the Download button, allowing the user to request and download a file from the server
        // The selected file name is retrieved from the fileComboBox, and the #ftpget command is sent to the server
        downloadB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the selected file name from the fileComboBox (drop-down menu)
                String selectedFile = (String) fileComboBox.getSelectedItem();

                // If no file is selected, notify the user
                if (selectedFile != null) {
                    // Send the download command (#ftpget) along with the selected file name to the server
                    client.handleMessageFromClientUI("#ftpget " + selectedFile);

                    // Inform the user that the download request is being processed
                    display("Requesting download of file: " + selectedFile);
                } else {
                    // If no file is selected from the dropdown, display an error message
                    display("No file selected for download.");
                }
            }
        });      
        
        quitB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send("#quit");
            }
        });

        //create ChatClient to handle messages
        try {
            client = new ChatClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection!!!!"
                    + " Terminating client.");
            System.exit(1);
        }
        
        //Display the window to the user. This should be the last step
        setVisible(true);
    }
    
    /**
     * Send a text message to the ChatClient to process 
     */
    public void send(String message)
    {
        
        //whatever we type in the send button we pass on to this
        client.handleMessageFromClientUI(message);
    }
    
    /**
     * Displays information to the user using the messageList JTextArea
     */
    //
    public void display(String message) {
        //add the "\n" so the messages dont stack on a single line
        //the 0 means we want it at the first line or top
        //why do i have the /n i there
        //whjat happens when i change the numbr
        messageList.insert(message+"\n", 0);
    }
}
