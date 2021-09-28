package me.kaotich00.easyranking.listener.gui.reward;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.RewardService;
import me.kaotich00.easyranking.gui.reward.RewardGUI;
import me.kaotich00.easyranking.reward.conversation.prompt.MoneyAmountPrompt;
import me.kaotich00.easyranking.reward.conversation.prompt.TitleRewardPrompt;
import me.kaotich00.easyranking.service.ERRewardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.GUIUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIRewardListener implements Listener, ConversationAbandonedListener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        dispatchClickEvent(event);
    }

    @EventHandler
    public void onItemSelectInventoryClose(InventoryCloseEvent event) {
        dispatchCloseEvent(event);
    }

    private void dispatchClickEvent(InventoryClickEvent event ) {
        if (event.getCurrentItem() == null)
            return;

        switch(event.getView().getTitle()) {
            case GUIUtil.REWARD_TS_INVENTORY_TITLE:
                if(event.getRawSlot() <= event.getInventory().getSize()) {
                    handleClickTypeSelection((Player) event.getWhoClicked(), event.getCurrentItem().getType());
                }
                event.setCancelled(true);
                break;
            case GUIUtil.REWARD_PS_INVENTORY_TITLE:
                if(event.getRawSlot() <= event.getInventory().getSize()) {
                    handleClickPositionSelection((Player) event.getWhoClicked(), event.getCurrentItem().getType());
                }
                event.setCancelled(true);
                break;
            case GUIUtil.REWARD_SELECT_ITEMS_TITLE:
                handleClickItemReward((Player) event.getWhoClicked(), event.getCurrentItem().getType());
                if( event.getRawSlot() >= 0 && event.getRawSlot() <= 8 )
                    event.setCancelled(true);
                break;
            default:
        }
    }

    private void dispatchCloseEvent(InventoryCloseEvent event) {
        switch(event.getView().getTitle()) {
            case GUIUtil.REWARD_SELECT_ITEMS_TITLE:
                handleCloseItemReward((Player) event.getPlayer());
                break;
        }
    }

    private void handleClickPositionSelection(Player player, Material clickedMenu) {
        RewardService rewardService = ERRewardService.getInstance();
        switch( clickedMenu ) {
            /* Close menu */
            case BARRIER:
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 10, 1);
                break;
            /* First position */
            case DIAMOND_BLOCK:
                RewardGUI guiFirstPosition = new RewardGUI(player, rewardService.getBoardFromModifyingPlayer(player.getUniqueId()), GUIUtil.FIRST_PLACE);
                guiFirstPosition.openGUI(GUIUtil.REWARD_TS_STEP);
                player.playSound( player.getLocation(), Sound.BLOCK_LEVER_CLICK, 10, 1 );
                break;
            /* Second position */
            case GOLD_BLOCK:
                RewardGUI guiSecondPosition = new RewardGUI(player, rewardService.getBoardFromModifyingPlayer(player.getUniqueId()), GUIUtil.SECOND_PLACE);
                guiSecondPosition.openGUI(GUIUtil.REWARD_TS_STEP);
                player.playSound( player.getLocation(), Sound.BLOCK_LEVER_CLICK, 10, 1 );
                break;
            /* Third position */
            case IRON_BLOCK:
                RewardGUI guiThirdPosition = new RewardGUI(player, rewardService.getBoardFromModifyingPlayer(player.getUniqueId()), GUIUtil.THIRD_PLACE);
                guiThirdPosition.openGUI(GUIUtil.REWARD_TS_STEP);
                player.playSound( player.getLocation(), Sound.BLOCK_LEVER_CLICK, 10, 1 );
                break;
        }
    }

    private void handleClickTypeSelection(Player player, Material clickedMenu) {
        RewardService rewardService = ERRewardService.getInstance();
        Board board = rewardService.getBoardFromModifyingPlayer(player.getUniqueId());
        int rewardRank = rewardService.getItemSelectionRankFromModifyingPlayer(player.getUniqueId());

        switch( clickedMenu ) {
            /* Back arrow */
            case BARRIER:
                RewardGUI prevGui = new RewardGUI(player, board);
                prevGui.openGUI(GUIUtil.REWARD_PS_STEP);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 10, 1);
                break;
            /* Item reward */
            case DIAMOND_SWORD:
                RewardGUI selectItemReward = new RewardGUI(player, board, rewardRank);
                selectItemReward.openGUI(GUIUtil.REWARD_SELECT_ITEMS_STEP);
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 10, 1);
                break;
            /* Money reward */
            case GOLD_NUGGET:
                ConversationFactory moneyFactory = new ConversationFactory(Easyranking.getPlugin(Easyranking.class))
                        .withModality(false)
                        .withFirstPrompt(new MoneyAmountPrompt(board, rewardRank))
                        .withEscapeSequence("cancel")
                        .withTimeout(20)
                        .thatExcludesNonPlayersWithMessage("This conversation can only be initiated by a player")
                        .addConversationAbandonedListener(this);
                moneyFactory.buildConversation(player).begin();
                player.closeInventory();

                break;
            /* Title reward */
            case NAME_TAG:
                ConversationFactory titleFactory = new ConversationFactory(Easyranking.getPlugin(Easyranking.class))
                        .withModality(false)
                        .withFirstPrompt(new TitleRewardPrompt(board, rewardRank))
                        .withEscapeSequence("cancel")
                        .withTimeout(20)
                        .thatExcludesNonPlayersWithMessage("This conversation can only be initiated by a player")
                        .addConversationAbandonedListener(this);
                titleFactory.buildConversation(player).begin();
                player.closeInventory();
                break;
        }
    }

    private void handleClickItemReward(Player player, Material clickedMenu) {
        switch( clickedMenu ) {
            /* Confirm item rewards */
            case EMERALD:
                RewardService rewardService = ERRewardService.getInstance();
                Board board = rewardService.getBoardFromModifyingPlayer(player.getUniqueId());
                int rewardRank = rewardService.getItemSelectionRankFromModifyingPlayer(player.getUniqueId());

                addItemRewards(player, player.getOpenInventory().getTopInventory());
                RewardGUI prevGui = new RewardGUI(player, board, rewardRank);
                prevGui.openGUI(GUIUtil.REWARD_TS_STEP);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                break;
        }
    }

    private void addItemRewards(Player player, Inventory inventory) {
        RewardService rewardService = ERRewardService.getInstance();
        Board board = rewardService.getBoardFromModifyingPlayer(player.getUniqueId());
        int rewardRank = rewardService.getItemSelectionRankFromModifyingPlayer(player.getUniqueId());
        rewardService.clearItemReward(board, rewardRank);

        for( int i = 9; i < inventory.getSize(); i++ ) {
            ItemStack reward = inventory.getItem(i);
            if( reward != null ) {
                rewardService.newItemReward(reward.clone(), board, rewardRank);
                player.sendMessage(
                        ChatFormatter.formatSuccessMessage(
                                "Board: " + ChatColor.GOLD + board.getId() +
                                        ChatColor.GREEN + " - Successfully added " +
                                        ChatColor.GOLD + reward.getType().getKey().getKey() + " " +
                                        ChatColor.GOLD + reward.getItemMeta().getDisplayName() +
                                        ChatColor.GREEN + " x " +
                                        ChatColor.GOLD + reward.getAmount() +
                                        ChatColor.GREEN + " for rank position " + ChatColor.GOLD + rewardRank
                        )
                );
            }
        }
    }

    private void handleCloseItemReward(Player player) {
        /*RewardService rewardService = ERRewardService.getInstance();
        Board board = rewardService.getBoardFromModifyingPlayer(player.getUniqueId());
        int rewardRank = rewardService.getItemSelectionRankFromModifyingPlayer(player.getUniqueId());*/
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.gracefulExit()) {
            abandonedEvent.getContext().getForWhom().sendRawMessage(ChatFormatter.formatSuccessMessage("Setup completed!"));
        } else {
            abandonedEvent.getContext().getForWhom().sendRawMessage(ChatFormatter.formatErrorMessage("Setup canceled!"));
        }
    }
}
