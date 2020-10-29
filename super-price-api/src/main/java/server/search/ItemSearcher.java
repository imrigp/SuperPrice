package server.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import server.DatabaseState;
import server.entities.Item;

public class ItemSearcher {

    private final SuffixTrie<Item> trie;

    public ItemSearcher() {
        Map<String, List<Item>> map = new HashMap<>();
        for (Item item : DatabaseState.getInstance().getAllDbItems().values()) {
            Arrays.stream(item.getName().split("\\s+"))
                  .filter(s -> s.matches("[\\p{L}\\d]+"))
                  .forEach(s -> map.computeIfAbsent(s, k -> new ArrayList<>()).add(item));
        }
        trie = new CompressedSuffixTrie<>(map);
    }

    public List<Item> search(String query) {
        if (query.length() > 50) {
            return Collections.emptyList();
        }

        final String[] words = query.strip().replaceAll("[\\p{P}]", "").split("\\s+");
        if (words.length == 0) {
            return Collections.emptyList();
        }

        final Set<Item> items = new HashSet<>();
        // Find the first valid word and add all of the matches
        int i = 0;
        for (; i < words.length; i++) {
            if (words[i].length() > 1) {
                items.addAll(trie.search(words[i]));
                break;
            }
        }
        // Continue, but now retain only words that occur in all matches
        for (; i < words.length; i++) {
            // No need to keep searching
            if (items.isEmpty()) {
                break;
            }
            if (words[i].length() > 1) {
                items.retainAll(trie.search(words[i]));
            }
        }

        return items.stream().limit(500).collect(Collectors.toList());
    }
}