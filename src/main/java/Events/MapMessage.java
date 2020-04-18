package Events;

import se.sics.kompics.KompicsEvent;

import java.util.HashMap;

public class MapMessage implements KompicsEvent {
    public String src;
    public String dst;
    public HashMap<String, Integer> wordsCount = new HashMap<>();

    public MapMessage(String src, String dst, HashMap<String, Integer> wordsCount) {
        this.src = src;
        this.dst = dst;
        this.wordsCount = wordsCount;
    }
}
