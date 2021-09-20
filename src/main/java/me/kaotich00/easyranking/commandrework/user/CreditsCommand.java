package me.kaotich00.easyranking.commandrework.user;

import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class CreditsCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.CREDITS;
    }

    @Override
    public String getInfo() {
        return "EasyRanking plugin credits";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "credits";
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
        sender.sendMessage(ChatFormatter.creditsMessage());
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
