package me.kaotich00.easyranking.api.service;

import me.kaotich00.easyranking.api.board.Board;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface BoardService {

    Board createBoard(String id, String name, String description, int maxShownPlayers, String userScoreName, boolean isDefault, boolean fgReset);

    void modifyBoardName(Board board, String name);

    void modifyBoardDescription(Board board, String description);

    void modifyBoardMaxShownPlayers(Board board, Integer maxShownPlayers);

    void modifyBoardSuffix(Board board, String suffix);

    void modifyBoardShouldReset(Board board, boolean shouldReset);

    void deleteBoard(Board board);

    Set<Board> getBoards();

    Optional<Board> getBoardById(String name);

    List<String> getBoardInfo(Board board);

    boolean isIdAlreadyUsed(String name);

    List<UUID> sortScores(Board board);

    void initUserScore(Board board, UUID playerUUID);

    void initUserScore(Board board, UUID playerUUID, Float amount);

    float addScoreToPlayer(Board board, UUID playerUUID, Float score);

    String getPlayerBoardRank(Board board, UUID playerUUID);

    float subtractScoreFromPlayer(Board board, UUID playerUUID, Float score);

    float setScoreOfPlayer(Board board, UUID playerUUID, Float score);

    void clearUserScores(UUID player);

    boolean isUserExempted(UUID player);

    void toggleUserExempt(UUID player);

    Set<UUID> getExemptedUsers();

    CompletableFuture<Void> saveBoardsToDatabase();

}
