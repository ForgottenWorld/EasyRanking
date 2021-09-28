package me.kaotich00.easyranking.listener.board;

import me.kaotich00.bounties.events.BountyAddEvent;
import me.kaotich00.bounties.events.BountySubtractEvent;
import me.kaotich00.bounties.service.SimpleBountyService;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

public class BountiesListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBountyAdd(BountyAddEvent event) {
        BoardService boardService = ERBoardService.getInstance();

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.BOUNTY_BOARD_ID);

        if (!optionalBoard.isPresent())
            return;

        Board board = optionalBoard.get();

        if (boardService.isUserExempted(event.getPlayerUUID()))
            return;

        //boardService.addScoreToPlayer(board, event.getPlayerUUID(), event.getAmount().floatValue());

        SimpleBountyService sbs = SimpleBountyService.getInstance();
        sbs.getPlayerBounty(event.getPlayerUUID())
                .ifPresent(value -> boardService.setScoreOfPlayer(board,event.getPlayerUUID(), value.floatValue()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBountySubtract(BountySubtractEvent event) {
        BoardService boardService = ERBoardService.getInstance();

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.BOUNTY_BOARD_ID);

        if (!optionalBoard.isPresent())
            return;

        Board board = optionalBoard.get();

        if(boardService.isUserExempted(event.getPlayerUUID()))
            return;

        //boardService.subtractScoreFromPlayer(board, event.getPlayerUUID(), event.getAmount().floatValue());

        SimpleBountyService sbs = SimpleBountyService.getInstance();
        sbs.getPlayerBounty(event.getPlayerUUID())
                .ifPresent(value -> boardService.setScoreOfPlayer(board,event.getPlayerUUID(), value.floatValue()));
    }

}
