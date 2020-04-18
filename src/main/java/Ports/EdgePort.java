package Ports;

import Events.*;
import se.sics.kompics.PortType;

public class EdgePort extends PortType {{
    positive(InitialMessage.class);
    positive(ReduceMessage.class);
    positive(MapMessage.class);
    positive(WaveMessage.class);

    negative(ReduceMessage.class);
    negative(InitialMessage.class);
    negative(MapMessage.class);
    negative(WaveMessage.class);
}}
