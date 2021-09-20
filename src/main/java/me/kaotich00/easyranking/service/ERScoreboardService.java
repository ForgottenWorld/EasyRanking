package me.kaotich00.easyranking.service;

import fr.mrmicky.fastboard.FastBoard;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.api.service.ScoreboardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class ERScoreboardService implements ScoreboardService {

    private static ERScoreboardService taskService;
    private final Map<UUID, FastBoard> scoreboards;

    private ERScoreboardService() {
        if (taskService != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        this.scoreboards = new HashMap<>();
    }

    public static ERScoreboardService getInstance() {
        if(taskService == null) {
            taskService = new ERScoreboardService();
        }
        return taskService;
    }

    @Override
    public void addPlayerToScoreboard(UUID playerUUID) {
        this.scoreboards.put(playerUUID, null);
    }

    @Override
    public boolean isPlayerInScoreboard(UUID playerUUID) {
        return this.scoreboards.containsKey(playerUUID);
    }

    @Override
    public void removePlayerFromScoreboard(UUID playerUUID) {
        FastBoard scoreboard = this.scoreboards.get(playerUUID);

        if(scoreboard != null) {
            scoreboard.delete();
            this.scoreboards.remove(playerUUID);
        }
    }

    @Override
    public void updateScoreBoard(UUID playerUUID) {

        if(!isPlayerInScoreboard(playerUUID))
            return;

        BoardService boardService = ERBoardService.getInstance();
        FastBoard scoreboard = this.scoreboards.get(playerUUID);

        boolean hasValues = false;

        List<String> lines = new ArrayList<>();

        if(scoreboard != null) {
            lines.add(ChatColor.DARK_GREEN + String.join("", Collections.nCopies(15, "-")));

            for(Board board: boardService.getBoards()) {
                if( board.getUserScore(playerUUID).isPresent() ) {
                    Float amount = board.getUserScore(playerUUID).get();
                    lines.add(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + board.getName() + ChatColor.DARK_GRAY
                            + "] " + ChatColor.GREEN + ">> " + boardService.getPlayerBoardRank(board,playerUUID) + "°");
                    lines.add(ChatColor.GOLD + ChatFormatter.thousandSeparator(amount.longValue()));
                    lines.add("");
                    hasValues = true;
                }
            }
        }

        if(!hasValues) {
            lines.add(ChatColor.DARK_GRAY + "No data found");
        }

        lines.add(ChatColor.GREEN + String.join("", Collections.nCopies(15, "-")));

        assert scoreboard != null;
        scoreboard.updateLines(lines);
    }

    @Override
    public void newScoreboard(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);

        if (player == null) return;

        FastBoard board = new FastBoard(player);
        board.updateTitle(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Easy" + ChatColor.GREEN + ChatColor.BOLD + "Ranking" + ChatColor.DARK_GRAY + "]");

        this.scoreboards.put(playerUUID, board);

        updateScoreBoard(playerUUID);
    }
}
