package me.kaotich00.easyranking.commandrework.admin;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.board.conversation.prompt.BoardDeletionPrompt;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.NameUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.DELETE;
    }

    @Override
    public String getInfo() {
        return "Delete a board";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "delete "  + ChatColor.DARK_GRAY
                + "<" + ChatColor.GRAY + "board_id" + ChatColor.DARK_GRAY + "> ";
    }

    @Override
    public String getPerm() {
        return "easyranking.admin";
    }

    @Override
    public int getArgsRequired() {
        return 1;
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

        if(board.isDefault()) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("You cannot delete a default board. However, you can disable it from configs"));
            return;
        }

        BoardDeletionPrompt creation = new BoardDeletionPrompt(Easyranking.getPlugin(Easyranking.class),board);
        creation.startConversationForPlayer(sender);
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
