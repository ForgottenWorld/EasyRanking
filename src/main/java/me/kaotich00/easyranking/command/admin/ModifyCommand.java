package me.kaotich00.easyranking.command.admin;

import com.sun.org.apache.xpath.internal.operations.Bool;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.command.api.ERAdminCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.CommandTypes;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Arrays;

public class ModifyCommand extends ERAdminCommand {

    private final static String MODIFY_NAME = "name";
    private final static String MODIFY_DESCRIPTION = "description";
    private final static String MODIFY_MAX_SHOWN_PLAYERS = "maxShownPlayers";
    private final static String MODIFY_SUFFIX = "suffix";
    private final static String SHOULD_RESET = "reset";

    public void onCommand(CommandSender sender, String[] args) {
        if( args.length < 4 ) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("Not enough arguments, usage:"));
            sender.sendMessage(ChatFormatter.formatSuccessMessage(ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "modify "  + ChatColor.DARK_GRAY + "<" + ChatColor.GRAY + "board_id" + ChatColor.DARK_GRAY + "> " + ChatColor.DARK_AQUA + "[name/description/maxShownPlayers/suffix] " + ChatColor.DARK_GRAY + "<" + ChatColor.GRAY + "value" + ChatColor.DARK_GRAY + "> "));
            return;
        }

        BoardService boardService = ERBoardService.getInstance();

        String boardName = args[1];
        if(!boardService.isIdAlreadyUsed(boardName)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("No board found for the name " + ChatColor.GOLD + boardName + ChatColor.RED ));
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

        sender.sendMessage(ChatFormatter.formatSuccessMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + board.getName() + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + "Successfully modified " + ChatColor.GREEN + modifyAction + ChatColor.GRAY + " to " + ChatColor.GOLD + value));
        return;
    }

    private static boolean isValidAction(String modifyAction) {
        return Arrays.asList("name","description","maxShownPlayers","suffix","reset").contains(modifyAction);
    }

}
