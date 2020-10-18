package server.search;

import java.util.List;

public interface SuffixTrie<T> {
    List<T> search(String query);
}
