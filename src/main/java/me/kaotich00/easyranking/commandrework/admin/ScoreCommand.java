package me.kaotich00.easyranking.commandrework.admin;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.NameUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScoreCommand extends SubCommand {

    private final List<String> actions = Arrays.asList("add","subtract","set");

    @Override
    public String getName() {
        return CommandName.SCORE;
    }

    @Override
    public String getInfo() {
        return "Manage player scores";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "score "  + ChatColor.DARK_GRAY + "<" +
                ChatColor.GRAY + "board_id" + ChatColor.DARK_GRAY + "> " + ChatColor.DARK_AQUA + "[" +
                ChatColor.AQUA + "add/subtract/set" + ChatColor.DARK_AQUA + "] " + ChatColor.DARK_GRAY + "<" +
                ChatColor.GRAY + "player" + ChatColor.DARK_GRAY + "> " + "<" + ChatColor.GRAY + "amount" +
                ChatColor.DARK_GRAY + ">";
    }

    @Override
    public String getPerm() {
        return "easyranking.admin";
    }

    @Override
    public int getArgsRequired() {
        return 5;
    }

    @Override
    public void perform(Player sender, String[] args) {
        BoardService boardService = ERBoardService.getInstance();

        String boardName = args[1];
        if(!boardService.isIdAlreadyUsed(boardName)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("No board found for the name " + ChatColor.GOLD + boardName + ChatColor.RED ));
            return;
        }
        Board board = boardService.getBoardById(boardName).get();

        String scoreOperator = args[2];
        if(!isValidScoreOperator(scoreOperator)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("Not a valid operator, allowed operators: add/subtract/set" ));
            return;
        }

        String playerName = args[3];
        if(Bukkit.getPlayer(playerName) == null && Bukkit.getOfflinePlayer(playerName) == null) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("The player " + playerName + " doesn't exist" ));
            return;
        }

        UUID playerUUID;
        Player player = Bukkit.getPlayer(playerName);
        if( player == null ) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[3]);
            playerUUID = offlinePlayer.getUniqueId();
            if( !offlinePlayer.hasPlayedBefore() ) {
                sender.sendMessage(ChatFormatter.formatErrorMessage("The player " + ChatColor.GOLD + playerName + ChatColor.RED + " has never played on this server"));
                return;
            }
        } else {
            playerUUID = player.getUniqueId();
        }

        if(boardService.isUserExempted(playerUUID)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("Cannot modify user score: the user " + playerName + " is exempted from leaderboards" ));
            return;
        }

        String pointsAmount = args[4];
        if(!NumberUtils.isNumber(pointsAmount)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("The amount selected is not a numeric value" ));
            return;
        }
        Float score = Float.parseFloat(pointsAmount);

        Float totalScore = 0f;
        switch(scoreOperator) {
            case "add":
                totalScore = boardService.addScoreToPlayer(board, playerUUID, score);
                sender.sendMessage(ChatFormatter.formatSuccessMessage("Successfully added " + ChatColor.GOLD + ChatFormatter.thousandSeparator(score.longValue()) + " " + ChatColor.GREEN + board.getUserScoreName() + " to " + ChatColor.GOLD + playerName));
                break;
            case "subtract":
                totalScore = boardService.subtractScoreFromPlayer(board, playerUUID, score);
                sender.sendMessage(ChatFormatter.formatSuccessMessage("Successfully subtracted " + ChatColor.GOLD + ChatFormatter.thousandSeparator(score.longValue()) + " " + ChatColor.GREEN + board.getUserScoreName() + " from " + ChatColor.GOLD + playerName));
                break;
            case "set":
                totalScore = boardService.setScoreOfPlayer(board, playerUUID, score);
                sender.sendMessage(ChatFormatter.formatSuccessMessage("Successfully set " + ChatColor.GOLD + ChatFormatter.thousandSeparator(score.longValue()) + " " + ChatColor.GREEN + board.getUserScoreName() + " to " + ChatColor.GOLD + playerName));
                break;
        }

        sender.sendMessage(ChatFormatter.formatSuccessMessage(ChatColor.GRAY + "New score for " + ChatColor.GOLD + playerName + ChatColor.GRAY + ": " + ChatColor.GREEN + ChatFormatter.thousandSeparator(totalScore.longValue())));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            BoardService boardService = ERBoardService.getInstance();
            return boardService.getBoards().stream().map(Board::getId)
                    .collect(Collectors.toList());
        }
        if (args.length == 3)
            return actions;
        return null;
    }

    private static boolean isValidScoreOperator(String scoreOperator) {
        return Arrays.asList("add","subtract","set").contains(scoreOperator);
    }
}
