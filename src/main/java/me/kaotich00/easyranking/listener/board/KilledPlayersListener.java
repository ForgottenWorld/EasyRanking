package me.kaotich00.easyranking.listener.board;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Optional;

public class KilledPlayersListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKilled(PlayerDeathEvent event) {
        if( !(event.getEntity().getKiller() instanceof Player) ) {
            return;
        }

        BoardService boardService = ERBoardService.getInstance();
        Player player = event.getEntity().getKiller();

        if(boardService.isUserExempted(player.getUniqueId())) {
            return;
        }

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.PLAYER_KILLED_BOARD_ID);

        if( !optionalBoard.isPresent() ) {
            return;
        }

        Board board = optionalBoard.get();
        boardService.addScoreToPlayer(board, player.getUniqueId(), 1f);
    }

}
