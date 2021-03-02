package me.kaotich00.easyranking.listener.board;

import me.kaotich00.bounties.events.BountyAddEvent;
import me.kaotich00.bounties.events.BountySubtractEvent;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

public class BountiesListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBountyAdd(BountyAddEvent event) {
        BoardService boardService = ERBoardService.getInstance();

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.BOUNTY_BOARD_ID);

        if( !optionalBoard.isPresent() ) {
            return;
        }

        Board board = optionalBoard.get();

        Player player = Bukkit.getPlayer(event.getPlayerUUID());
        if(boardService.isUserExempted(player.getUniqueId())) {
            return;
        }

        boardService.addScoreToPlayer(board, player.getUniqueId(), event.getAmount().floatValue());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBountySubtract(BountySubtractEvent event) {
        BoardService boardService = ERBoardService.getInstance();

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.BOUNTY_BOARD_ID);

        if( !optionalBoard.isPresent() ) {
            return;
        }

        Board board = optionalBoard.get();

        Player player = Bukkit.getPlayer(event.getPlayerUUID());
        if(boardService.isUserExempted(player.getUniqueId())) {
            return;
        }

        boardService.subtractScoreFromPlayer(board, player.getUniqueId(), event.getAmount().floatValue());
    }

}
