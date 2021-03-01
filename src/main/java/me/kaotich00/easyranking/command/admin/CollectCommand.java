package me.kaotich00.easyranking.command.admin;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.api.service.RewardService;
import me.kaotich00.easyranking.command.api.ERAdminCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.service.ERRewardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Optional;

public class CollectCommand extends ERAdminCommand {

    public void onCommand(CommandSender sender, String[] args) {

        if(!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("This command can only be run by console"));
            return;
        }

        BoardService boardService = ERBoardService.getInstance();
        Optional<Board> board = boardService.getBoardById(BoardUtil.MOB_KILLED_BOARD_ID);

        if(!board.isPresent()) {
            return;
        }

        Bukkit.getServer().broadcastMessage(ChatFormatter.chatHeader());
        Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "oOo________[ " + ChatColor.YELLOW + ChatColor.BOLD + "Rank collection day is here" + ChatColor.GRAY + " ]_________oOo\n\n" );

        RewardService rewardService = ERRewardService.getInstance();
        rewardService.collectRewards();
    }

}
