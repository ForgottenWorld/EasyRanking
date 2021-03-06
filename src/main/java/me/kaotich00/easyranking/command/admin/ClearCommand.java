package me.kaotich00.easyranking.command.admin;

import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.command.api.ERAdminCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.CommandTypes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClearCommand extends ERAdminCommand {

    public void onCommand(CommandSender sender, String[] args) {

        if( args.length < 2 ) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("Not enough arguments, usage:"));
            sender.sendMessage(ChatFormatter.formatSuccessMessage(ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "clear "  + ChatColor.DARK_GRAY + "<" + ChatColor.GRAY + "player" + ChatColor.DARK_GRAY + "> "));
            return;
        }

        String playerName = args[1];
        UUID playerUUID = null;
        Player player = Bukkit.getPlayer(args[1]);
        if( player == null ) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            playerUUID = offlinePlayer.getUniqueId();
            if( !offlinePlayer.hasPlayedBefore() ) {
                sender.sendMessage(ChatFormatter.formatErrorMessage("The player " + ChatColor.GOLD + args[1] + ChatColor.RED + " has never played on this server"));
                return;
            }
        } else {
            playerUUID = player.getUniqueId();
        }

        if( playerUUID == null ) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("No user found for the name " + args[1]));
            return;
        }

        BoardService boardService = ERBoardService.getInstance();
        boardService.clearUserScores(playerUUID);
        sender.sendMessage(ChatFormatter.formatSuccessMessage("Successfully cleared data for player " + ChatColor.GOLD + playerName));

        return;
    }

}
