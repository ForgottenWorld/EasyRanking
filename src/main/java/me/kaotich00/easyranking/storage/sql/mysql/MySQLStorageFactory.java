package me.kaotich00.easyranking.storage.sql.mysql;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.data.UserData;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.storage.ConnectionFactory;
import me.kaotich00.easyranking.storage.StorageCredentials;
import me.kaotich00.easyranking.storage.StorageFactory;

import me.kaotich00.easyranking.storage.hikari.HikariConnectionFactory;
import me.kaotich00.easyranking.storage.sql.reader.SchemaReader;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MySQLStorageFactory extends StorageFactory {

    private static final String BOARD_INSERT_OR_UPDATE = "INSERT INTO easyranking_board(`id`,`name`,`description`,`max_players`,`user_score_name`,`is_visible`,`is_deleted`) VALUES (?,?,?,?,?,true,false) ON DUPLICATE KEY UPDATE `id`=`id`";
    private static final String USER_INSERT_OR_UPDATE = "INSERT INTO easyranking_user(`uuid`,`nickname`) VALUES (?,?) ON DUPLICATE KEY UPDATE `uuid`=`uuid`";
    private static final String USER_SCORE_INSERT_OR_UPDATE = "INSERT INTO easyranking_user_score(`id_user`,`id_board`,`amount`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `amount` = ?";

    private StorageCredentials credentials;
    private ConnectionFactory connectionFactory;
    Easyranking plugin = Easyranking.getPlugin(Easyranking.class);

    public MySQLStorageFactory(String host, String database, String username, String password) {
        credentials = new StorageCredentials(host, database, username, password);
        this.connectionFactory = new HikariConnectionFactory(credentials);
    }

    @Override
    public void initDatabase() {
        this.connectionFactory.init(Easyranking.getPlugin(Easyranking.class));
        executeSchema();
    }

    public void executeSchema() {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                List<String> statements = null;

                String schemaFileName = "me/kaotich00/easyranking/schema/mysql.schema.sql";
                try (InputStream is = plugin.getResource(schemaFileName)) {
                    if (is == null) {
                        throw new IOException("Couldn't locate schema file for MySQL");
                    }

                    statements = SchemaReader.getStatements(is).stream()
                            .collect(Collectors.toList());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try (Connection connection = connectionFactory.getConnection()) {
                    boolean utf8mb4Unsupported = false;

                    try (Statement s = connection.createStatement()) {
                        for (String query : statements) {
                            s.addBatch(query);
                        }

                        try {
                            s.executeBatch();
                        } catch (BatchUpdateException e) {
                            if (e.getMessage().contains("Unknown character set")) {
                                utf8mb4Unsupported = true;
                            } else {
                                throw e;
                            }
                        }
                    }

                    // try again
                    if (utf8mb4Unsupported) {
                        try (Statement s = connection.createStatement()) {
                            for (String query : statements) {
                                s.addBatch(query.replace("utf8mb4", "utf8"));
                            }

                            s.executeBatch();
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        };
        task.runTaskAsynchronously(plugin);
    }

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    public Connection getConnection() throws SQLException {
        return this.connectionFactory.getConnection();
    }

    public void saveBoards() throws SQLException {
        BoardService boardService = ERBoardService.getInstance();

        PreparedStatement preparedStatement = getConnection().prepareStatement(BOARD_INSERT_OR_UPDATE);
        for( Board b : boardService.getBoards() ) {
            preparedStatement.setString(1, b.getName());
            preparedStatement.setString(2, b.getName());
            preparedStatement.setString(3, b.getDescription());
            preparedStatement.setInt(4, b.getMaxShownPlayers());
            preparedStatement.setString(5, b.getUserScoreName());
            preparedStatement.executeUpdate();
        }
    }

    public void saveUserData() throws SQLException {
        BoardService boardService = ERBoardService.getInstance();

        PreparedStatement userInsert = getConnection().prepareStatement(USER_INSERT_OR_UPDATE);
        PreparedStatement userScoreInsert = getConnection().prepareStatement(USER_SCORE_INSERT_OR_UPDATE);
        for(Map.Entry<Board, Set<UserData>> data : boardService.getBoardData().entrySet() ){
            Board board = data.getKey();
            Set<UserData> userData = data.getValue();
            for (UserData ud : userData) {
                String nickname = ud.getNickname();
                UUID uuid = ud.getUniqueId();
                float amount = ud.getScore();

                userInsert.setString(1, uuid.toString());
                userInsert.setString(2, nickname);
                userInsert.executeUpdate();

                userScoreInsert.setString(1, uuid.toString());
                userScoreInsert.setString(2, board.getName());
                userScoreInsert.setFloat(3,amount);
                userScoreInsert.setFloat(4,amount);
                userScoreInsert.executeUpdate();
            }
        }
    }

}
