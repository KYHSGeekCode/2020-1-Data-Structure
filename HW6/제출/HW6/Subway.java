import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// 환승역을 어떻게 하나.
// 환승하는 데 걸리는 시간은 5분


public class Subway {
    static final int tableSize = 100;
//    static final HashMap<String, ArrayList<String>> adjacencyList = new HashMap<>();

    static final Map<String, Station> idToStation = new HashMap<>();
    static final Map<String, List<Station>> nameToStation = new HashMap<>();

    static final long INFINITY = Long.MAX_VALUE;

    public static void main(String[] args) {
//        System.out.println(Arrays.toString(args));
        String fileName = args[0];
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            idToStation.clear();
            while (!(line = bufferedReader.readLine()).isBlank()) {
                StringTokenizer stringTokenizer = new StringTokenizer(line);
                String id = stringTokenizer.nextToken();
                String name = stringTokenizer.nextToken();
                String lineNo = stringTokenizer.nextToken();
                Station station = new Station(id, name, lineNo);
                idToStation.put(id, station);
                if (!nameToStation.containsKey(name)) {
                    nameToStation.put(name, new LinkedList<>());
                }
                List<Station> alreadyStations = nameToStation.get(name);
                for (Station alreadyStation : alreadyStations) {
                    alreadyStation.addOutgoingEdge(new Edge(alreadyStation, station, 5));
                    station.addOutgoingEdge(new Edge(station, alreadyStation, 5));
                }
                alreadyStations.add(station);
            }
            // 갈아타는 시간은 5분이다.
            while ((line = bufferedReader.readLine()) != null) {
                StringTokenizer stringTokenizer = new StringTokenizer(line);
                String fromId = stringTokenizer.nextToken();
                String toId = stringTokenizer.nextToken();
                int time = Integer.parseInt(stringTokenizer.nextToken());
                Station from = idToStation.get(fromId);
                Station to = idToStation.get(toId);
                from.addOutgoingEdge(new Edge(from, to, time));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner inputScanner = new Scanner(System.in);
        while (true) {
            String inputLine = inputScanner.nextLine();
            if ("QUIT".equals(inputLine))
                break;
            String[] split = inputLine.split("\\s");
            String from = split[0];
            String to = split[1];
            Path path = findPath(from, to);
//            System.out.println(path);
            if (path != null) {
                final int size = path.size();
                for (int i = 0; i < size; i++) {
                    Station station = path.get(i);
                    boolean willTransfer = isWillTransfer(path, i);
                    if (willTransfer)
                        System.out.print("[");
                    //System.out.print(station.id);
                    System.out.print(station);
                    //System.out.print(station.line);
                    if (willTransfer)
                        System.out.print("]");
                    if (i != path.size() - 1)
                        System.out.print(" ");
                    if (willTransfer)
                        i++;
                }
                System.out.println();
                System.out.println(path.getTotalWeight());
            }
        }
    }

    private static boolean isWillTransfer(Path path, int i) {
        final int size = path.size();
        Station station = path.get(i);
        if (i < size - 1) {
            Station next = path.get(i + 1);
            String nextLine = next.line;
            String nextId = next.id;
            String nextName = next.name;
            if (!Objects.equals(station.line, nextLine) ||
                    !Objects.equals(station.id, nextId) && Objects.equals(station.name, nextName))
                return true;
        }
        return false;
    }

    private static Path findPath(String from, String to) {
        List<Station> startStations = nameToStation.get(from);
        List<Station> endStations = nameToStation.get(to);
        Path shortestPath = null;
        for (Station startStation : startStations) {
            for (Station endStation : endStations) {
                Path path1 = findPath(startStation, endStation);
                if (path1 == null) {
                    System.err.println("No such path");
                    return null;
                }
                if (shortestPath == null)
                    shortestPath = path1;
                else if (shortestPath.totalWeight > path1.totalWeight)
                    shortestPath = path1;
            }
        }
//        if (!Objects.equals(path1.get(0).line, path1.get(1).line)) {
//            path1.remove(0);
//            path1.totalWeight -= 5;
//        }
//        int len = path1.size();
//        if (!Objects.equals(path1.get(len - 2).line, path1.get(len - 1).line)) {
//            path1.remove(len - 1);
//            path1.totalWeight -= 5;
//        }
        return shortestPath;
    }

    private static Path findPath(Station fromStation, Station toStation) {
//        System.out.println(from + " " + to);
        // Dijkstra Algorithm
        Set<Station> done = new HashSet<>();
        Set<Station> notDone = new HashSet<>(idToStation.values());
        Map<Station, Long> distance = new HashMap<>();
        Map<Station, Path> path = new HashMap<>();
        distance.put(fromStation, 0L);
        path.put(fromStation, new Path(Collections.singletonList(fromStation)));
        while (!notDone.isEmpty()) {
            Station x = extractMin(notDone, distance);
            done.add(x);
            notDone.remove(x);
            for (Edge e : x.getOutgoingEdges()) {
                Station eto = e.to;
                if (notDone.contains(eto)) {
                    long distanceX = distance.getOrDefault(x, INFINITY);
                    long distanceEto = distance.getOrDefault(eto, INFINITY);
                    if (distanceX + e.weight < distanceEto) {
                        distance.put(eto, distanceX + e.weight);
                        Path newpath = (Path) path.getOrDefault(x, new Path(Collections.singletonList(x))).clone();
                        newpath.add(e);
                        path.put(eto, newpath);
                    }
                }
            }
        }
        return path.get(toStation);
    }

    private static Station extractMin(Set<Station> notDone, Map<Station, Long> distance) {
        Station minStation = notDone.iterator().next();
        long min = distance.getOrDefault(minStation, INFINITY);
        for (Station station : notDone) {
            long dist = distance.getOrDefault(station, INFINITY);
            if (dist < min) {
                min = dist;
                minStation = station;
            }
        }
        return minStation;
    }


    private static class Edge {
        Station from;

        public Edge(Station from, Station to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        Station to;
        int weight;
    }

    private static class Station {
        String id;
        String name;
        String line;
        List<Edge> outgoingEdges = new ArrayList<>();

        public Station(String id, String name, String line) {
            this.id = id;
            this.name = name;
            this.line = line;
        }

        public List<Edge> getOutgoingEdges() {
            return outgoingEdges;
        }

        public void addOutgoingEdge(Edge edge) {
            outgoingEdges.add(edge);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class Path extends ArrayList<Station> {
        long totalWeight;

        public Path(List<Station> asList) {
            super(asList);
        }

        public Station get(int index) {
            return super.get(index);
        }

        public long getTotalWeight() {
            return totalWeight;
        }

        public boolean add(Edge edge) {
            totalWeight += edge.weight;
            return super.add(edge.to);
        }
    }

//    private static class HashTable<Key extends Comparable<Key>, Value> {
//
//        public HashTable() {
//            data = new AVLTree[tableSize];
//        }
//
//        public void put(Key key, Value value) {
//            int index = key.hashCode() % tableSize;
//            if (data[index] == null)
//                data[index] = new AVLTree<>();
//            data[index].put(key, value);
//        }
//
//        public ListNode<Value> get(Key key) {
//            int index = key.hashCode() % tableSize;
//            if (data[index] == null)
//                return null;
//            else
//                return data[index].get(key);
//        }
//
//        AVLTree<Key, Value>[] data;
//
//        public void printAt(int index) {
//            if (data[index] != null)
//                data[index].print();
//            else
//                System.out.println("EMPTY");
//        }

//        private static class AVLTree<Key extends Comparable<Key>, Value> {
//            AVLTreeNode<Key, Value> root;
//
//            public void put(Key key, Value value) {
//                if (root == null) {
//                    root = new AVLTreeNode<>(key, value);
//                    return;
//                }
//                root.put(key, value);
//            }
//
//            public ListNode<Value> get(Key key) {
//                if (root == null)
//                    return null;
//                return root.get(key);
//            }
//
//            public void print() {
//                if (root == null)
//                    System.out.println("EMPTY");
//                root.print();
//                System.out.println();
//            }
//
//            private static class AVLTreeNode<Key extends Comparable<Key>, Value> {
//                ListNode<Value> data;
//                Key key;
//                AVLTreeNode<Key, Value> leftChild, rightChild;
//                int leftHeight, rightHeight;
//
//                public AVLTreeNode(Key key, Value data) {
//                    leftHeight = 0;
//                    rightHeight = 0;
//                    this.data = new ListNode<>(data);
//                    this.key = key;
//                }
//
//                public void put(Key theKey, Value theValue) {
//                    int cmp = key.compareTo(theKey);
//                    if (cmp > 0) {
//                        leftHeight++;
//                        if (leftChild == null) {
//                            leftChild = new AVLTreeNode<>(theKey, theValue);
//                        } else {
//                            leftChild.put(theKey, theValue);
//                        }
//                    } else if (cmp == 0) {
//                        data.append(theValue);
//                        return;
//                    } else {
//                        rightHeight++;
//
//                        if (rightChild == null) {
//                            rightChild = new AVLTreeNode<>(theKey, theValue);
//                        } else {
//                            rightChild.put(theKey, theValue);
//                        }
//                    }
//                    int diffHeight = leftHeight - rightHeight;
//                    if (diffHeight > 1) {
//                        // left가 너무 많다. 우회전
//                        // 만약 left가 right가 있다면
//                        // 좌회전 후 우회전
//
//                    } else if (diffHeight < -1) {
//                        // right가 너무 많다. 좌회전
//
//                    }
//                }
//
//                public ListNode<Value> get(Key theKey) {
//                    int cmp = key.compareTo(theKey);
//                    if (cmp == 0) {
//                        return data;
//                    } else if (cmp > 0) {
//                        if (leftChild == null)
//                            return null;
//                        else
//                            return leftChild.get(theKey);
//                    } else {
//                        if (rightChild == null)
//                            return null;
//                        else
//                            return rightChild.get(theKey);
//                    }
//                }
//
//                public void print() {
//                    System.out.print(data.toString());
//                    if (leftChild != null) {
//                        System.out.print(" ");
//                        leftChild.print();
//                    }
//                    if (rightChild != null) {
//                        System.out.print(" ");
//                        rightChild.print();
//                    }
//                }
//            }
//        }
//    }


    // 집함의 표현을 위한 자료구조
    // From algorithm HW 3-2
    private static class SetNode {
        int data;
        int rank;
        SetNode parent;

        public SetNode(int data) {
            this.data = data;
            parent = this;
            rank = 0;
        }

        public SetNode findSet() {
            if (parent != this)
                parent = parent.findSet();
            return parent;
        }

        public static void union(SetNode x, SetNode y) {
            x = x.findSet();
            y = y.findSet();
            if (x.rank > y.rank) {
                y.parent = x;
            } else {
                x.parent = y;
                if (x.rank == y.rank)
                    y.rank++;
            }
        }
    }

    private static class ListNode<Value> {
        // 대표 노드만 알려주는 특수한 toString
        @Override
        public String toString() {
            return value.toString();
        }

        public ListNode(Value value) {
            this.value = value;
        }

        Value value;
        ListNode<Value> next;

        public void append(Value theValue) {
            ListNode<Value> newNode = new ListNode<>(theValue);
            if (next == null)
                next = newNode;
            else {
                next.append(theValue);
            }
        }
    }
}
