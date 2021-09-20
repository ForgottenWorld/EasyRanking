package me.kaotich00.easyranking.task;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

public class EconomyBoardTask {

    public static int scheduleEconomy() {
        FileConfiguration defaultConfig = Easyranking.getDefaultConfig();
        long period = defaultConfig.getLong("economy.sync_frequency") * 1200;

        return Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Easyranking.getPlugin(Easyranking.class), () -> {
            BoardService boardService = ERBoardService.getInstance();
            Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.ECONOMY_BOARD_SERVICE_ID);

            if( !optionalBoard.isPresent() ) {
                return;
            }

            Board board = optionalBoard.get();

            for( OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers() ) {

                if (offlinePlayer.getName() == null) {
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + "[LOG] [ER] EconomyBoardTask line 33 : " +
                            "\n offlinePlayer with null name -> "+ offlinePlayer.getUniqueId());
                    continue;
                }

                double balance = Easyranking.getEconomy().getBalance(offlinePlayer);


                if(boardService.isUserExempted(offlinePlayer.getUniqueId()))
                    continue;


                boardService.setScoreOfPlayer(board, offlinePlayer.getUniqueId(), (float) balance);
            }
        }, period, period );
    }

}
