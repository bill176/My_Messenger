import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.security.*;
import java.util.Scanner;


public class Client{

    private static final int PORT = 6789;
    private static final String PASSWORDFILE = "/Users/user/Desktop/password.txt";

    private JFrame mainFrame;
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String serverIP;
    private Socket connection;
    private User currentUser;
    private User targetUser;
    private BinaryTreeMap root = null;

    //private JList<String> contact;

    //constructor, initializing the main chatting window that is set to invisible at first
    public Client(final String HOST){

        mainFrame = new JFrame("LiveChat Client Side");

        serverIP = HOST;

        //setting up the userText area, where the user will be able to type and send messages
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand(), true);
                userText.setText("");
            }
        });
        mainFrame.add(userText, BorderLayout.SOUTH);

        //setting up the chatWindow area, where all the messages sent by the users will be displayed
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        mainFrame.add(new JScrollPane(chatWindow), BorderLayout.CENTER);


        mainFrame.setLocationRelativeTo(null);
        mainFrame.setSize(300, 400);
        mainFrame.setVisible(false);
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //connect to server
    public void startRunning(){



        readPasswordFile(PASSWORDFILE);
        login();

        targetUser = new User(JOptionPane.showInputDialog("Please enter the user you want to talk to: "));
        showMessage("Talking to " + targetUser.getName());


        try{

            connectToServer();
            setupStreams();
            sendMessage(targetUser.getName(), false);
            whileChatting();

        }catch (EOFException eofException){
            showMessage("EOFException caught in startRunning method");
        }catch (IOException ioException){
            //ioException.printStackTrace();
            showMessage("IOException caught in startRunning method");
        }finally {
            endConnection();
            writePasswordFile(PASSWORDFILE);
        }
    }


//-----------------authentication required, online SQL server required to verify the account of the user, add login-failed situation
    //login method that fetches the user profile as well as his/her contact
    private void login(){


        JFrame loginFrame = new JFrame("Login to LiveChat!");

        loginFrame.setSize(300, 180);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);


        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(null);


        JLabel userLabel = new JLabel("Username: ");
        userLabel.setBounds(10, 10, 80, 25);
        loginPanel.add(userLabel);

        JTextField userField = new JTextField(20);
        userField.setBounds(100, 10, 160, 25);
        loginPanel.add(userField);

        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setBounds(10, 40, 80, 25);
        loginPanel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setBounds(100, 40, 160, 25);
        loginPanel.add(passwordField);

        JButton loginButton = new JButton("login");
        loginButton.setBounds(50, 80, 90, 25);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        //if the username field is empty
                        if(userField.getText().trim().isEmpty() ||
                                //if the username field contains space
                                userField.getText().contains(" ") ||
                                //if the password field is empty
                                String.valueOf(passwordField.getPassword()).trim().isEmpty() ||
                                //if the password field contains space
                                String.valueOf(passwordField.getPassword()).contains(" ")){
                            JOptionPane.showMessageDialog(null, "Illegal values!\n" +
                                    "Note that no space or empty value is allowed\n" +
                                    "in either Username or Password.");
                            return;
                        }


                        final String userName = userField.getText();
                        final String passwordHash = getSHA1(String.valueOf(passwordField.getPassword()));

                        //if the authentication succeeds...
                        if(authenticateUser(userName, passwordHash)) {

                            currentUser = new User(userName);
                            showMessage("Signing in as " + currentUser.getName());
                            sendMessage(currentUser.getName(), false);

                            loginFrame.setVisible(false);
                            mainFrame.setVisible(true);

                        //if the authentication failed...
                        }else{
                            userField.setText("");
                            passwordField.setText("");
                        }
                    }
                });
            }
        });
        loginPanel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(165, 80, 90,25);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        //create a new frame for registration

                        JFrame registerFrame = new JFrame("Register!");

                        registerFrame.setSize(300, 180);
                        registerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        registerFrame.setLocationRelativeTo(null);


                        JPanel registerPanel = new JPanel();
                        registerPanel.setLayout(null);


                        JLabel userLabel = new JLabel("Username: ");
                        userLabel.setBounds(10, 10, 80, 25);
                        registerPanel.add(userLabel);

                        JTextField userField = new JTextField(20);
                        userField.setBounds(120, 10, 160, 25);
                        registerPanel.add(userField);

                        JLabel passwordLabel = new JLabel("Password: ");
                        passwordLabel.setBounds(10, 40, 80, 25);
                        registerPanel.add(passwordLabel);

                        JPasswordField passwordField = new JPasswordField(20);
                        passwordField.setBounds(120, 40, 160, 25);
                        registerPanel.add(passwordField);

                        JLabel passwordAgainLabel = new JLabel("Retype Password:");
                        passwordAgainLabel.setBounds(10, 70, 160,25);
                        registerPanel.add(passwordAgainLabel);

                        JPasswordField passwordAgainField = new JPasswordField(20);
                        passwordAgainField.setBounds(120, 70, 160, 25);
                        registerPanel.add(passwordAgainField);

                        JButton registerButton = new JButton("Register");
                        registerButton.setBounds(105, 110, 90, 25);
                        registerButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {

                                        //if the username field is empty
                                        if(userField.getText().trim().isEmpty() ||
                                                //if the username field contains space
                                                userField.getText().contains(" ") ||
                                                //if the password field is empty
                                                String.valueOf(passwordField.getPassword()).trim().isEmpty() ||
                                                //if the password field contains space
                                                String.valueOf(passwordField.getPassword()).contains(" ") ||
                                                //if the retype password field is empty
                                                String.valueOf(passwordAgainField.getPassword()).trim().isEmpty() ||
                                                //if the retype password field contains space
                                                String.valueOf(passwordAgainField.getPassword()).contains(" ")){
                                            JOptionPane.showMessageDialog(null, "Illegal values!\n" +
                                                    "Note that no space or empty value is allowed\n" +
                                                    "in either Username or Password.");
                                            return;
                                        }


                                        //if the passwords typed match
                                        if(String.valueOf(passwordField.getPassword()).equals(String.valueOf(passwordAgainField.getPassword()))){

                                            final String userName = userField.getText();
                                            final String passwordHash = getSHA1(String.valueOf(passwordField.getPassword()));

                                            if(root == null)
                                                root = new BinaryTreeMap(userName, passwordHash);
                                            else
                                                BinaryTreeMap.insert(new BinaryTreeMap(userName, passwordHash), root);

                                            writePasswordFile(PASSWORDFILE);
                                            readPasswordFile(PASSWORDFILE);

                                            currentUser = new User(userName);
                                            showMessage("Signing in as " + currentUser.getName());
                                            sendMessage(currentUser.getName(), false);

                                            //close login and register frame and open the main chatting window
                                            registerFrame.setVisible(false);
                                            loginFrame.setVisible(false);
                                            mainFrame.setVisible(true);

                                        //if the passwords don't match
                                        }else{
                                            JOptionPane.showMessageDialog(null, "Passwords don't match!");
                                            passwordField.setText("");
                                            passwordAgainField.setText("");
                                        }
                                    }
                                });
                            }
                        });
                        registerPanel.add(registerButton);

                        registerFrame.add(registerPanel);
                        registerFrame.setVisible(true);
                    }
                });
            }
        });
        loginPanel.add(registerButton);

        loginFrame.add(loginPanel);
        loginFrame.setVisible(true);
    }


    //this method loads user names and their password hashes into the program
    private void readPasswordFile(final String FILE){

        try(Scanner input = new Scanner(new File(FILE))){

            while(input.hasNextLine()){

                final String userName = input.nextLine();

                // '#' marks the end of the file. If encountered, break
                if(userName.equals("#"))
                    break;

                final String passwordHash = input.nextLine();

                if(root == null)
                    root = new BinaryTreeMap(userName, passwordHash);
                else
                    BinaryTreeMap.insert(new BinaryTreeMap(userName, passwordHash), root);

            }

        }catch (FileNotFoundException fnfe){
            //showMessage("FileNotFoundException caught in readPasswordFile method");
        }
    }

    //this method writes all the username-password map stored in binary tree to a file
    private void writePasswordFile(final String FILE){
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE)))){

            if(root != null)
                writeSomething(out, root);

            out.write("#");
        }catch (IOException ioe){
            JOptionPane.showMessageDialog(null, "IOException caught in writePasswordFile method");
        }
    }

    //this is a recursive method called by writePasswordFile that continues to write stuff to the file
    private void writeSomething(Writer out, BinaryTreeMap toBeWritten) throws IOException{
        out.write(toBeWritten.getKey() + "\n");
        out.write(toBeWritten.getVal() + "\n");

        if(toBeWritten.getPrev() != null)
            writeSomething(out, toBeWritten.getPrev());

        if(toBeWritten.getNext() != null)
            writeSomething(out, toBeWritten.getNext());

    }

    //this method authenticates user with correct user name and password
    private boolean authenticateUser(final String userName, final String passwordHash){

        //search for the user name and its password hash...
        BinaryTreeMap target;

        //if there is not stored user at all...
        if(root == null){
            JOptionPane.showMessageDialog(null, "No such user!");

            return false;
        }else
            target = BinaryTreeMap.search(userName, root);

        //if the user name is not found...
        if(target == null) {
            JOptionPane.showMessageDialog(null, "No such user!");
            return false;

            //if found...
        }else{
            //if the password is correct...
            if(target.getVal().equals(passwordHash))
                return true;
                //if the password is incorrect...
            else{
                JOptionPane.showMessageDialog(null, "Wrong password!");
                return false;
            }
        }
    }

    //this method generates the SHA1 hashing value of the string that is passed into it
    private static String getSHA1(final String p){

        MessageDigest md = null;

        try{
            md = MessageDigest.getInstance("SHA1");
        }catch (NoSuchAlgorithmException nsae){
            JOptionPane.showMessageDialog(null, "NoSuchAlgorithmException caught in getSHA1 method");
        }

        //check for null pointer
        if(md != null)
            md.reset();
        else
            return null;

        try{
            md.update(p.getBytes("utf8"));
        }catch (UnsupportedEncodingException uee){
            JOptionPane.showMessageDialog(null, "UnsupportedEncodingException caught in getSHA1 method");
        }

        return new BigInteger(1, md.digest()).toString(16);

    }

    //connect to server
    private void connectToServer() throws IOException{
        //showMessage("Attempting connection...");
        connection = new Socket(serverIP, PORT);
        //showMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    //setup streams to send and receive messages
    private void setupStreams() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        //showMessage("Your streams are now good to go!");
    }

    //while chatting with server
    private void whileChatting() throws IOException{

        Message msg = new Message("");

        ableToType(true);
        do{
            try {
                msg = (Message) input.readObject();
            }catch (ClassNotFoundException classNotFoundException){
                showMessage("ClassNotFoundException caught in whileChatting method");
            }

            showMessage(parseMessage(msg));
        }while(!msg.getContent().equals("END"));
    }

    //close the streams and sockets
    private void endConnection(){
        showMessage("Ending the connection...");
        ableToType(false);
        try{
            output.close();
            input.close();
            connection.close();
        }catch (IOException ioException){
            ioException.printStackTrace();
        }catch (NullPointerException nullPointerException){
            showMessage("It seems that you have never connected to anyone yet.");
        }
        showMessage("Connection ended.");
    }

    //send messages to server
    private void sendMessage(final String message, final boolean visible){

        if(message.trim().isEmpty())
            return;

        Message msg = new Message(message);
        if(currentUser != null)
            msg.setSource(currentUser.getName());
        if(targetUser != null)
            msg.setDestination(targetUser.getName());
        if(visible)
            showMessage(parseMessage(msg));

        try{
            output.writeObject(msg);
            output.flush();

        }catch (IOException ioException){
            showMessage("Something messed up when sending message");
        }
    }

    //parses a Message object to a string
    private String parseMessage(final Message msg){
        return String.format("%s--%s - %s",msg.getTime(), msg.getSource(), msg.getContent());
    }

    //change/update chatWindow
    private void showMessage(final String m){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatWindow.append(m + "\n");
            }
        });
    }

    //give user permission to type something into the text box
    private void ableToType(final boolean tof){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                userText.setEditable(tof);
            }
        });
    }

    //main method
    public static void main(String[] args) {

        Client c = new Client("127.0.0.1");
        c.startRunning();
    }
}