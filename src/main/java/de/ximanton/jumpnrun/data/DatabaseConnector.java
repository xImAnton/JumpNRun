package de.ximanton.jumpnrun.data;

import de.ximanton.jumpnrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

public class DatabaseConnector {

    private final Connection connection;

    private Connection openConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + Main.getInstance().getDatabasePath());
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database Error!");
            e.printStackTrace();
        }
        return null;
    }

    public DatabaseConnector() {
        System.out.println("Opening Database Connection...");
        connection = openConnection();
        if (connection == null) {
            throw new RuntimeException("database connection couldn't be established");
        }
        System.out.println("Database Connection Established");
    }

    public void resetJNR(String id) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM $tableName".replace("$tableName", jnrTable(id)));
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void addJNR(String id) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS $tableName (uuid TEXT PRIMARY KEY UNIQUE, start_time INTEGER, end_time INTEGER);".replace("$tableName", jnrTable(id)));
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String jnrTable(String jnrId) {
        return "jnr_" + jnrId;
    }

    public boolean updateScore(PlayerRecord record) {
        try {
            PlayerRecord prevPlayerRecord = getPlayer(record.getJnrID(), record.getPlayerUUID());

            if (prevPlayerRecord != null && prevPlayerRecord.better(record)) {
                return false;
            }

            PreparedStatement updateScoreStmt = connection.prepareStatement("REPLACE INTO $tableName (uuid, start_time, end_time) VALUES (?, ?, ?);".replace("$tableName", jnrTable(record.getJnrID())));
            updateScoreStmt.setString(1, record.getPlayerUUID().toString());
            updateScoreStmt.setLong(2, record.getStartTime());
            updateScoreStmt.setLong(3, record.getEndTime());
            updateScoreStmt.executeUpdate();
            updateScoreStmt.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean removeHighscore(String jnrId) {
        try {
            PlayerRecord highscore = getHighscore(jnrId);
            if (highscore == null) return false;
            PreparedStatement deleteHighscoreStmt = connection.prepareStatement("DELETE FROM $tableName WHERE uuid = ?;".replace("$tableName", jnrTable(jnrId)));
            deleteHighscoreStmt.setString(1, highscore.getPlayerUUID().toString());
            int deleted = deleteHighscoreStmt.executeUpdate();
            deleteHighscoreStmt.close();
            return deleted != 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public PlayerRecord getHighscore(String jnrId) {
        try {
            PreparedStatement selectMaxStmt = connection.prepareStatement("SELECT * FROM $tableName WHERE end_time - start_time = (SELECT MIN(end_time - start_time) FROM $tableName);".replace("$tableName", jnrTable(jnrId)));
            ResultSet res = selectMaxStmt.executeQuery();

            if (!res.next() | res.getMetaData().getColumnCount() == 1) {
                selectMaxStmt.close();
                res.close();
                return null;
            }

            PlayerRecord maxPlayer = new PlayerRecord(jnrId, res);
            res.close();
            selectMaxStmt.close();

            return maxPlayer;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public PlayerRecord getPlayer(String jnrId, UUID playerUUID) {
        try {
            PreparedStatement getPlayerStmt = connection.prepareStatement("SELECT * FROM $tableName WHERE uuid = ?;".replace("$tableName", jnrTable(jnrId)));
            getPlayerStmt.setString(1, playerUUID.toString());
            ResultSet res = getPlayerStmt.executeQuery();
            if (!res.next()) {
                getPlayerStmt.close();
                res.close();
                return null;
            }

            PlayerRecord out = new PlayerRecord(jnrId, res);
            getPlayerStmt.close();
            res.close();
            return out;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static class PlayerRecord {

        private final String jnr_id;
        private final UUID playerUUID;
        private final long startTime;
        private final long endTime;

        public PlayerRecord(String jnrId, ResultSet res) throws SQLException {
            this(jnrId, UUID.fromString(res.getString("uuid")), res.getLong("start_time"), res.getLong("end_time"));
        }

        public PlayerRecord(String jnr_id, UUID playerUUID, long startTime, long endTime) {
            this.jnr_id = jnr_id;
            this.playerUUID = playerUUID;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getJnrID() {
            return jnr_id;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public long time() {
            return endTime - startTime;
        }

        public boolean better(PlayerRecord other) {
            return time() < other.time();
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(playerUUID);
        }
    }
}
