import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by billzhang on 2017-05-23.
 */

public class Message implements Serializable{

    private final String content;
    private String source;
    private String destination;
    private final String time;

    public Message(String content){
        this.content = content;

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        time = df.format(cal.getTime());
    }

    public String getContent(){return content;}

    public String getTime(){return time;}

    public String getSource(){return source;}

    public String getDestination(){return destination;}

    public void setSource(String source){
        this.source = source;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }
}
