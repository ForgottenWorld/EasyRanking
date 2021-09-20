package me.kaotich00.easyranking.commandrework.user;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.NameUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TopCommand extends SubCommand {
    @Override
    public String getName() {
        return CommandName.TOP;
    }

    @Override
    public String getInfo() {
        return "Top players of selected board";
    }

    @Override
    public String getUsage() {
        return ChatColor.DARK_GREEN + "/er " + ChatColor.GREEN + "top " +  ChatColor.DARK_GRAY + "<" + ChatColor.GRAY +
                "board_id" + ChatColor.DARK_GRAY + "> " + ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "page" +
                ChatColor.DARK_AQUA + "]";
    }

    @Override
    public String getPerm() {
        return "easyranking.user";
    }

    @Override
    public int getArgsRequired() {
        return 2;
    }

    @Override
    public void perform(Player sender, String[] args) {
        BoardService boardService = ERBoardService.getInstance();

        String boardName = args[1];
        if(!boardService.isIdAlreadyUsed(boardName)) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("No board found for the name " + ChatColor.GOLD + boardName));
            return;
        }
        Board board = boardService.getBoardById(boardName).get();

        Integer page = 1;
        if( args.length == 3 ) {
            if(!NumberUtils.isNumber(args[2])) {
                sender.sendMessage(ChatFormatter.formatErrorMessage("The page number must be a numeric value" ));
                return;
            }
            page = Integer.parseInt(args[2]);
        }

        List<UUID> userScores = boardService.sortScores(board);
        paginateBoard(sender, board, userScores, page);
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            BoardService boardService = ERBoardService.getInstance();
            return boardService.getBoards().stream().map(Board::getId)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private static void paginateBoard(CommandSender sender, Board board, List<UUID> playerList, int page) {

        int maxPlayersPerPage = 10;
        int totalPages = 1;

        if( playerList.size() > 0 ) {
            totalPages = (int) Math.ceil((double) playerList.size() / maxPlayersPerPage);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ChatFormatter.chatHeader());
        sb.append("\n ");
        sb.append("Top players for the board " + ChatColor.DARK_AQUA).append(board.getName());

        if(playerList.size() == 0) {
            sb.append("\n" + ChatColor.DARK_GRAY + "No players found");
        } else {
            sb.append(" \n \n");
        }

        int position = ((page - 1) * maxPlayersPerPage) + 1;
        for(int i = ((page - 1) * maxPlayersPerPage); i < (maxPlayersPerPage * page); i++) {
            if(i >= playerList.size()) {
                sb.append(" \n");
                position++;
                continue;
            }
            UUID uuid = playerList.get(i);
            Player player = Bukkit.getPlayer(uuid);
            if( player == null ) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if( offlinePlayer != null ) {
                    sb.append("\n" + ChatColor.YELLOW + position + "." + ChatColor.GOLD + " " + offlinePlayer.getName() + ChatColor.DARK_GRAY + " (" + ChatColor.GREEN + ChatFormatter.thousandSeparator(board.getUserScore(uuid).get().longValue()) + " " + board.getUserScoreName() + ChatColor.DARK_GRAY + ")");
                }
            } else {
                sb.append("\n" + ChatColor.YELLOW + position + "." + ChatColor.GOLD + " " + player.getPlayerListName() + ChatColor.DARK_GRAY + " (" + ChatColor.GREEN + ChatFormatter.thousandSeparator(board.getUserScore(uuid).get().longValue()) + " " + board.getUserScoreName() + ChatColor.DARK_GRAY + ")");
            }
            position++;
        }
        sb.append(" \n \n");
        sender.sendMessage(sb.toString());
        sendChatPaginationFooter((Player)sender,board.getId(),page,totalPages);
    }

    public static void sendChatPaginationFooter(Player player, String boardId, int currentPage, int totalPage) {
        ComponentBuilder builder = new ComponentBuilder();
        if(currentPage > 1) {
            TextComponent previousPage = new TextComponent(ChatColor.DARK_GREEN + "[prev] ");
            previousPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Previous page").create()));
            previousPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/er top " + boardId + " " + String.valueOf(currentPage-1)));

            builder.append(previousPage);
        } else {
            builder.append(ChatColor.GREEN + "------");
        }

        builder.append(ChatColor.GREEN + "----------------< " + currentPage + "/" + totalPage + " >------------------");

        if(currentPage < totalPage) {
            TextComponent nextPage = new TextComponent(ChatColor.DARK_GREEN + "[next] ");
            nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Next page").create()));
            nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/er top " + boardId + " " + String.valueOf(currentPage+1)));

            builder.append(nextPage);
        } else {
            builder.append(ChatColor.GREEN + "------");
        }

        player.spigot().sendMessage(builder.create());
    }

}
