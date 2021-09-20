package me.kaotich00.easyranking.commandrework;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class SubCommand {

    public abstract String getName();

    public abstract String getInfo();

    public abstract String getUsage();

    public abstract String getPerm();

    public abstract int getArgsRequired();

    public abstract void perform(Player sender, String[] args);

    public abstract List<String> getSubcommandArguments(Player player, String[] args);

}
