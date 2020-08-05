import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Matching {
    static final int k = 6;
    static final int tableSize = 100;

    public static void main(String args[]) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String input = br.readLine();
                if (input.compareTo("QUIT") == 0)
                    break;

                command(input);
            } catch (IOException e) {
                System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
            }
        }
    }

    private static void command(String input) {
        if (input.isEmpty())
            return;
        switch (input.charAt(0)) {
            case '<':
                processInputFile(input.substring(2));
                break;
            case '@':
                printAt(Integer.parseInt(input.substring(2)));
                break;
            case '?':
                findPattern(input.substring(2));
                break;
        }
    }

    private static void findPattern(String pattern) {
        ListNode<StringPos> filtered = null;
        boolean initialized = false;
        for (int i = 0; i < pattern.length() - k + 1; i ++) {
            String subs = pattern.substring(i, i + k);
            HashableString hashStr = new HashableString(subs);
//            System.out.print(hashStr.hashCode());
            ListNode<StringPos> filter = hashTable.get(hashStr);
//            System.out.println(filter.value);
            // filter out wrong answers
            if (filtered != null) {
                ListNode<StringPos> lastIter = null;
                for (ListNode<StringPos> item = filtered; item != null; item = item.next) {
//                    System.out.println("valuex" + item.value.x + "foundvaluex" + filter.value.x);
                    if (!matches(item.value, filter, i)) {
                        // remove item
                        if (lastIter == null) {
                            filtered = item.next;
                        } else {
                            lastIter.next = item.next;
                        }
                    } else {
                        lastIter = item;
                    }
                }
            } else {
                if (!initialized) {
                    filtered = filter;
                    initialized = true;
                } else {
                    break;
                }
//                System.out.println("Before filtering =================");
//                for (ListNode<StringPos> iter = filtered; iter != null; iter = iter.next) {
//                    System.out.println("(" + iter.value.y + "," + iter.value.x + ")");
//                }

            }
        }
        if (filtered == null) {
            System.out.println("(0, 0)");
        } else {
//            System.out.println("After filtering =================");
            for (ListNode<StringPos> iter = filtered; iter != null; iter = iter.next) {
                System.out.print("(" + iter.value.y + ", " + iter.value.x + ")");
                if (iter.next != null)
                    System.out.print(" ");
            }
            System.out.println();
        }
    }

    private static boolean matches(StringPos item, ListNode<StringPos> filter, int offset) {
        for (ListNode<StringPos> iter = filter; iter != null; iter = iter.next) {
            if (iter.value.x - item.x == offset && iter.value.y == item.y) {
                return true;
            }
        }
        return false;
    }

    private static void printAt(int index) {
        hashTable.printAt(index);
    }

    private static HashTable<HashableString, StringPos> hashTable;

    private static void processInputFile(String filePath) {
        hashTable = new HashTable<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            int y = 1;
            while ((line = reader.readLine()) != null) {
                int m = line.length();
                for (int x = 1; x <= m - k + 1; x++) {
                    String subs = line.substring(x - 1, x - 1 + k);
                    StringPos stringPos = new StringPos(subs, x, y);
//                    System.out.println(stringPos+"("+stringPos.y+","+stringPos.x+")");
//                    if (new HashableString(subs).hashCode() == 73) {
//                        System.out.println(subs);
//                    }
                    hashTable.put(new HashableString(subs), stringPos);
                }
                y++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class HashableString implements Comparable<HashableString> {
        public HashableString(String string) {
            this.string = string;
        }

        String string;

        @Override
        public int hashCode() {
            int sum = 0;
            char[] chars = string.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                sum += chars[i];
            }
            return sum % tableSize;
        }

        @Override
        public int compareTo(HashableString o) {
            return string.compareTo(o.string);
        }
    }

    private static class StringPos {
        String substring;
        int x;
        int y;

        public StringPos(String substring, int x, int y) {
            this.substring = substring;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return substring; // + "(" + y + "," + x + ")";
        }
    }

    private static class HashTable<Key extends Comparable<Key>, Value> {

        public HashTable() {
            data = new AVLTree[tableSize];
        }

        public void put(Key key, Value value) {
            int index = key.hashCode() % tableSize;
            if (data[index] == null)
                data[index] = new AVLTree<>();
            data[index].put(key, value);
        }

        public ListNode<Value> get(Key key) {
            int index = key.hashCode() % tableSize;
            if (data[index] == null)
                return null;
            else
                return data[index].get(key);
        }

        AVLTree<Key, Value>[] data;

        public void printAt(int index) {
            if (data[index] != null)
                data[index].print();
            else
                System.out.println("EMPTY");
        }

        private static class AVLTree<Key extends Comparable<Key>, Value> {
            AVLTreeNode<Key, Value> root;

            public void put(Key key, Value value) {
                if (root == null) {
                    root = new AVLTreeNode<>(key, value);
                    return;
                }
                root = root.put(key, value);
            }

            public ListNode<Value> get(Key key) {
                if (root == null)
                    return null;
                return root.get(key);
            }

            public void print() {
                if (root == null)
                    System.out.println("EMPTY");
                root.print();
                System.out.println();
            }

            private static class AVLTreeNode<Key extends Comparable<Key>, Value> {
                ListNode<Value> data;
                Key key;
                AVLTreeNode<Key, Value> leftChild, rightChild;
                int leftHeight, rightHeight;

                public AVLTreeNode(Key key, Value data) {
                    leftHeight = 0;
                    rightHeight = 0;
                    this.data = new ListNode<>(data);
                    this.key = key;
                }

                // returns new root
                public AVLTreeNode<Key, Value> put(Key theKey, Value theValue) {
//                    System.out.println("Put(" + theKey + "," + theValue + ")");
                    AVLTreeNode<Key, Value> newRoot = this;
                    int cmp = key.compareTo(theKey);
                    if (cmp > 0) {
//                        leftHeight++;
                        if (leftChild == null) {
                            leftChild = new AVLTreeNode<>(theKey, theValue);
                        } else {
                            leftChild = leftChild.put(theKey, theValue);
                        }
                    } else if (cmp == 0) {
                        data.append(theValue);
                        return this;
                    } else {
//                        rightHeight++;
                        if (rightChild == null) {
                            rightChild = new AVLTreeNode<>(theKey, theValue);
                        } else {
                            rightChild = rightChild.put(theKey, theValue);
                        }
                    }
                    updateHeights();
                    int diffHeight = leftHeight - rightHeight;
                    if (diffHeight > 1) {
                        // left가 너무 많다. 우회전
                        if (leftChild.rightHeight > leftChild.leftHeight) {
                            // leftChild의 rightChild 기준으로 좌회전 먼저
                            AVLTreeNode<Key, Value> tmp = leftChild.rightChild;
                            leftChild.rightChild = leftChild.rightChild.leftChild;
                            tmp.leftChild = leftChild;
                            leftChild = tmp;
                        }
                        // leftChild 기준 우회전
                        AVLTreeNode<Key, Value> tmp = leftChild.rightChild;
                        leftChild.rightChild = this;
                        newRoot = leftChild;
                        leftChild = tmp;
                    } else if (diffHeight < -1) {
                        // right가 너무 많다. 좌회전
                        if (rightChild.leftHeight > rightChild.rightHeight) {
                            // rightChild의 leftChild 기준으로 우회전해준다.
                            AVLTreeNode<Key, Value> tmp = rightChild.leftChild;
                            rightChild.leftChild = rightChild.leftChild.rightChild;
                            tmp.rightChild = rightChild;
                            rightChild = tmp;
                        }
                        // 이제 좌회전을 한다.
                        // 이제 rightChild를 기준으로좌회전
                        // leftChild는 그대로
                        // rightChild의 rightChild도 그대로
                        AVLTreeNode<Key, Value> tmp = rightChild.leftChild;
                        rightChild.leftChild = this;
                        newRoot = rightChild;
                        rightChild = tmp;
                    }
                    updateHeights();
                    return newRoot;
                }

                private void updateHeights() {
                    if (leftChild == null)
                        leftHeight = 0;
                    else {
                        leftChild.updateHeights();
                        leftHeight = leftChild.maxHeight() + 1;
                    }
                    if (rightChild == null)
                        rightHeight = 0;
                    else {
                        rightChild.updateHeights();
                        rightHeight = rightChild.maxHeight() + 1;
                    }
                }

                private int maxHeight() {
                    if (leftHeight > rightHeight)
                        return leftHeight;
                    return rightHeight;
                }

                public ListNode<Value> get(Key theKey) {
                    int cmp = key.compareTo(theKey);
                    if (cmp == 0) {
                        return data;
                    } else if (cmp > 0) {
                        if (leftChild == null)
                            return null;
                        else
                            return leftChild.get(theKey);
                    } else {
                        if (rightChild == null)
                            return null;
                        else
                            return rightChild.get(theKey);
                    }
                }

                public void print() {
                    System.out.print(data.toString());
                    if (leftChild != null) {
                        System.out.print(" ");
                        leftChild.print();
                    }
                    if (rightChild != null) {
                        System.out.print(" ");
                        rightChild.print();
                    }
                }
            }
        }
    }

    private static class ListNode<Value> {
        // 대표 노드만 알려주는 특수한 toString
        @Override
        public String toString() {
            return value.toString(); // + "/" + (next != null ? next.toString() : "") + "}";
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
