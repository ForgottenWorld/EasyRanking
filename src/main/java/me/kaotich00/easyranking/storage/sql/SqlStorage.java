package me.kaotich00.easyranking.storage.sql;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.reward.Reward;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.api.service.RewardService;
import me.kaotich00.easyranking.reward.types.ERItemReward;
import me.kaotich00.easyranking.reward.types.ERMoneyReward;
import me.kaotich00.easyranking.reward.types.ERTitleReward;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.service.ERRewardService;
import me.kaotich00.easyranking.storage.StorageMethod;
import me.kaotich00.easyranking.storage.util.SchemaReader;
import me.kaotich00.easyranking.utils.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SqlStorage implements StorageMethod {

    private static final String BOARD_INSERT_OR_UPDATE = "INSERT INTO easyranking_board(`id`,`name`,`description`,`max_players`,`user_score_name`,`is_visible`,`is_deleted`,`is_default`) VALUES (?,?,?,?,?,true,false,?) ON DUPLICATE KEY UPDATE `id`=`id`";
    private static final String BOARD_SELECT = "SELECT * FROM easyranking_board WHERE is_deleted = false AND is_default = false";
    private static final String BOARD_DELETE = "DELETE FROM easyranking_board WHERE id = ?";

    private static final String BOARD_ITEM_REWARD_DELETE = "DELETE FROM easyranking_item_reward WHERE id_board = ?";
    private static final String BOARD_ITEM_REWARD_INSERT_OR_UPDATE = "INSERT INTO easyranking_item_reward(`id_board`,`rank_position`,`item_type`) VALUES (?,?,?)";
    private static final String BOARD_ITEM_REWARD_SELECT = "SELECT * FROM easyranking_item_reward";

    private static final String BOARD_MONEY_REWARD_DELETE = "DELETE FROM easyranking_money_reward WHERE id_board = ?";
    private static final String BOARD_MONEY_REWARD_INSERT_OR_UPDATE = "INSERT INTO easyranking_money_reward(`id_board`,`rank_position`,`amount`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `amount` = ?";
    private static final String BOARD_MONEY_REWARD_SELECT = "SELECT * FROM easyranking_money_reward";

    private static final String BOARD_TITLE_REWARD_DELETE = "DELETE FROM easyranking_title_reward WHERE id_board = ?";
    private static final String BOARD_TITLE_REWARD_INSERT_OR_UPDATE = "INSERT INTO easyranking_title_reward(`id_board`,`rank_position`,`title`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `title` = ?";
    private static final String BOARD_TITLE_REWARD_SELECT = "SELECT * FROM easyranking_title_reward";

    private static final String USER_INSERT_OR_UPDATE = "INSERT INTO easyranking_user(`uuid`,`nickname`,`is_exempted`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `is_exempted`=?";
    private static final String USER_EXEMPT_SELECT = "SELECT uuid FROM easyranking_user WHERE is_exempted = true";

    private static final String USER_SCORE_INSERT_OR_UPDATE = "INSERT INTO easyranking_user_score(`id_user`,`id_board`,`amount`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `amount` = ?";
    private static final String USER_DATA_SELECT = "SELECT * FROM easyranking_user_score";
    private static final String USER_DATA_DELETE = "DELETE FROM easyranking_user_score WHERE id_board = ?";

    private ConnectionFactory connectionFactory;
    private final Easyranking plugin;

    public SqlStorage(Easyranking plugin, ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.plugin = plugin;
    }

    @Override
    public Easyranking getPlugin() {
        return this.plugin;
    }

    @Override
    public void init() {
        this.connectionFactory.init(this.plugin);
        prepareDatabaseAndLoadData();
    }

    @Override
    public void shutdown() {
        try {
            saveBoards();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Saving boards to database...");
            saveUserData();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Saving user data to database...");
            saveBoardRewards();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Saving rewards to database...");
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void prepareDatabaseAndLoadData() {
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

                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Loading boards from database...");
                loadBoards();
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Loading user data from database...");
                loadUserData();
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Loading rewards from database...");
                loadBoardRewards();

            }
        };
        task.runTaskAsynchronously(plugin);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connectionFactory.getConnection();
    }

    @Override
    public void loadBoards() {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(BOARD_SELECT)) {
                BoardService boardService = ERBoardService.getInstance();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String name = rs.getString("name");
                        String description = rs.getString("description");
                        int maxShownPlayers = rs.getInt("max_players");
                        String userScoreName = rs.getString("user_score_name");

                        Optional<Board> board = boardService.getBoardById(id);
                        if( !board.isPresent() ) {
                            boardService.createBoard(id,name,description,maxShownPlayers,userScoreName, false);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadUserData() {
        try (Connection c = getConnection()) {
            BoardService boardService = ERBoardService.getInstance();
            try (PreparedStatement ps = c.prepareStatement(USER_DATA_SELECT)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("id_user"));
                        String boardId = rs.getString("id_board");
                        Float amount = rs.getFloat("amount");

                        Optional<Board> boardOptional = boardService.getBoardById(boardId);
                        if( boardOptional.isPresent() ) {
                            boardService.initUserScore(boardOptional.get(), uuid, amount);
                        }
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement(USER_EXEMPT_SELECT)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        boardService.toggleUserExempt(uuid);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveBoards() {
        try (Connection c = getConnection()) {
            BoardService boardService = ERBoardService.getInstance();

            PreparedStatement preparedStatement = c.prepareStatement(BOARD_INSERT_OR_UPDATE);
            for( Board b : boardService.getBoards() ) {
                preparedStatement.setString(1, b.getId());
                preparedStatement.setString(2, b.getName());
                preparedStatement.setString(3, b.getDescription());
                preparedStatement.setInt(4, b.getMaxShownPlayers());
                preparedStatement.setString(5, b.getUserScoreName());
                preparedStatement.setBoolean(6, b.isDefault());
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveUserData() {
        try (Connection c = getConnection()) {
            BoardService boardService = ERBoardService.getInstance();

            PreparedStatement userInsert = c.prepareStatement(USER_INSERT_OR_UPDATE);
            PreparedStatement userScoreInsert = c.prepareStatement(USER_SCORE_INSERT_OR_UPDATE);
            for (Board board : boardService.getBoards()) {
                Iterator iterator = board.getAllScores().entrySet().iterator();
                while( iterator.hasNext() ) {
                    Map.Entry pair = (Map.Entry)iterator.next();
                    UUID playerUUID = (UUID)pair.getKey();
                    Float score = (Float)pair.getValue();

                    String playerName = Bukkit.getPlayer(playerUUID) != null ? Bukkit.getPlayer(playerUUID).getPlayerListName() : Bukkit.getOfflinePlayer(playerUUID).getName();

                    userInsert.setString(1, playerUUID.toString());
                    userInsert.setString(2, playerName);
                    userInsert.setInt(3, boardService.isUserExempted(playerUUID) ? 1 : 0);
                    userInsert.setInt(4, boardService.isUserExempted(playerUUID) ? 1 : 0);
                    userInsert.executeUpdate();

                    userScoreInsert.setString(1, playerUUID.toString());
                    userScoreInsert.setString(2, board.getId());
                    userScoreInsert.setFloat(3, score);
                    userScoreInsert.setFloat(4, score);
                    userScoreInsert.executeUpdate();
                }
            }
            userInsert.close();
            userScoreInsert.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveBoardRewards() {
        RewardService rewardService = ERRewardService.getInstance();

        try (Connection c = getConnection()) {
            PreparedStatement deletePreviousItemsReward = c.prepareStatement(BOARD_ITEM_REWARD_DELETE);
            PreparedStatement insertItemReward = c.prepareStatement(BOARD_ITEM_REWARD_INSERT_OR_UPDATE);

            PreparedStatement insertMoneyReward = c.prepareStatement(BOARD_MONEY_REWARD_INSERT_OR_UPDATE);

            PreparedStatement insertTitleReward = c.prepareStatement(BOARD_TITLE_REWARD_INSERT_OR_UPDATE);
            for (Map.Entry<Board, List<Reward>> data : rewardService.getRewardsList().entrySet()) {
                Board board = data.getKey();
                List<Reward> rewards = data.getValue();

                deletePreviousItemsReward.setString(1, board.getId());
                deletePreviousItemsReward.executeUpdate();

                for (Reward reward : rewards) {
                    Integer rankingPosition = reward.getRankingPosition();
                    if (reward instanceof ERItemReward) {
                        String itemType = SerializationUtil.toBase64(((ERItemReward)reward).getReward());

                        insertItemReward.setString(1, board.getId());
                        insertItemReward.setInt(2, rankingPosition);
                        insertItemReward.setString(3, itemType);
                        insertItemReward.executeUpdate();
                    }
                    if (reward instanceof ERMoneyReward) {
                        Double amount = ((ERMoneyReward)reward).getReward();

                        insertMoneyReward.setString(1, board.getId());
                        insertMoneyReward.setInt(2, rankingPosition);
                        insertMoneyReward.setFloat(3, amount.floatValue());
                        insertMoneyReward.setFloat(4, amount.floatValue());
                        insertMoneyReward.executeUpdate();
                    }
                    if (reward instanceof ERTitleReward) {
                        String title = ((ERTitleReward)reward).getReward();

                        insertTitleReward.setString(1, board.getId());
                        insertTitleReward.setInt(2, rankingPosition);
                        insertTitleReward.setString(3, title);
                        insertTitleReward.setString(4, title);
                        insertTitleReward.executeUpdate();
                    }
                }
            }
            deletePreviousItemsReward.close();
            insertItemReward.close();
            insertMoneyReward.close();
            insertTitleReward.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadBoardRewards() {
        try (Connection c = getConnection()) {
            RewardService rewardService = ERRewardService.getInstance();
            BoardService boardService = ERBoardService.getInstance();

            try (PreparedStatement ps = c.prepareStatement(BOARD_ITEM_REWARD_SELECT)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String boardId = rs.getString("id_board");
                        Integer rankPosition = rs.getInt("rank_position");
                        String itemType = rs.getString("item_type");

                        /* Deserialize ItemStack */
                        ItemStack itemStack = SerializationUtil.fromBase64(itemType);

                        Optional<Board> boardOptional = boardService.getBoardById(boardId);
                        if( boardOptional.isPresent() ) {
                            rewardService.newItemReward(itemStack, boardOptional.get(), rankPosition);
                        }
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement(BOARD_MONEY_REWARD_SELECT)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String boardId = rs.getString("id_board");
                        Integer rankPosition = rs.getInt("rank_position");
                        Double amount = rs.getDouble("amount");

                        Optional<Board> boardOptional = boardService.getBoardById(boardId);
                        if( boardOptional.isPresent() ) {
                            rewardService.newMoneyReward(amount, boardOptional.get(), rankPosition);
                        }
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement(BOARD_TITLE_REWARD_SELECT)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String boardId = rs.getString("id_board");
                        Integer rankPosition = rs.getInt("rank_position");
                        String title = rs.getString("title");

                        Optional<Board> boardOptional = boardService.getBoardById(boardId);
                        if( boardOptional.isPresent() ) {
                            rewardService.newTitleReward(title, boardOptional.get(), rankPosition);
                        }
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteBoard(String boardId) {
        try (Connection c = getConnection()) {
            PreparedStatement deleteItemRewards = c.prepareStatement(BOARD_ITEM_REWARD_DELETE);
            deleteItemRewards.setString(1, boardId);
            deleteItemRewards.executeUpdate();
            deleteItemRewards.close();

            PreparedStatement deleteMoneyRewards = c.prepareStatement(BOARD_MONEY_REWARD_DELETE);
            deleteMoneyRewards.setString(1, boardId);
            deleteMoneyRewards.executeUpdate();
            deleteMoneyRewards.close();

            PreparedStatement deleteTitleRewards = c.prepareStatement(BOARD_TITLE_REWARD_DELETE);
            deleteTitleRewards.setString(1, boardId);
            deleteTitleRewards.executeUpdate();
            deleteTitleRewards.close();

            PreparedStatement deleteUserScore = c.prepareStatement(USER_DATA_DELETE);
            deleteUserScore.setString(1, boardId);
            deleteUserScore.executeUpdate();
            deleteUserScore.close();

            PreparedStatement deleteBoard = c.prepareStatement(BOARD_DELETE);
            deleteBoard.setString(1, boardId);
            deleteBoard.executeUpdate();
            deleteBoard.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
