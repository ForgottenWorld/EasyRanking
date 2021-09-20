package me.kaotich00.easyranking.commandrework.user;

import me.kaotich00.easyranking.api.service.ScoreboardService;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.service.ERScoreboardService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ShowCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.SHOW;
    }

    @Override
    public String getInfo() {
        return "Enable/Disable scoreboard";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "show ";
    }

    @Override
    public String getPerm() {
        return "easyranking.user";
    }

    @Override
    public int getArgsRequired() {
        return 0;
    }

    @Override
    public void perform(Player sender, String[] args) {
        UUID playerUUID = (sender.getUniqueId());
        ScoreboardService scoreboardService = ERScoreboardService.getInstance();
        if(!scoreboardService.isPlayerInScoreboard(playerUUID)) {
            scoreboardService.newScoreboard(playerUUID);
        } else {
            scoreboardService.removePlayerFromScoreboard(playerUUID);
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
