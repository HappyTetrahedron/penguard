package verteiltesysteme.penguard.lowLevelNetworking;

/* Represents an UDP message. For now, it just contains a String, but for extensibility I made it its own class.
 * Later on we may expect certain properties and contents from a message that we can then pass using this object.
 */
public class Message {
    private String content;

    public Message(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString(){
        return content;
    }

    // Uses protobuf to convert the message object to an array of bytes.
    public byte[] getBytes(){
        //TODO
        return content.getBytes();
    }
}
