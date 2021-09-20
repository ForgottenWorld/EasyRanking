package me.kaotich00.easyranking.commandrework.admin;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.gui.reward.RewardGUI;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.GUIUtil;
import me.kaotich00.easyranking.utils.NameUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RewardCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.REWARD;
    }

    @Override
    public String getInfo() {
        return "Manage board rewards";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "reward " + ChatColor.DARK_GRAY + "<" +
                ChatColor.GRAY + "board_id" + ChatColor.DARK_GRAY + ">";
    }

    @Override
    public String getPerm() {
        return "easyranking.admin";
    }

    @Override
    public int getArgsRequired() {
        return 2;
    }

    @Override
    public void perform(Player sender, String[] args) {
        BoardService boardService = ERBoardService.getInstance();

        String boardName = args[1];
        if( !boardService.isIdAlreadyUsed(boardName) ) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("No board found for the name " + ChatColor.GOLD + boardName + ChatColor.RED ));
            return;
        }

        Optional<Board> optionalBoard = boardService.getBoardById(boardName);
        if( optionalBoard.isPresent() ) {
            RewardGUI gui = new RewardGUI(sender, optionalBoard.get());
            gui.openGUI(GUIUtil.REWARD_PS_STEP);
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            BoardService boardService = ERBoardService.getInstance();
            return boardService.getBoards().stream().map(Board::getId)
                    .collect(Collectors.toList());
        }
        return null;
    }
}
