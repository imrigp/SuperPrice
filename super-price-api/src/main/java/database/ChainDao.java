package database;

import java.util.List;

import server.entities.Chain;

public interface ChainDao {
    List<Chain> getAllChains();

    Chain getChain(long id);

    void addChain(Chain chain);
}
