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

package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.entities.Chain;

public class ChainDaoPostgres implements ChainDao {
    private static final Logger log = LoggerFactory.getLogger(ChainDaoPostgres.class);
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
            log.error("", e);
        }

        return chains;
    }

    @Override
    public Chain getChain(long id) {
        Chain chain = null;
        try (Connection db = ds.getConnection();
             PreparedStatement pst = db.prepareStatement(GET_CHAIN_SQL)) {

            pst.setLong(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    chain = new Chain(rs.getLong("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("", e);
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
            log.error("", e);
        }
    }

    private static final String ADD_CHAIN_SQL =
            "INSERT INTO chain (id, name) " +
                    "VALUES (?, ?) " +
                    "ON CONFLICT (id) " +
                    "DO UPDATE " +
                    "SET name = EXCLUDED.name";
    private static final String GET_ALL_CHAINS_SQL = "SELECT * FROM chain";
    private static final String GET_CHAIN_SQL = "SELECT * FROM chain WHERE id = ?";
}
