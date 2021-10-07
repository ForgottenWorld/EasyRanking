package me.kaotich00.easyranking.listener.board;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public class BlacksmithListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onItemRepair(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player))
            return;

        Player player = (Player) humanEntity;
        Inventory eventInventory = event.getInventory();

        if (!(eventInventory instanceof AnvilInventory))
            return;

        AnvilInventory anvilInventory = (AnvilInventory) eventInventory;

        if (event.getRawSlot() != 2)
            return;

        if (player.getLevel() < anvilInventory.getRepairCost())
            return;

        ItemStack[] items = anvilInventory.getContents();

        ItemStack itemLeft = items[0];
        ItemStack itemRepaired = event.getCurrentItem();

        if (itemLeft == null || itemRepaired == null)
            return;

        if (itemLeft.getType() != itemRepaired.getType())
            return;

        ItemMeta metaItemLeft = itemLeft.getItemMeta();
        ItemMeta metaItemRepaired = itemRepaired.getItemMeta();

        if (!(metaItemLeft instanceof Damageable) || !(metaItemRepaired instanceof Damageable))
            return;

        Damageable damageableItemLeft = (Damageable) metaItemLeft;
        Damageable damageableItemRepaired = (Damageable) metaItemRepaired;

        if (damageableItemLeft.getDamage() == damageableItemRepaired.getDamage())
            return;

        int repairedValue = damageableItemLeft.getDamage() - damageableItemRepaired.getDamage();

        BoardService boardService = ERBoardService.getInstance();

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.BLACKSMITH_BOARD_ID);

        optionalBoard.ifPresent(board -> {
            if (boardService.isUserExempted(player.getUniqueId()))
                return;
            boardService.addScoreToPlayer(board,player.getUniqueId(), (float) repairedValue);
        });

    }
}
