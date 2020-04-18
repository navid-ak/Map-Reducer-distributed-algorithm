package Events;

import se.sics.kompics.KompicsEvent;

import java.util.ArrayList;

public class ReduceMessage implements KompicsEvent {
    public String src;
    public String dst;
    public ArrayList<String> data;

    public ReduceMessage(String src, String dst, ArrayList<String> data) {
        this.src = src;
        this.dst = dst;
        this.data = data;
    }
}
