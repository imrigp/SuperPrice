package server.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SimpleSuffixTrie<T> implements SuffixTrie<T> {
    private static class Node<T> {
        final Map<Character, Node<T>> children;
        final List<T> values;
        boolean isEnd;

        Node() {
            children = new HashMap<>();
            values = new ArrayList<>();
        }
    }

    final private Node<T> root;

    public SimpleSuffixTrie(Map<String, List<T>> words) {
        root = new Node<>();
        words.forEach(this::addPrefix);
        countNodes();
    }

    void countNodes() {
        Queue<Node<T>> q = new LinkedList<>();
        q.add(root);
        int count = 0;
        int total = 0;
        while (!q.isEmpty()) {
            Node<T> cur = q.poll();
            total++;
            if (cur.children.size() == 1) {
                count++;
            }
            q.addAll(cur.children.values());
        }
        System.out.println(count);
        System.out.println(total);
    }

    void addPrefix(String s, List<T> values) {
        for (int i = 0; i < s.length(); i++) {
            add(s.substring(i), values);
        }
    }

    void add(String word, List<T> values) {
        Node<T> cur = root;

        for (char c : word.toCharArray()) {
            Node<T> child = cur.children.get(c);
            if (child != null) {
                cur = child;
            } else {
                Node<T> newNode = new Node<>();
                cur.children.put(c, newNode);
                cur = newNode;
            }
        }
        cur.isEnd = true;
        cur.values.addAll(values);
    }

    public List<T> search(String word) {
        Node<T> cur = getEndNode(word);
        return getWordsFromNode(cur);
    }

    private List<T> getWordsFromNode(Node<T> cur) {
        final ArrayList<T> list = new ArrayList<>();
        if (cur == null) {
            return list;
        }

        Queue<Node<T>> q = new LinkedList<>();
        q.add(cur);

        while (!q.isEmpty()) {
            cur = q.poll();
            if (cur.isEnd) {
                list.addAll(cur.values);
            }
            q.addAll(cur.children.values());
        }
        return list;
    }

    private Node<T> getEndNode(String word) {
        Node<T> cur = root;
        for (char c : word.toCharArray()) {
            Node<T> child = cur.children.get(c);
            if (child == null) {
                return null;
            } else {
                cur = child;
            }
        }
        return cur;
    }
}
