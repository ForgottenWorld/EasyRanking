package me.kaotich00.easyranking.commandrework.user;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.commandrework.CommandName;
import me.kaotich00.easyranking.commandrework.SubCommand;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
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
        String boardName = args[1];

        BoardService boardService = ERBoardService.getInstance();
        Board board = boardService.getBoardById(boardName).orElse(null);

        if (board == null) {
            sender.sendMessage(ChatFormatter.formatErrorMessage("No board found for the name " + ChatColor.GOLD + boardName));
            return;
        }

        int page = 1;
        if (args.length == 3) {
            if (!NumberUtils.isNumber(args[2])) {
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

    private static void paginateBoard(Player sender, Board board, List<UUID> playerList, int page) {

        int maxPlayersPerPage = 5;
        int totalPages = 1;

        if (playerList.size() > 0)
            totalPages = (int) Math.ceil((double) playerList.size() / maxPlayersPerPage);


        StringBuilder sb = new StringBuilder();
        sb.append(ChatFormatter.chatHeader());
        sb.append("\n ");
        sb.append("Top players for the board " + ChatColor.DARK_AQUA).append(board.getName());

        if (playerList.size() == 0) {
            sb.append("\n" + ChatColor.DARK_GRAY + "No players found" + "\n \n");
            sender.sendMessage(sb.toString());
            sendChatPaginationFooter(sender,board.getId(),page,totalPages);
            return;
        } else
            sb.append(" \n \n");

        int rankPosition = ((page - 1) * maxPlayersPerPage) + 1;

        for (int i = ((page - 1) * maxPlayersPerPage); i < (maxPlayersPerPage * page); i++) {
            if (i >= playerList.size()) {
                break;
                /*
                sb.append(" \n");
                rankPosition++;
                continue;

                 */
            }

            UUID uuid = playerList.get(i);
            Player player = Bukkit.getPlayer(uuid);

            if( player == null ) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer.hasPlayedBefore()) {
                    sb.append("\n" + ChatColor.YELLOW + rankPosition + "." + ChatColor.GOLD + " "
                            + offlinePlayer.getName() + ChatColor.DARK_GRAY + " (" + ChatColor.GREEN
                            + ChatFormatter.thousandSeparator(board.getUserScore(uuid).orElse(0f).longValue())
                            + " " + board.getUserScoreName() + ChatColor.DARK_GRAY + ")");
                }
            } else {
                sb.append("\n" + ChatColor.YELLOW + rankPosition + "." + ChatColor.GOLD + " " + player.getName() +
                        ChatColor.DARK_GRAY + " (" + ChatColor.GREEN +
                        ChatFormatter.thousandSeparator(board.getUserScore(uuid).orElse(0f).longValue()) + " " +
                        board.getUserScoreName() + ChatColor.DARK_GRAY + ")");
            }
            rankPosition++;
        }
        sb.append(" \n \n");
        sender.sendMessage(sb.toString());
        sendChatPaginationFooter(sender,board.getId(),page,totalPages);
    }

    public static void sendChatPaginationFooter(Player player, String boardId, int currentPage, int totalPage) {
        ComponentBuilder builder = new ComponentBuilder();
        if(currentPage > 1) {
            TextComponent previousPage = new TextComponent(ChatColor.DARK_GREEN + "[prev] ");
            previousPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/er top " +
                    boardId + " " + (currentPage - 1)));

            builder.append(previousPage);
        } else {
            builder.append(ChatColor.GREEN + "------");
        }

        builder.append(ChatColor.GREEN + "----------------< " + currentPage + "/" + totalPage + " >------------------");

        if (currentPage < totalPage) {
            TextComponent nextPage = new TextComponent(ChatColor.DARK_GREEN + "[next] ");
            nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/er top " + boardId +
                    " " + (currentPage + 1)));
            builder.append(nextPage);
        } else {
            builder.append(ChatColor.GREEN + "------");
        }

        player.spigot().sendMessage(builder.create());
    }

}
