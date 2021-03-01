package me.kaotich00.easyranking.service;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.api.service.RewardService;
import me.kaotich00.easyranking.api.service.ScoreboardService;
import me.kaotich00.easyranking.board.ERBoard;
import me.kaotich00.easyranking.storage.Storage;
import me.kaotich00.easyranking.storage.StorageFactory;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.SortUtil;
import me.rayzr522.jsonmessage.JSONMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ERBoardService implements BoardService {

    private static ERBoardService boardServiceInstance;
    private Set<Board> boardsList;
    private Set<UUID> exemptedUsers;

    private ERBoardService() {
        if (boardServiceInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        this.boardsList = new HashSet<>();
        this.exemptedUsers = new HashSet<>();
    }

    public static ERBoardService getInstance() {
        if(boardServiceInstance == null) {
            boardServiceInstance = new ERBoardService();
        }
        return boardServiceInstance;
    }

    @Override
    public Board createBoard(String id, String name, String description, int maxShownPlayers, String userScoreName, boolean isDefault, boolean fgReset) {
        ERBoard board = new ERBoard(id, name, description, maxShownPlayers, userScoreName, isDefault, fgReset);
        boardsList.add(board);
        ERRewardService.getInstance().registerBoard(board);
        return board;
    }

    @Override
    public void modifyBoardName(Board board, String name) {
        board.setName(name);
    }

    @Override
    public void modifyBoardDescription(Board board, String description) {
        board.setDescription(description);
    }

    @Override
    public void modifyBoardMaxShownPlayers(Board board, Integer maxShownPlayers) {
        board.setMaxShownPlayers(maxShownPlayers);
    }

    @Override
    public void modifyBoardSuffix(Board board, String suffix) {
        board.setUserScoreName(suffix);
    }

    @Override
    public void modifyBoardShouldReset(Board board, boolean shouldReset) {
        board.setFgReset(shouldReset);
    }

    @Override
    public void deleteBoard(Board board) {
        /* Removes the board from the database */
        Storage storage = StorageFactory.getInstance();
        CompletableFuture.runAsync(() -> storage.getStorageMethod().deleteBoard(board.getId()));
        /* Removes all rewards from the board */
        RewardService rewardService = ERRewardService.getInstance();
        rewardService.deleteBoardRewards(board);
        /* Removes the board */
        boardsList.remove(board);
    }

    @Override
    public Set<Board> getBoards() {
        return this.boardsList;
    }

    @Override
    public Optional<Board> getBoardById(String id) {
        return boardsList.stream().filter(board -> board.getId().equals(id)).findFirst();
    }

    @Override
    public List<String> getBoardInfo(Board board) {
        List<String> info = new ArrayList<>();
        info.add(ChatColor.GREEN + "ID" + ": " + ChatColor.RESET + board.getId());
        info.add(ChatColor.GREEN + "Name" +  ": " + ChatColor.RESET + board.getName());
        info.add(ChatColor.GREEN + "Description" + ": " + ChatColor.RESET + board.getDescription());
        info.add(ChatColor.GREEN + "Max shown players" + ": " + ChatColor.RESET + board.getMaxShownPlayers());
        info.add(ChatColor.GREEN + "Score suffix" + ": " + ChatColor.RESET + board.getUserScoreName());
        return info;
    }

    @Override
    public boolean isIdAlreadyUsed(String id) {
        return boardsList.stream().anyMatch(board -> board.getId().equals(id));
    }

    @Override
    public List<UUID> sortScores(Board board) {
        Map<UUID,Float> sortedMap = SortUtil.sortByValue(board.getAllScores(),SortUtil.DESC);
        return new ArrayList<UUID>(sortedMap.keySet());
    }

    @Override
    public void initUserScore(Board board, UUID playerUUID) {
        board.addUser(playerUUID);
    }

    @Override
    public void initUserScore(Board board, UUID playerUUID, Float amount) {
        board.addUser(playerUUID,amount);
    }

    @Override
    public float addScoreToPlayer(Board board, UUID playerUUID, Float score) {

        if( isUserExempted(playerUUID) ) {
            return 0;
        }

        if(!board.getUserScore(playerUUID).isPresent()) {
            board.addUser(playerUUID);
        }

        Float newScore = board.getUserScore(playerUUID).get() + score;
        board.setUserScore(playerUUID, newScore);

        Player player = Bukkit.getPlayer(playerUUID);
        if(player != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                    (ChatFormatter.formatSuccessMessage(
                            ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + board.getName() + ChatColor.DARK_GRAY + "] " +
                                    ChatColor.GRAY + "(" + ChatColor.GREEN + "+" + ChatFormatter.thousandSeparator(score.longValue()) + " " + board.getUserScoreName() + ChatColor.GRAY + ")" +
                                    ChatColor.DARK_GRAY + " |" +
                                    ChatColor.GRAY + " New score: " +
                                    ChatColor.GOLD + ChatFormatter.thousandSeparator(newScore.longValue()) + " " + board.getUserScoreName()
                    ))
            ));
        }

        ScoreboardService scoreboardService = ERScoreboardService.getInstance();
        scoreboardService.updateScoreBoard(playerUUID);

        return newScore;
    }

    @Override
    public float subtractScoreFromPlayer(Board board, UUID playerUUID, Float score) {

        if( isUserExempted(playerUUID) ) {
            return 0;
        }

        if(!board.getUserScore(playerUUID).isPresent()) {
            board.addUser(playerUUID);
        }

        Float newScore = (board.getUserScore(playerUUID).get() - score) >= 0 ? (board.getUserScore(playerUUID).get() - score) : 0;
        board.setUserScore(playerUUID, newScore);

        Player player = Bukkit.getPlayer(playerUUID);
        if(player != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                    (ChatFormatter.formatSuccessMessage(
                            ChatColor.DARK_GRAY +
                                    "[" + ChatColor.DARK_AQUA + board.getName() + ChatColor.DARK_GRAY + "] " +
                                    ChatColor.GRAY + "(" + ChatColor.RED + "-" + ChatFormatter.thousandSeparator(score.longValue()) + " " + board.getUserScoreName() + ChatColor.GRAY + ")" +
                                    ChatColor.DARK_GRAY + " |" +
                                    ChatColor.GRAY + " New score: " +
                                    ChatColor.GOLD + ChatFormatter.thousandSeparator(newScore.longValue()) + " " + board.getUserScoreName()
                    ))
            ));
        }

        ScoreboardService scoreboardService = ERScoreboardService.getInstance();
        scoreboardService.updateScoreBoard(playerUUID);

        return newScore;
    }

    @Override
    public float setScoreOfPlayer(Board board, UUID playerUUID, Float score) {

        if( isUserExempted(playerUUID) ) {
            return 0;
        }

        if(!board.getUserScore(playerUUID).isPresent()) {
            board.addUser(playerUUID);
        }

        board.setUserScore(playerUUID, score);

        Player player = Bukkit.getPlayer(playerUUID);
        if(player != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                    (ChatFormatter.formatSuccessMessage(
                            ChatColor.DARK_GRAY +
                                    "[" + ChatColor.DARK_AQUA + board.getName() + ChatColor.DARK_GRAY + "] " +
                                    ChatColor.GRAY + "(" + ChatColor.GREEN + "=" + ChatFormatter.thousandSeparator(score.longValue()) + " " + board.getUserScoreName() + ChatColor.GRAY + ")" +
                                    ChatColor.DARK_GRAY + " |" +
                                    ChatColor.GRAY + " New score: " +
                                    ChatColor.GOLD + ChatFormatter.thousandSeparator(score.longValue()) + " " + board.getUserScoreName()
                    ))
            ));
        }

        ScoreboardService scoreboardService = ERScoreboardService.getInstance();
        scoreboardService.updateScoreBoard(playerUUID);

        return score;
    }

    @Override
    public void clearUserScores(UUID player) {
        Storage storage = StorageFactory.getInstance();
        for( Board b : this.boardsList ) {
            b.clearUserScore(player);
        }
        CompletableFuture.runAsync(() -> storage.getStorageMethod().deleteUserScores(player));
    }

    @Override
    public boolean isUserExempted(UUID player) {
        return this.exemptedUsers.contains(player);
    }

    @Override
    public void toggleUserExempt(UUID player) {
        if(isUserExempted(player)) {
            this.exemptedUsers.remove(player);
        } else {
            this.exemptedUsers.add(player);
        }
    }

    @Override
    public Set<UUID> getExemptedUsers() {
        return this.exemptedUsers;
    }

    @Override
    public CompletableFuture<Void> saveBoardsToDatabase() {
        Storage storage = StorageFactory.getInstance();
        return CompletableFuture.runAsync(() -> {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Saving boards to database...");
            storage.getStorageMethod().saveBoards();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Saving scores to database...");
            storage.getStorageMethod().saveUserData();
        });
    }
}
