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

import java.util.*;

public class ClearCommand extends SubCommand {

    @Override
    public String getName() {
        return CommandName.CLEAR;
    }

    @Override
    public String getInfo() {
        return "Clears all scores of the indicated user";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + CommandName.CLEAR +
                ChatColor.DARK_GRAY + " <" + ChatColor.GRAY + "player_name" + ChatColor.DARK_GRAY + "> ";
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

        String playerName = args[1];
        Player player = Bukkit.getPlayer(args[1]);
        UUID playerUUID;

        if( player == null ) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if( !offlinePlayer.hasPlayedBefore() ) {
                sender.sendMessage(ChatFormatter.formatErrorMessage("The player " + ChatColor.GOLD + args[1] +
                        ChatColor.RED + " has never played on this server"));
                return;
            }
            playerUUID = offlinePlayer.getUniqueId();
        } else
            playerUUID = player.getUniqueId();

        BoardService boardService = ERBoardService.getInstance();
        boardService.clearUserScores(playerUUID);
        sender.sendMessage(ChatFormatter.formatSuccessMessage("Successfully cleared data for player " +
                ChatColor.GOLD + playerName));

    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }

}
