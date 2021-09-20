package me.kaotich00.easyranking.commandrework.admin;

import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.config.ConfigurationManager;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class ReloadCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.RELOAD;
    }

    @Override
    public String getInfo() {
        return "Reload configurations";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "reload";
    }

    @Override
    public String getPerm() {
        return "easyranking.admin";
    }

    @Override
    public int getArgsRequired() {
        return 0;
    }

    @Override
    public void perform(Player sender, String[] args) {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        configManager.reloadDefaultConfig();
        sender.sendMessage(ChatFormatter.formatSuccessMessage("Successfully reloaded config.yml"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
