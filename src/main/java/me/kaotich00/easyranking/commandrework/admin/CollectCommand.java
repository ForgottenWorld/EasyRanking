package me.kaotich00.easyranking.commandrework.admin;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.api.service.RewardService;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.service.ERRewardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class CollectCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.COLLECT;
    }

    @Override
    public String getInfo() {
        return "Collect rewards";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + CommandName.COLLECT;
    }

    @Override
    public String getPerm() {
        return "easyranking.admin";
    }

    @Override
    public int getArgsRequired() {
        return 0;
    }

    @Override
    public void perform(Player sender, String[] args) {

        BoardService boardService = ERBoardService.getInstance();
        Optional<Board> board = boardService.getBoardById(BoardUtil.MOB_KILLED_BOARD_ID);

        if(!board.isPresent()) {
            return;
        }

        Bukkit.getServer().broadcastMessage(ChatFormatter.chatHeader());
        Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "oOo________[ " + ChatColor.YELLOW + ChatColor.BOLD +
                "Rank collection day is here" + ChatColor.GRAY + " ]_________oOo\n\n" );

        RewardService rewardService = ERRewardService.getInstance();
        rewardService.collectRewards();

    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
