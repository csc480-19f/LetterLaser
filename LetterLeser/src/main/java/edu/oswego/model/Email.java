import edu.oswego.model.EmailAddress;
import edu.oswego.model.SentimentScore;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Email object based on SQL table model: email
 * 
 * @author Jimmy Nguyen
 * @since 10/28/2019
 *
 */

public class Email implements Serializable, Comparable {

    private int id;
    private Timestamp dateReceived;
    private String subject;
    private double size;
    private boolean isSeen;
    private boolean hasAttachment;

    private SentimentScore sentimentScore;
    private String folder;
    private List<EmailAddress> from;

    // TODO ADD USERFOLDER?

    public Email(byte [] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        this.id = bb.getInt();
        this.dateReceived = new Timestamp(bb.getLong());
        int subjectLength = bb.getInt();
        char [] subjectChars = new char[subjectLength];
        for(int i = 0; i < subjectLength; i++){
            subjectChars[i] = bb.getChar();
        }
        this.subject = new String(subjectChars);
        this.size = bb.getDouble();
        this.isSeen = (bb.getInt() == 1);
        this.hasAttachment = (bb.getInt() == 1);
        double pos = bb.getDouble();
        double neg = bb.getDouble();
        double neu = bb.getDouble();
        double cpd = bb.getDouble();
        this.sentimentScore = new SentimentScore(pos,neg,neu,cpd);
        int folderLength = bb.getInt();
        char [] folderChars = new char[folderLength];
        for(int i = 0; i < folderLength; i++){
            folderChars[i] = bb.getChar();
        }
        this.folder = new String(folderChars);
        this.from = new ArrayList<>();
        int fromSize = bb.getInt();
        for(int i = 0; i < fromSize; i++){
            int id = bb.getInt();
            int emailLength = bb.getInt();
            char [] emailChars = new char[emailLength];
            for(int j = 0; j < emailLength; j++){
                emailChars[i] = bb.getChar();
            }
            String email = new String(emailChars);
            from.add(new EmailAddress(id,email));
        }
    }

    public Email(int id, Timestamp dateReceived, String subject, double size, boolean isSeen, boolean hasAttachment,
                 String folder) {
        this.id = id;
        this.dateReceived = dateReceived;
        this.subject = subject;
        this.size = size;
        this.isSeen = isSeen;
        this.hasAttachment = hasAttachment;
        this.sentimentScore = null;
        this.folder = folder;
        this.from = new ArrayList<>();
    }

    public Email(int id, Timestamp dateReceived, String subject, double size, boolean isSeen, boolean hasAttachment,
                 SentimentScore sentimentScore) {
        this.id = id;
        this.dateReceived = dateReceived;
        this.subject = subject;
        this.size = size;
        this.isSeen = isSeen;
        this.hasAttachment = hasAttachment;
        this.sentimentScore = sentimentScore;
        this.from = new ArrayList<>();
    }

    public Email(int id, Timestamp dateReceived, String subject, double size, boolean isSeen, boolean hasAttachment,
                 SentimentScore sentimentScore, String folder) {
        this.id = id;
        this.dateReceived = dateReceived;
        this.subject = subject;
        this.size = size;
        this.isSeen = isSeen;
        this.hasAttachment = hasAttachment;
        this.sentimentScore = sentimentScore;
        this.folder = folder;
        this.from = new ArrayList<>();
    }

    public Email(int id, Timestamp dateReceived, String subject, double size, boolean isSeen, boolean hasAttachment,
                 SentimentScore sentimentScore, String folder, List<EmailAddress> from) {
        this.id = id;
        this.dateReceived = dateReceived;
        this.subject = subject;
        this.size = size;
        this.isSeen = isSeen;
        this.hasAttachment = hasAttachment;
        this.sentimentScore = sentimentScore;
        this.folder = folder;
        this.from = from;
    }

    public String getTimestamp() {
        return dateReceived.toString().substring(0, dateReceived.toString().length() - 2);
    }

    public int getId() {
        return id;
    }

    public Timestamp getDateReceived() {
        return dateReceived;
    }

    public String getSubject() {
        return subject;
    }

    public double getSize() {
        return size;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public boolean hasAttachment() {
        return hasAttachment;
    }

    public SentimentScore getSentimentScore() {
        return sentimentScore;
    }

    public String getFolder() {
        return folder;
    }

    public List<EmailAddress> getFrom() {
        return from;
    }

    private int calculateNumBytes(){
        int subjectSize = 4 + (2 * subject.length()); // for length (int) and 2*length (chars)
        int folderSize = 4 + (2 * folder.length()); // for length (int) and 2*length (chars)
        int sentimentScoreSize = 8*4; //4 doubles
        int fromSize = 4; //int for the length of the list
        for(EmailAddress ea : from){
            fromSize += 8; //for the id and length (both int)
            fromSize += (2 * ea.getEmailAddress().length());
        }

        return 4 // id (int)
                + 8 // dateReceived (TimeStamp, can be constructed with long)
                + subjectSize
                + 8 // size (double)
                + 4 // isSeen (boolean, stored as int)
                + 4 // hasAttachment (boolean, stored as int)
                + sentimentScoreSize
                + folderSize
                + fromSize;
    }

    public byte[] toBytes(){
        int numBytes = calculateNumBytes();
        ByteBuffer bb = ByteBuffer.wrap(new byte[numBytes]);

        bb.putInt(id);
        bb.putLong(dateReceived.getTime());
        bb.putInt(subject.length());
        char [] subjectChars = subject.toCharArray();
        for(char c : subjectChars) bb.putChar(c);
        bb.putDouble(size);
        bb.putInt((isSeen)? 1 : 0);
        bb.putInt((hasAttachment)? 1 : 0);
        bb.putDouble(sentimentScore.getPositive());
        bb.putDouble(sentimentScore.getNegative());
        bb.putDouble(sentimentScore.getNeutral());
        bb.putDouble(sentimentScore.getCompound());
        bb.putInt(folder.length());
        char [] folderChars = folder.toCharArray();
        for(char c : folderChars) bb.putChar(c);
        bb.putInt(from.size());
        for(EmailAddress ea : from){
            bb.putInt(ea.getId());
            bb.putInt(ea.getEmailAddress().length());
            char [] addressChars = ea.getEmailAddress().toCharArray();
            for(char c : addressChars) bb.putChar(c);
        }

        return bb.array();
    }

    @Override
    public int compareTo(Object o) {
        Email e = (Email) o;
        if(this.getDateReceived().getTime() > e.getDateReceived().getTime()) {
            return 1;
        }else if(this.getDateReceived().getTime() == e.getDateReceived().getTime()) {
            return 0;
        }else {
            return -1;
        }
    }
}
