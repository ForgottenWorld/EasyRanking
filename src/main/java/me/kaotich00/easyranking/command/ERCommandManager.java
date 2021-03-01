package me.kaotich00.easyranking.command;

import com.sun.org.apache.xpath.internal.operations.Mod;
import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.command.ERCommand;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.command.admin.*;
import me.kaotich00.easyranking.command.user.CreditsCommand;
import me.kaotich00.easyranking.command.user.InfoCommand;
import me.kaotich00.easyranking.command.user.ShowCommand;
import me.kaotich00.easyranking.command.user.TopCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.CommandTypes;
import me.kaotich00.easyranking.utils.NameUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class ERCommandManager implements TabExecutor {

    private Map<String,ERCommand> commandRegistry;
    private Easyranking plugin;

    public ERCommandManager(Easyranking plugin) {
        this.commandRegistry = new HashMap<>();
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        this.commandRegistry.put(CommandTypes.CREATE_COMMAND, new CreateCommand());
        this.commandRegistry.put(CommandTypes.DELETE_COMMAND, new DeleteCommand());
        this.commandRegistry.put(CommandTypes.MODIFY_COMMAND, new ModifyCommand());
        this.commandRegistry.put(CommandTypes.INFO_COMMAND, new InfoCommand());
        this.commandRegistry.put(CommandTypes.REWARD_COMMAND, new RewardCommand());
        this.commandRegistry.put(CommandTypes.SCORE_COMMAND, new ScoreCommand());
        this.commandRegistry.put(CommandTypes.COLLECT_COMMAND, new CollectCommand());
        this.commandRegistry.put(CommandTypes.RELOAD_COMMAND, new ReloadCommand());
        this.commandRegistry.put(CommandTypes.EXEMPT_COMMAND, new ExemptCommand());
        this.commandRegistry.put(CommandTypes.CLEAR_COMMAND, new ClearCommand());
        this.commandRegistry.put(CommandTypes.CREDITS_COMMAND, new CreditsCommand());
        this.commandRegistry.put(CommandTypes.TOP_COMMAND, new TopCommand());
        this.commandRegistry.put(CommandTypes.SHOW_COMMAND, new ShowCommand());

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if( args.length == 0 ) {
            sender.sendMessage(ChatFormatter.helpMessage());
            return CommandTypes.COMMAND_SUCCESS;
        }

        ERCommand erCommand = getCommand(args[0]);

        if( erCommand != null ) {
            erCommand.onCommand(sender, args);
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        String argsIndex = "";

        /* Suggest child commands */
        if( args.length == 1 ) {
            argsIndex = args[0];
            /* Admin commands */
            suggestions.add("create");
            suggestions.add("modify");
            suggestions.add("delete");
            suggestions.add("score");
            suggestions.add("reward");
            suggestions.add("collect");
            suggestions.add("reload");
            suggestions.add("exempt");
            suggestions.add("clear");
            suggestions.add("credits");
            /* User commands */
            suggestions.add("info");
            suggestions.add("top");
            suggestions.add("show");
        }

        if( args.length == 2 && (   args[0].equals(CommandTypes.MODIFY_COMMAND) ||
                                    args[0].equals(CommandTypes.SCORE_COMMAND) ||
                                    args[0].equals(CommandTypes.DELETE_COMMAND) ||
                                    args[0].equals(CommandTypes.REWARD_COMMAND) ||
                                    args[0].equals(CommandTypes.INFO_COMMAND) ||
                                    args[0].equals(CommandTypes.TOP_COMMAND))) {
            argsIndex = args[1];
            BoardService boardService = ERBoardService.getInstance();
            Set<Board> boardsList = boardService.getBoards();
            if( boardsList != null ) {
                for (Board board : boardsList) {
                    suggestions.add(board.getId());
                }
            }
        }

        if(args.length == 2 && args[0].equals(CommandTypes.EXEMPT_COMMAND)) {
            argsIndex = args[1];
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("list");
        }

        if(args.length == 3) {
            argsIndex = args[2];
            switch(args[0]) {
                case CommandTypes.SCORE_COMMAND:
                    suggestions.add("add");
                    suggestions.add("subtract");
                    suggestions.add("set");
                    break;
                case CommandTypes.MODIFY_COMMAND:
                    suggestions.add("name");
                    suggestions.add("description");
                    suggestions.add("maxShownPlayers");
                    suggestions.add("suffix");
                    suggestions.add("reset");
                    break;
                case CommandTypes.EXEMPT_COMMAND:
                case CommandTypes.CLEAR_COMMAND:
                    if(args[2].equals("list")) {
                        break;
                    }
                    for( Player p : Bukkit.getOnlinePlayers() ) {
                        suggestions.add(p.getPlayerListName());
                    }
                    break;
            }
        }

        /* Suggest player name for score command */
        if(args.length == 4 && args[0].equals(CommandTypes.SCORE_COMMAND)) {
            argsIndex = args[3];
            for( Player p : Bukkit.getOnlinePlayers() ) {
                suggestions.add(p.getPlayerListName());
            }
        }

        return NameUtil.filterByStart(suggestions, argsIndex);
    }

    private ERCommand getCommand(String name) {
        return this.commandRegistry.get(name);
    }

}
