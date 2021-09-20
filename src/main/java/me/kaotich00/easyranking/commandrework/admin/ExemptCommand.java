package me.kaotich00.easyranking.commandrework.admin;

import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ExemptCommand extends SubCommand {

    private final static String EXEMPT_ADD = "add";
    private final static String EXEMPT_REMOVE = "remove";
    private final static String EXEMPT_LIST = "list";

    @Override
    public String getName() {
        return CommandName.EXEMPT;
    }

    @Override
    public String getInfo() {
        return "Manages exempted players list";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "exempt " + ChatColor.DARK_AQUA + "[" +
                ChatColor.AQUA + "list/add/remove" + ChatColor.DARK_AQUA + "] " + ChatColor.DARK_GRAY + "<" +
                ChatColor.GRAY + "player" + ChatColor.DARK_GRAY + "> ";
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

        if ((args[1].equalsIgnoreCase(EXEMPT_ADD) || args[1].equalsIgnoreCase(EXEMPT_REMOVE)) && args.length < 3) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("Not enough arguments, usage:"));
            sender.sendMessage(ChatFormatter.formatSuccessMessage(ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN +
                    "exempt "  + ChatColor.DARK_GRAY + "[add/remove] " + ChatColor.DARK_GRAY + "<" + ChatColor.GRAY +
                    "player" + ChatColor.DARK_GRAY + "> "));
            return;
        }

        UUID playerUUID = null;
        if( (args[1].equals(EXEMPT_ADD) || args[1].equals(EXEMPT_REMOVE)) ) {
            Player player = Bukkit.getPlayer(args[2]);
            if( player == null ) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                playerUUID = offlinePlayer.getUniqueId();
            } else {
                playerUUID = player.getUniqueId();
            }
        }

        if( (args[1].equals(EXEMPT_ADD) || args[1].equals(EXEMPT_REMOVE)) && playerUUID == null ) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("No user found for the name " + args[2]));
            return;
        }

        String playerName = "";
        BoardService boardService = ERBoardService.getInstance();
        switch(args[1]) {
            case EXEMPT_ADD:
                playerName = args[2];
                if(boardService.isUserExempted(playerUUID)) {
                    sender.sendMessage(ChatFormatter.formatErrorMessage("The user " + playerName + " is already exempted"));
                    return;
                } else {
                    boardService.toggleUserExempt(playerUUID);
                }
                sender.sendMessage(ChatFormatter.formatSuccessMessage("Successfully added " + ChatColor.GOLD + playerName + ChatColor.GREEN + " to exempted players"));
                break;
            case EXEMPT_REMOVE:
                playerName = args[2];
                if(!boardService.isUserExempted(playerUUID)) {
                    sender.sendMessage(ChatFormatter.formatErrorMessage("The user " + playerName + " is not exempted"));
                    return;
                } else {
                    boardService.toggleUserExempt(playerUUID);
                }
                sender.sendMessage(ChatFormatter.formatSuccessMessage("Successfully removed " + ChatColor.GOLD + playerName + ChatColor.GREEN + " from exempted players"));
                break;
            case EXEMPT_LIST:
                sender.sendMessage(ChatFormatter.formatSuccessMessage("List of exempted users:"));
                if( boardService.getExemptedUsers().size() == 0 ) {
                    sender.sendMessage(ChatFormatter.formatSuccessMessage(ChatColor.DARK_GRAY + "No users found"));
                } else {
                    for (UUID uuid : boardService.getExemptedUsers()) {
                        String name = "";
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                            if (offlinePlayer != null) {
                                name = offlinePlayer.getName();
                            }
                        } else {
                            name = player.getPlayerListName();
                        }
                        sender.sendMessage(ChatFormatter.formatErrorMessage(ChatColor.GOLD + "> " + ChatColor.YELLOW + name));
                    }
                }
                break;
        }

    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }

}
