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