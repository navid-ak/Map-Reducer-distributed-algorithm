package Events;

import Components.Node;
import misc.Edge;
import se.sics.kompics.Init;
import se.sics.kompics.KompicsEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class WaveMessage implements KompicsEvent {
    public String src;
    public String dst;
    public ArrayList<Edge> edgeInfo;
    public boolean isAnswer;
    public String initiator;


    public WaveMessage(String src, String dst, String initiator, ArrayList<Edge> edgeInfo, boolean isAnswer) {
        this.src = src;
        this.dst = dst;
        this.initiator = initiator;
        this.edgeInfo = edgeInfo;
        this.isAnswer = isAnswer;
    }
}