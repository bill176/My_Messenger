import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server{

    private static final int PORT = 6789;

    static BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    private JFrame mainFrame;
    private JTextField userText;
    static JTextArea chatWindow;
    private ServerSocket server;

    public Server(){
        mainFrame = new JFrame("LiveChat Server Side");

        //setting up userText
        userText = new JTextField();
        userText.setEditable(false);
        mainFrame.add(userText, BorderLayout.SOUTH);

        //setting up chatWindow
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        mainFrame.add(new JScrollPane(chatWindow), BorderLayout.CENTER);

        //setting up the mainWindow
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(300, 400);
        mainFrame.setVisible(true);

    }

    private void startRunning(){
        try{
            server = new ServerSocket(PORT);

            chatWindow.append("-------------Message Log-------------\n");

            while(true) new ClientThread(server.accept()).start();

        }catch(IOException ioexception){
            showMessage("IOException caught in the startRunning method");
        }
    }

    void showMessage(final String str){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                chatWindow.append(str + "\n");
            }
        });
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.startRunning();
    }


}
