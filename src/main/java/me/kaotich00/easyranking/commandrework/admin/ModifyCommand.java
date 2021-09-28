package me.kaotich00.easyranking.commandrework.admin;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModifyCommand extends SubCommand {

    private final static String MODIFY_NAME = "name";
    private final static String MODIFY_DESCRIPTION = "description";
    private final static String MODIFY_MAX_SHOWN_PLAYERS = "maxShownPlayers";
    private final static String MODIFY_SUFFIX = "suffix";
    private final static String SHOULD_RESET = "reset";
    private final List<String> actions = Arrays.asList(MODIFY_NAME, MODIFY_DESCRIPTION,
            MODIFY_MAX_SHOWN_PLAYERS, MODIFY_SUFFIX, SHOULD_RESET);

    @Override
    public String getName() {
        return CommandName.MODIFY;
    }

    @Override
    public String getInfo() {
        return "Manage boards settings";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "modify "  + ChatColor.DARK_GRAY +
                "<" + ChatColor.GRAY + "board_id" + ChatColor.DARK_GRAY + "> " + ChatColor.DARK_AQUA + "[" +
                ChatColor.AQUA + "name/description/maxShownPlayers/suffix" + ChatColor.DARK_AQUA + "] " +
                ChatColor.DARK_GRAY + "<" + ChatColor.GRAY + "value" + ChatColor.DARK_GRAY + "> ";
    }

    @Override
    public String getPerm() {
        return "easyranking.admin";
    }

    @Override
    public int getArgsRequired() {
        return 4;
    }

    @Override
    public void perform(Player sender, String[] args) {
        BoardService boardService = ERBoardService.getInstance();

        String boardName = args[1];
        if(!boardService.isIdAlreadyUsed(boardName)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("No board found for the name " + ChatColor.GOLD +
                    boardName + ChatColor.RED ));
            return;
        }
        Board board = boardService.getBoardById(boardName).get();

        String modifyAction = args[2];
        if(!isValidAction(modifyAction)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("Not a valid action, allowed actions: name/description/maxShownPlayers/suffix" ));
            return;
        }

        /* Join together all the remaining args */
        StringBuilder value = new StringBuilder();
        for(int i = 3; i < args.length; i++){
            String arg = args[i];
            if(i + 1 != args.length) {
                arg += " ";
            }
            value.append(arg);
        }

        switch(modifyAction) {
            case MODIFY_NAME:
                boardService.modifyBoardName(board, value.toString());
                break;
            case MODIFY_DESCRIPTION:
                boardService.modifyBoardDescription(board, value.toString());
                break;
            case MODIFY_MAX_SHOWN_PLAYERS:
                if(!StringUtils.isNumeric(args[3])) {
                    sender.sendMessage(ChatFormatter.formatErrorMessage("Max shown players must be a number"));
                    return;
                }
                boardService.modifyBoardMaxShownPlayers(board, Integer.valueOf(args[3]));
                value = new StringBuilder(String.valueOf(args[3]));
                break;
            case MODIFY_SUFFIX:
                boardService.modifyBoardSuffix(board, value.toString());
                break;
            case SHOULD_RESET:
                if(!value.toString().equals("true") && !value.toString().equals("false")) {
                    sender.sendMessage(ChatFormatter.formatErrorMessage("Value should either be true or false"));
                    return;
                }
                boardService.modifyBoardShouldReset(board, Boolean.parseBoolean(value.toString()));
        }

        sender.sendMessage(ChatFormatter.formatSuccessMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA +
                board.getName() + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + "Successfully modified " +
                ChatColor.GREEN + modifyAction + ChatColor.GRAY + " to " + ChatColor.GOLD + value));

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
        if (args.length == 4 && args[3].equalsIgnoreCase("reset"))
            return Arrays.asList("true","false");
        return null;
    }


    private static boolean isValidAction(String modifyAction) {
        return Arrays.asList("name","description","maxShownPlayers","suffix","reset").contains(modifyAction);
    }
}
