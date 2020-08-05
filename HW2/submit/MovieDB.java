import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Genre, Title 을 관리하는 영화 데이터베이스.
 * <p>
 * MyLinkedList 를 사용해 각각 Genre와 Title에 따라 내부적으로 정렬된 상태를
 * 유지하는 데이터베이스이다.
 */
public class MovieDB {
    private MyLinkedList<MyLinkedList<MovieDBItem>> items = new MyLinkedList<>();

    public MovieDB() {
        // HINT: MovieDBGenre 클래스를 정렬된 상태로 유지하기 위한
        // MyLinkedList 타입의 멤버 변수를 초기화 한다.
    }

    public void insert(MovieDBItem item) {
        // Insert the given item to the MovieDB.
        // do not insert duplicates
        //Genre genre = new Genre(item.getGenre());
        int listIndex = 0;
        for (MyLinkedList<MovieDBItem> list : items) { // iterate over genres
            final String thisGroupGenre = list.first().getGenre();
            int compare = thisGroupGenre.compareTo(item.getGenre());
            if (compare > 0) {
                MyLinkedList<MovieDBItem> newList = new MyLinkedList<>();
                newList.add(item);
                items.insert(listIndex, newList);
                return;
            } else if (compare == 0) {
                Iterator<MovieDBItem> iterator = list.iterator();
                int index = 0;
                while (iterator.hasNext()) {
                    MovieDBItem i = iterator.next();
                    int compare2 = i.compareTo(item);
                    if (compare2 > 0) {
                        list.insert(index, item);
                        return;
                    } else if (compare2 == 0) {
                        return;
                    } else {
                        index++;
                    }
                }
                list.add(item);
                return;
            } else {
                listIndex++;
            }
        }
        MyLinkedList<MovieDBItem> newList = new MyLinkedList<>();
        newList.add(item);
        items.insert(listIndex, newList);
        // Printing functionality is provided for the sake of debugging.
        // This code should be removed before submitting your work.
//        System.err.printf("[trace] MovieDB: INSERT [%s] [%s]\n", item.getGenre(), item.getTitle());
    }

    public void delete(MovieDBItem item) {
        // FIXME implement this
        // Remove the given item from the MovieDB.

        // Printing functionality is provided for the sake of debugging.
        // This code should be removed before submitting your work.
//        System.err.printf("[trace] MovieDB: DELETE [%s] [%s]\n", item.getGenre(), item.getTitle());
        String genre = item.getGenre();
        Iterator<MyLinkedList<MovieDBItem>> listIter = items.iterator();
//        outer:
        while (listIter.hasNext()) {
            MyLinkedList<MovieDBItem> list = listIter.next();
            String thisGenre = list.first().getGenre();
            if (thisGenre.compareTo(genre) == 0) {
                Iterator<MovieDBItem> iter = list.iterator();
                int index = 0;
                while (iter.hasNext()) {
                    MovieDBItem e = iter.next();
                    int cmp = e.compareTo(item);
                    if (cmp > 0) {
                        return;
                    } else if (cmp == 0) {
                        iter.remove();
                        if (list.isEmpty()) {
                            listIter.remove();
                        }
                        return;
                    }
                    index++;
                }
            }
        }

    }

    public MyLinkedList<MovieDBItem> search(String term) {
        // FIXME implement this
        // Search the given term from the MovieDB.
        // You should return a linked list of MovieDBItem.
        // The search command is handled at SearchCmd class.

        // Printing search results is the responsibility of SearchCmd class.
        // So you must not use System.out in this method to achieve specs of the assignment.

        // This tracing functionality is provided for the sake of debugging.
        // This code should be removed before submitting your work.
//        System.err.printf("[trace] MovieDB: SEARCH [%s]\n", term);

        // FIXME remove this code and return an appropriate MyLinkedList<MovieDBItem> instance.
        // This code is supplied for avoiding compilation error.
        MyLinkedList<MovieDBItem> results = new MyLinkedList<MovieDBItem>();
        for (MyLinkedList<MovieDBItem> list : items) {
            for (MovieDBItem item : list) {
                if (item.getTitle().contains(term))
                    results.add(item);
            }
        }
        return results;
    }

    public MyLinkedList<MovieDBItem> items() {
        // FIXME implement this
        // Search the given term from the MovieDatabase.
        // You should return a linked list of QueryResult.
        // The print command is handled at PrintCmd class.

        // Printing movie items is the responsibility of PrintCmd class.
        // So you must not use System.out in this method to achieve specs of the assignment.

        // Printing functionality is provided for the sake of debugging.
        // This code should be removed before submitting your work.
//        System.err.printf("[trace] MovieDB: ITEMS\n");

        // FIXME remove this code and return an appropriate MyLinkedList<MovieDBItem> instance.
        // This code is supplied for avoiding compilation error.
        MyLinkedList<MovieDBItem> results = new MyLinkedList<MovieDBItem>();
        for (MyLinkedList<MovieDBItem> list : items) {
            for (MovieDBItem item : list) {
                results.add(item);
            }
        }
        return results;
    }
}

class Genre extends Node<String> implements Comparable<Genre> {
    public Genre(String name) {
        super(name);
    }

    @Override
    public int compareTo(Genre o) {
        return getItem().compareTo(o.getItem());
    }

    @Override
    public int hashCode() {
        return getItem().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Genre)
            return false;
        String otherItem = ((Genre) obj).getItem();
        return getItem().equals(otherItem);
    }
}

class MovieList implements ListInterface<String> {
    MyLinkedList<String> internalLinkedList = new MyLinkedList<>();

    public MovieList() {
    }

    @Override
    public Iterator<String> iterator() {
        return internalLinkedList.iterator();
    }

    @Override
    public boolean isEmpty() {
        return internalLinkedList.isEmpty();
    }

    @Override
    public int size() {
        return internalLinkedList.size();
    }

    @Override
    public void add(String item) {
        Iterator<String> iter = internalLinkedList.iterator();
        int index = 0;
        while (iter.hasNext()) {
            String e = iter.next();
            int cmp = e.compareTo(item);
            if (cmp > 0) {
                break;
            } else if (cmp == 0) {
                return;
            }
            index++;
        }
        insert(index, item);
    }

    @Override
    public String first() {
        return internalLinkedList.first();
    }

    @Override
    public void removeAll() {
        internalLinkedList.removeAll();
    }

    @Override
    public void insert(int i, String item) {
        internalLinkedList.insert(i, item);
    }
}