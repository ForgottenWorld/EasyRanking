package me.kaotich00.easyranking.commandrework.user;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.utils.ChatFormatter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class WebPageCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.WEBPAGE;
    }

    @Override
    public String getInfo() {
        return "Link to EasyRanked WEB-Page";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + CommandName.WEBPAGE;
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

        String webURL = Easyranking.getDefaultConfig().getString("web_page_link");

        TextComponent textComponent = new TextComponent("CLICK");
        textComponent.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        textComponent.setBold(true);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,webURL));
        ComponentBuilder componentBuilder = new ComponentBuilder(ChatFormatter.formatSuccessMessage("EasyRanking Web-page : "))
                .append(textComponent);
        sender.spigot().sendMessage(componentBuilder.create());

                /*


                 */
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
