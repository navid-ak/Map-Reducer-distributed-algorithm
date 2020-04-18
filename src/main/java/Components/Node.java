package Components;

import Events.*;
import Ports.EdgePort;
import misc.Edge;
import se.sics.kompics.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.String.format;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Node extends ComponentDefinition {
    Positive<EdgePort> recievePort = positive(EdgePort.class);
    Negative<EdgePort> sendPort = negative(EdgePort.class);

    public String nodeName;
    public String rootName;
    public String parentName;
    public int numberOfresponce;
    public int numberOfresponceInMapper;
    public int numberOfLeaf;
    public HashMap<String, ArrayList<String>> sentWave = new HashMap<>();
    public HashMap<String, String> parent = new HashMap<>();
    public ArrayList<String> data = new ArrayList<>();
    public Stack<ArrayList<String>> splitedData = new Stack<>();
    public ArrayList<String> childrenName = new ArrayList<>();
    public HashMap<String, Integer> neighbours = new HashMap<>();
    public HashMap<String, Integer> neighboursPortion = new HashMap<>();
    public HashMap<String, Integer> wordsCount = new HashMap<>();
    public ArrayList<Edge> edgeInfo = new ArrayList<>();
    Map<String, Integer> edgesMap = new HashMap<>();

    public Handler initialMessageHandler = new Handler<InitialMessage>() {
        @Override
        public void handle(InitialMessage event) {
            if (nodeName.equalsIgnoreCase(event.dst)) {
                if (!event.isAnswer) {
                    parentName = event.src;
                    for (Map.Entry<String, Integer> entry : neighbours.entrySet()) {
                        if (!entry.getKey().equals(parentName)) {
                            childrenName.add(entry.getKey());
                            trigger(new InitialMessage(nodeName, entry.getKey() ,false,false, 0),sendPort);
                        }
                    }
                    if(childrenName.size() == 0){
                        trigger(new InitialMessage(nodeName, parentName, true, true, 1), sendPort);
                    }
                    System.out.println("In initialMessageHandler -> " + nodeName +
                            " received message: src " + event.src +
                            " dst " + event.dst +
                            " isAnswer " + event.isAnswer +
                            " parentName " + parentName +
                            " childrenName " + childrenName);
                }else{
                    numberOfresponce++;
                    numberOfLeaf += event.numOfLeaf;
                    neighboursPortion.put(event.src, event.numOfLeaf);
                    if(numberOfresponce == childrenName.size()){
                        trigger(new InitialMessage(nodeName, parentName, false, true, numberOfLeaf), sendPort);
                        if(rootName == nodeName){
                            callingReduceFile();
                        }
                    }
                    System.out.println("In initialMessageHandler -> " + nodeName +
                            " received message: src " + event.src +
                            " dst " + event.dst +
                            " isAnswer " + event.isAnswer +
                            " numberOfLeaf " + numberOfLeaf +
                            " isLeaf " + event.isLeaf);
                }
            }
        }
    };

    public void mapping(ArrayList<String> data){

        for(int i = 0 ; i < data.size() ; i++){
            if(!wordsCount.containsKey(data.get(i))){
                wordsCount.put(data.get(i), 1);
            }else{
                wordsCount.replace(data.get(i), wordsCount.get(data.get(i)) + 1);
            }
        }
        System.out.println("In mapping function -> " + nodeName +
                            " splitedData size " + data.size());

        trigger(new MapMessage(nodeName, parentName, wordsCount), sendPort);
    }

    public ArrayList<String> gettingData(int portion){
        ArrayList<String> temp = new ArrayList<>();
        for(int i = 0 ; i < portion ; i++){
            temp.addAll(splitedData.pop());
        }
        return temp;
    }

    public void callingReduceFile(){
        int size = (data.size()/numberOfLeaf) + 1;
        for(int i = 0; i < data.size(); i+=size){
            ArrayList<String> temp = new ArrayList<>();
            temp.addAll(data.subList(i , Math.min(i + size, data.size())));
            splitedData.push(temp);
        }
        for(int i = 0 ; i < childrenName.size() ; i++){
            trigger(new ReduceMessage(nodeName, childrenName.get(i), gettingData(neighboursPortion.get(childrenName.get(i)))), sendPort);
        }
    }

    public Handler reduceMessageHandler = new Handler<ReduceMessage>() {
        @Override
        public void handle(ReduceMessage event) {
            if (nodeName.equalsIgnoreCase(event.dst)){
                System.out.println("In reduceMessageHandler -> " + nodeName +
                        " received message: src " + event.src +
                        " dst " + event.dst +
                        " dataSize " + event.data.size());

                data.addAll(event.data);
                if(childrenName.size() == 0){
                    mapping(event.data);
                }else{
                    callingReduceFile();
                }
            }
        }
    };

    public Handler mapMessageHandler = new Handler<MapMessage>() {
        @Override
        public void handle(MapMessage event) {
            if (nodeName.equalsIgnoreCase(event.dst)) {
                System.out.println("In MapMessage -> " + nodeName +
                        " received message: src " + event.src +
                        " dst " + event.dst);
                numberOfresponceInMapper++;
                for(Map.Entry<String, Integer> entry : event.wordsCount.entrySet()) {
                    if(!wordsCount.containsKey(entry.getKey())){
                        wordsCount.put(entry.getKey(), entry.getValue());
                    }else{
                        wordsCount.replace(entry.getKey(), wordsCount.get(entry.getKey()) + entry.getValue());
                    }
                }
                System.out.println("in " + nodeName + " wordcount " + wordsCount.size());
                if(numberOfresponceInMapper == childrenName.size()){
                    trigger(new MapMessage(nodeName, parentName, wordsCount), sendPort);
                    if(nodeName == rootName){
                        System.out.println(wordsCount.size());
                        Path path = Paths.get("src/main/java/output.txt");
                        OpenOption[] options = new OpenOption[] {APPEND , CREATE};
                        try {
                            for(Map.Entry<String, Integer> entry : wordsCount.entrySet()) {
                                Files.write(path, Collections.singleton(entry.getKey() + ":" + entry.getValue()),options);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    };

    public static ArrayList<Edge> removeDuplicates(ArrayList<Edge> list)
    {
        ArrayList<Edge> newList = new ArrayList<>();
        boolean isExist = false;
        for (Edge element : list) {
            for(Edge t : newList)
                if(t.weight == element.weight) {
                    isExist = true;
                    break;
                }
            if(!isExist)
                newList.add(element);
            isExist = false;
        }
        return newList;
    }

    static int floydWarshall(int[][] weights, int numVertices) {

        double[][] dist = new double[numVertices][numVertices];
        for (double[] row : dist)
            Arrays.fill(row, Double.POSITIVE_INFINITY);

        for (int[] w : weights)
            dist[w[0] - 1][w[1] - 1] = w[2];

        int[][] next = new int[numVertices][numVertices];
        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next.length; j++)
                if (i != j)
                    next[i][j] = j + 1;
        }

        for (int k = 0; k < numVertices; k++)
            for (int i = 0; i < numVertices; i++)
                for (int j = 0; j < numVertices; j++)
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
//        printResult(dist, next);
        return findingRoot(dist, next, numVertices);
    }

    static int findingRoot(double[][] dist, int[][] next, int numVertices) {

        int temp;
        double ratio;
        double preRatio = Integer.MAX_VALUE;
        int result = 0;
        for (int i = 0; i < next.length; i++) {
            temp = 0;
            for (int j = 0; j < next.length; j++) {
                if (i != j) {
                    int u = i + 1;
                    int v = j + 1;
                    temp += (int) dist[i][j];
                }
            }
            ratio = temp / numVertices;
            if(ratio < preRatio){
                preRatio = ratio;
                result = i + 1;
            }
        }

        return result;
    }

    static void printResult(double[][] dist, int[][] next) {
        System.out.println("pair     dist    path");
        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next.length; j++) {
                if (i != j) {
                    int u = i + 1;
                    int v = j + 1;
                    String path = format("%d -> %d    %2d     %s", u, v, (int) dist[i][j], u);
                    do {
                        u = next[u - 1][v - 1];
                        path += " -> " + u;
                    } while (u != v);
                    System.out.println(path);
                }
            }
        }
    }

    public Handler waveHandler = new Handler<WaveMessage>() {
        @Override
        public void handle(WaveMessage event) {
            if (nodeName.equalsIgnoreCase(event.dst)){

                if(!event.isAnswer){
                    parent.put(event.initiator, event.src);
                    if(neighbours.size() == 1){
                        event.edgeInfo.addAll(gettingEdgeInfo());
                        trigger(new WaveMessage(nodeName, parent.get(event.initiator), event.initiator, event.edgeInfo , true), sendPort);
                    }
                    for (Map.Entry<String, Integer> entry : neighbours.entrySet()) {
                        if(!entry.getKey().equals(event.src)) {
                            event.edgeInfo.addAll(gettingEdgeInfo());
                            if(!sentWave.containsKey(event.initiator))
                                sentWave.put(event.initiator, new ArrayList<>());
                            if(!sentWave.get(event.initiator).contains(entry.getKey()))
                                sentWave.get(event.initiator).add(entry.getKey());
                            trigger(new WaveMessage(nodeName, entry.getKey(), event.initiator, event.edgeInfo , false), sendPort);
                        }
                    }
                }else{
                    sentWave.get(event.initiator).remove(event.src);

                    if(event.initiator.equals(nodeName)){
                        if(sentWave.get(event.initiator).size() == 0){
                            edgeInfo = removeDuplicates(edgeInfo);
                            int temp = 1;
                            for(int i = 0 ; i < edgeInfo.size() ; i++) {
                                if (!edgesMap.containsKey(edgeInfo.get(i).src)) {
                                    edgesMap.put(edgeInfo.get(i).src, temp);
                                    temp++;
                                }
                                if(!edgesMap.containsKey(edgeInfo.get(i).dst)){
                                    edgesMap.put(edgeInfo.get(i).dst, temp);
                                    temp++;
                                }
                            }
                            int[][] weight = new int[2*edgeInfo.size()][3];
                            for(int i = 0 ; i < edgeInfo.size() ; i++){
                                int[] tempi =  {edgesMap.get(edgeInfo.get(i).src), edgesMap.get(edgeInfo.get(i).dst), edgeInfo.get(i).weight};
                                int[] tempj =  {edgesMap.get(edgeInfo.get(i).dst), edgesMap.get(edgeInfo.get(i).src), edgeInfo.get(i).weight};
                                weight[2*i] = tempi;
                                weight[2*i+1] = tempj;
                            }
                            int numVertices = edgeInfo.size() + 1;
                            int root = floydWarshall(weight, numVertices);
                            for (Map.Entry<String, Integer> entry : edgesMap.entrySet()) {
//                                if(entry.getKey().equals("b")){
//                                    rootName = entry.getKey();
//                                }
                                if (entry.getValue().equals(root)) {
                                    rootName = entry.getKey();
                                    break;
                                }
                            }

                            if (nodeName.equals(rootName)) {
                                System.out.println("In startMapReducer -> " + "root name is " + rootName);

                                try {
                                    File file = new File("src/main/java/test.txt");
                                    FileInputStream fis = null;
                                    fis = new FileInputStream(file);
                                    byte[] dataByte = new byte[(int) file.length()];
                                    fis.read(dataByte);
                                    fis.close();
                                    Collections.addAll(data, new String(dataByte, "UTF-8").replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase().split(" "));
                                    data.removeIf(d -> d.equals(""));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                parentName = null;
                                for (Map.Entry<String, Integer> entry : neighbours.entrySet()) {
                                    childrenName.add(entry.getKey());
                                    trigger(new InitialMessage(nodeName, entry.getKey(), false, false, 0), sendPort);
                                }
                            }
                        }
                    }else{
                        if(sentWave.get(event.initiator).size() == 0) {
                            trigger(new WaveMessage(nodeName, parent.get(event.initiator), event.initiator, event.edgeInfo, true), sendPort);
                        }
                    }
                }
            }
        }
    };

    public ArrayList<Edge> gettingEdgeInfo(){
        ArrayList<Edge> temp = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : neighbours.entrySet())
            temp.add(new Edge(nodeName,entry.getKey(), entry.getValue()));
        return temp;
    }

    public Handler startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            edgeInfo.addAll(gettingEdgeInfo());
            for (Map.Entry<String, Integer> entry : neighbours.entrySet()) {
                if(!sentWave.containsKey(nodeName))
                    sentWave.put(nodeName, new ArrayList<>());
                sentWave.get(nodeName).add(entry.getKey());
                trigger(new WaveMessage(nodeName, entry.getKey(), nodeName, edgeInfo, false), sendPort);
            }
        }
    };

    public Node(InitMessage initMessage) {
        System.out.println("initNode: " + initMessage.nodeName);

        this.nodeName = initMessage.nodeName;
        this.neighbours = initMessage.neighbours;
        this.numberOfLeaf = 0;

        subscribe(startHandler, control);
        subscribe(initialMessageHandler, recievePort);
        subscribe(reduceMessageHandler, recievePort);
        subscribe(mapMessageHandler, recievePort);
        subscribe(waveHandler, recievePort);
    }

}