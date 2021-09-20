package me.kaotich00.easyranking.commandrework.admin;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.board.conversation.prompt.BoardCreationPrompt;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.CREATE;
    }

    @Override
    public String getInfo() {
        return "Create a new board";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "create ";
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
        BoardCreationPrompt creation = new BoardCreationPrompt(Easyranking.getPlugin(Easyranking.class));
        creation.startConversationForPlayer(sender);
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
