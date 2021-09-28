package me.kaotich00.easyranking.listener.board;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FarmingListener implements Listener {

    private final List<Material> specialCases = Arrays.asList(Material.MELON,Material.PUMPKIN,Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,Material.CRIMSON_FUNGUS,Material.WARPED_FUNGUS,Material.SUGAR_CANE,Material.CACTUS);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFarming(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
            return;

        if (!event.getBlock().getMetadata("PLACED").isEmpty())
            return;

        FileConfiguration defaultConfig = Easyranking.getDefaultConfig();
        ConfigurationSection farmingSection = defaultConfig.getConfigurationSection("farming.values");


        if (farmingSection == null
                || !farmingSection.contains(event.getBlock().getType().name()))
            return;

        BoardService boardService = ERBoardService.getInstance();
        Player player = event.getPlayer();

        if (boardService.isUserExempted(player.getUniqueId()))
            return;

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.FARMER_BOARD_ID);

        if (!optionalBoard.isPresent())
            return;

        Board board = optionalBoard.get();
        String harvested = event.getBlock().getType().name();

        if (specialCases.contains(event.getBlock().getType())) {
            boardService.addScoreToPlayer(board, player.getUniqueId(),
                    (float) defaultConfig.getInt("farming.values." + harvested));
            return;
        }

        if (event.getBlock().getBlockData() instanceof Ageable) {
            Ageable age = (Ageable) event.getBlock().getBlockData();
            if (age.getAge() == age.getMaximumAge())
                boardService.addScoreToPlayer(board, player.getUniqueId(),
                        (float) defaultConfig.getInt("farming.values." + harvested));
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFarmBlockPlaced(BlockPlaceEvent event) {
        if (specialCases.contains(event.getBlock().getType())) {
            Block block = event.getBlock();
            block.setMetadata("PLACED", new FixedMetadataValue(Easyranking.getPlugin(Easyranking.class), true));
        }
    }
}
