package database;


import server.Chain;

import java.util.ArrayList;

public interface ChainDao {
    ArrayList<Chain> getAllChains();

    Chain getChain(long id);

    void addChain(Chain chain);
}
