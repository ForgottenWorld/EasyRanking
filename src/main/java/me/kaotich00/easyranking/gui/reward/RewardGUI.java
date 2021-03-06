package me.kaotich00.easyranking.gui.reward;

import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.reward.Reward;
import me.kaotich00.easyranking.reward.types.ERItemReward;
import me.kaotich00.easyranking.service.ERRewardService;
import me.kaotich00.easyranking.utils.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class RewardGUI {

    private Player player;
    private Board board;
    private int rankPlace;

    public RewardGUI(Player player, Board board) {
        this.player = player;
        this.board = board;
        ERRewardService.getInstance().addModifyingPlayer(player.getUniqueId(), board);
    }

    public RewardGUI(Player player, Board board, int rankPlace) {
        this.player = player;
        this.board = board;
        this.rankPlace = rankPlace;
        ERRewardService.getInstance().addModifyingPlayer(player.getUniqueId(), board);
        ERRewardService.getInstance().addItemSelectionRank(player.getUniqueId(), rankPlace);
    }

    public void openGUI(int step) {
        switch( step ) {
            case GUIUtil.REWARD_PS_STEP:
                openRankPositionGUI();
                break;
            case GUIUtil.REWARD_TS_STEP:
                openRewardTypeGUI();
                break;
            case GUIUtil.REWARD_SELECT_ITEMS_STEP:
                openItemTypeRewardGUI();
                break;
        }
    }

    private void openRankPositionGUI() {
        Inventory GUI = Bukkit.createInventory(null, GUIUtil.REWARD_PS_INVENTORY_SIZE, GUIUtil.REWARD_PS_INVENTORY_TITLE);

        GUI.setItem(GUIUtil.REWARD_PS_INFO_SLOT, rpInfoMenu());
        GUI.setItem(GUIUtil.REWARD_PS_TITLE_SLOT, rpTitleMenu());
        GUI.setItem(GUIUtil.REWARD_PS_CLOSE_SLOT, rpCloseMenu());
        GUI.setItem(GUIUtil.REWARD_PS_POSITION_FIRST_SLOT, rpPositionFirstMenu());
        GUI.setItem(GUIUtil.REWARD_PS_POSITION_SECOND_SLOT, rpPositionSecondMenu());
        GUI.setItem(GUIUtil.REWARD_PS_POSITION_THIRD_SLOT, rpPositionThirdMenu());

        player.openInventory(GUI);
    }

    private void openRewardTypeGUI() {
        Inventory GUI = Bukkit.createInventory(null, GUIUtil.REWARD_TS_INVENTORY_SIZE, GUIUtil.REWARD_TS_INVENTORY_TITLE);

        GUI.setItem(GUIUtil.REWARD_PS_INFO_SLOT, rtInfoMenu());
        GUI.setItem(GUIUtil.REWARD_PS_TITLE_SLOT, rpTitleMenu());
        GUI.setItem(GUIUtil.REWARD_PS_CLOSE_SLOT, rtCloseMenu());
        GUI.setItem(GUIUtil.REWARD_TS_ITEM_REWARD_SLOT, rewardItemTypeMenu());
        GUI.setItem(GUIUtil.REWARD_TS_MONEY_REWARD_SLOT, rewardMoneyTypeMenu());
        GUI.setItem(GUIUtil.REWARD_TS_TITLE_REWARD_SLOT, rewardTitleTypeMenu());

        player.openInventory(GUI);
    }

    private void openItemTypeRewardGUI() {
        Inventory GUI = Bukkit.createInventory(null, GUIUtil.REWARD_SELECT_ITEMS_INVENTORY_SIZE, GUIUtil.REWARD_SELECT_ITEMS_TITLE);
        List<Reward> rewardsList = ERRewardService.getInstance().getItemRewardsByPosition( board, this.rankPlace );

        GUI.setItem(GUIUtil.REWARD_ITEM_INFO_SLOT, itemRewardInfoMenu());
        GUI.setItem(1, new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        GUI.setItem(2, new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        GUI.setItem(3, new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        GUI.setItem(GUIUtil.REWARD_ITEM_TITLE_SLOT, itemRewardTitleMenu());
        GUI.setItem(5, new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        GUI.setItem(6, new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        GUI.setItem(7, new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        GUI.setItem(GUIUtil.REWARD_ITEM_BACK_SLOT, itemRewardConfirmMenu());

        if( rewardsList != null ) {
            int currentSlot = 9;
            for( Reward reward : rewardsList ) {
                GUI.setItem(currentSlot, ((ERItemReward) reward).getReward());
                currentSlot++;
            }
        }

        player.openInventory(GUI);
    }

    private ItemStack rpTitleMenu(){
        String[] lores = new String[] {};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_PS_TITLE_MATERIAL,ChatColor.GOLD + "Board: " + board.getName(), lores );
    }

    private ItemStack rpInfoMenu(){
        String[] lores = new String[] {
            ChatColor.GRAY + "Through this GUI you can select",
            ChatColor.GRAY + "which position in the board to",
            ChatColor.GRAY + "give the rewards",
        };
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_PS_INFO_MATERIAL,ChatColor.RED + "Info", lores );
    }

    private ItemStack rpCloseMenu(){
        String[] lores = new String[] {};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_PS_CLOSE_MATERIAL,ChatColor.RED + "Close menu", lores );
    }

    private ItemStack rpPositionFirstMenu(){
        String[] lores = new String[] {
                ChatColor.GOLD + "Set the rewards for the player",
                ChatColor.GOLD + "in first position"};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_PS_POSITION_FIRST_MATERIAL,ChatColor.GREEN + "First position", lores );
    }

    private ItemStack rpPositionSecondMenu(){
        String[] lores = new String[] {
                ChatColor.GOLD + "Set the rewards for the player",
                ChatColor.GOLD + "in second position"};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_PS_POSITION_SECOND_MATERIAL,ChatColor.GREEN + "Second position", lores );
    }

    private ItemStack rpPositionThirdMenu(){
        String[] lores = new String[] {
                ChatColor.GOLD + "Set the rewards for the player",
                ChatColor.GOLD + "in third position"};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_PS_POSITION_THIRD_MATERIAL,ChatColor.GREEN + "Third position", lores );
    }

    private ItemStack rewardItemTypeMenu() {
        String[] lores = new String[] {ChatColor.GOLD + "Item reward"};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_TS_ITEM_REWARD_MATERIAL,ChatColor.GREEN + "Set item rewards", lores );
    }

    private ItemStack rewardMoneyTypeMenu() {
        Optional<Reward> reward = ERRewardService.getInstance().getMoneyRewardByPosition(this.board, this.rankPlace);
        Double currentValue = reward.isPresent() ? (Double)reward.get().getReward() : 0.0;
        String[] lores = new String[] {
                ChatColor.GOLD + "Set money rewards",
                ChatColor.GOLD + "Current value: " + ChatColor.GREEN + currentValue
        };
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_TS_MONEY_REWARD_MATERIAL,ChatColor.GREEN + "Money reward", lores );
    }

    private ItemStack rewardTitleTypeMenu() {
        Optional<Reward> reward = ERRewardService.getInstance().getTitleRewardByPosition(this.board, this.rankPlace);
        String currentValue = reward.isPresent() ? (String)reward.get().getReward() : ChatColor.GRAY + "unset";
        String[] lores = new String[] {
                ChatColor.GOLD + "Set title rewards",
                ChatColor.GOLD + "Current value: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', currentValue)
        };
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_TS_TITLE_REWARD_MATERIAL,ChatColor.GREEN + "Title reward", lores );
    }

    private ItemStack rtInfoMenu(){
        String[] lores = new String[] {
                ChatColor.GRAY + "Through this GUI you can modify",
                ChatColor.GRAY + "each type of reward",
                ChatColor.GRAY + "individually",
        };
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_PS_INFO_MATERIAL,ChatColor.RED + "Info", lores );
    }

    private ItemStack rtCloseMenu(){
        String[] lores = new String[] {};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_PS_CLOSE_MATERIAL,ChatColor.RED + "Back", lores );
    }

    private ItemStack itemRewardTitleMenu(){
        String[] lores = new String[] {};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_ITEM_SELECTION_TITLE_MATERIAL,ChatColor.GOLD + "Board: " + board.getName(), lores );
    }

    private ItemStack itemRewardInfoMenu(){
        String[] lores = new String[] {
                ChatColor.GRAY + "Drag and drop an item",
                ChatColor.GRAY + "in the slots below to",
                ChatColor.GRAY + "add it as a reward",
        };
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_ITEM_SELECTION_INFO_MATERIAL,ChatColor.RED + "Info", lores );
    }

    private ItemStack itemRewardConfirmMenu(){
        String[] lores = new String[] {};
        return GUIUtil.prepareMenuPoint(GUIUtil.REWARD_ITEM_SELECTION_CONFIRM_MATERIAL,ChatColor.GREEN + "Confirm", lores );
    }

}
