package Events;

import se.sics.kompics.KompicsEvent;

public class InitialMessage implements KompicsEvent {
    public String src;
    public String dst;
    public boolean isAnswer;
    public boolean isLeaf;
    public int numOfLeaf;


    public InitialMessage(String src, String dst, boolean isLeaf, boolean isAnswer, int numOfLeaf) {
        this.src = src;
        this.dst = dst;
        this.isAnswer = isAnswer;
        this.isLeaf = isLeaf;
        this.numOfLeaf = numOfLeaf;
    }
}
