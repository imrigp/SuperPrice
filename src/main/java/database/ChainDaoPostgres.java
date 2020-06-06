package database;

import server.Chain;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChainDaoPostgres implements ChainDao {

    private final DataSource ds;

    public ChainDaoPostgres(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public ArrayList<Chain> getAllChains() {
        ArrayList<Chain> chains = new ArrayList<>();

        try (Connection db = ds.getConnection();
             PreparedStatement pst = db.prepareStatement(GET_ALL_CHAINS_SQL);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                chains.add(new Chain(rs.getLong("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return chains;
    }

    @Override
    public Chain getChain(long id) {
        Chain chain = null;
        try (Connection db = ds.getConnection();
             PreparedStatement pst = db.prepareStatement(GET_CHAINS_SQL)) {

            pst.setLong(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    chain = new Chain(rs.getLong("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return chain;
    }

    @Override
    public void addChain(Chain chain) {
        try (Connection db = ds.getConnection();
             PreparedStatement pst = db.prepareStatement(ADD_CHAIN_SQL)) {

            pst.setLong(1, chain.getId());
            pst.setString(2, chain.getName());
            pst.executeUpdate();
        } catch (
                SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static final String ADD_CHAIN_SQL =
            "INSERT INTO chain (id, name) " +
                    "VALUES (?, ?) " +
                    "ON CONFLICT (id) " +
                    "DO UPDATE " +
                    "SET name = EXCLUDED.name";
    private static final String GET_ALL_CHAINS_SQL = "SELECT * FROM chain";
    private static final String GET_CHAINS_SQL = "SELECT * FROM chain WHERE id = ?";
}
