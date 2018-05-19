import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;

/**
 * Created by billzhang on 2017-05-26.
 */
public class ClientThread extends Thread {

    private String to = null;
    private String from = null;

    private final Socket connection;

    public ClientThread(final Socket connection){
        this.connection = connection;
    }

    @Override
    public void run() {

        try(
                //setting up streams
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(connection.getInputStream())
        ){

            Message msg1 = null, msg2 = null;

            //read the first two messages from the server that contains information about the target user and the source user
            try{
                msg1 = (Message) input.readObject();
                msg2 = (Message) input.readObject();
            }catch (ClassNotFoundException cnfe){}

            if(msg1 != null)
                from = msg1.getContent();
            else
                JOptionPane.showMessageDialog(null, "null pointer exception when assigning variable name");

            if(msg2 != null)
                to = msg2.getContent();
            else
                JOptionPane.showMessageDialog(null, "null pointer exception when assigning variable name");

            //create a new thread that keeps track on the messageQueue
            new Thread() {
                @Override
                public void run() {
                    while(true){
                        //iterate through the messageQueue to check for messages destined for current user
                        Iterator<Message> it = Server.messageQueue.iterator();
                        while(it.hasNext()){
                            Message m = it.next();
                            if(m.getDestination().equals(to) && m.getSource().equals(from)){
                                try{
                                    output.writeObject(m);
                                    output.flush();
                                }catch (IOException ioe){
                                    //JOptionPane.showMessageDialog(null, "IOException caught in messageChecking");
                                }
                                it.remove();
                            }
                        }
                        try{
                            Thread.sleep(500);
                        }catch (InterruptedException ie){}
                    }
                }
            }.start();

            Message msg = null;

            //start chatting
            do{
                try{
                    //reading messages from the input
                    msg = (Message) input.readObject();

                    //appending the message received to the messageQueue
                    Server.messageQueue.put(msg);
                    Server.chatWindow.append(msg.getTime() + "---" + msg.getSource() + " -> " + msg.getDestination() + " : " + msg.getContent() + "\n");

                }catch (Exception e){}
                //catch (ClassNotFoundException cnfe){}
                //catch (InterruptedException ie){}

            }while(!msg.getContent().equals("END"));

            //finally close the connection
            connection.close();

        }catch (IOException ioe){
            //JOptionPane.showMessageDialog(null, "IOException caught in run method");
        }
    }
}
