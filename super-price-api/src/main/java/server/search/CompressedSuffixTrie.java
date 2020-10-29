/*
 *
 *  * Copyright 2020 Imri
 *  *
 *  * This application is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package server.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class CompressedSuffixTrie<T> implements SuffixTrie<T> {
    private static class Node<T> {
        List<Character> charList;
        Map<Character, Node<T>> children;
        boolean isEnd;
        List<T> values;

        Node() {
            charList = new ArrayList<>();
            children = new HashMap<>();
            values = new ArrayList<>();
        }
    }

    final private Node<T> root;

    public CompressedSuffixTrie(Map<String, List<T>> words) {
        root = new Node<>();
        words.forEach(this::addPrefix);
        countNodes();
        compress();
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
                newNode.charList.add(c);
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
        int ind = 1;
        for (char c : word.toCharArray()) {
            if (ind >= cur.charList.size()) {
                ind = 1;
                Node<T> child = cur.children.get(c);
                if (child == null) {
                    return null;
                } else {
                    cur = child;
                }
            } else if (!cur.charList.get(ind++).equals(c)) {
                return null;
            }
        }
        return cur;
    }

    private void compress() {
        final Queue<Node<T>> q = new LinkedList<>();
        q.add(root);

        while (!q.isEmpty()) {
            final Node<T> cur = q.poll();
            while (!cur.isEnd && cur.children.size() == 1) {
                final Map.Entry<Character, Node<T>> child = cur.children.entrySet().iterator().next();
                cur.children = child.getValue().children;
                cur.charList.add(child.getKey());
                cur.isEnd = child.getValue().isEnd;
            }
            q.addAll(cur.children.values());
        }
    }
}
