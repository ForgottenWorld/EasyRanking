package me.kaotich00.easyranking.service;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.reward.Reward;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.api.service.RewardService;
import me.kaotich00.easyranking.reward.types.ERItemReward;
import me.kaotich00.easyranking.reward.types.ERMoneyReward;
import me.kaotich00.easyranking.reward.types.ERTitleReward;
import me.kaotich00.easyranking.storage.StorageFactory;
import me.kaotich00.easyranking.storage.StorageMethod;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ERRewardService implements RewardService {

    private static ERRewardService rewardServiceInstance;
    private final Map<Board, List<Reward>> rewardData;
    private final Map<UUID, String> activeTitles;
    private final Map<UUID, Board> isModifyingBoard;
    private final Map<UUID, Integer> isSelectingItems;
    private final Map<UUID, List<ItemStack>> uncollectedRewards;

    private ERRewardService() {
        if (rewardServiceInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        this.rewardData = new HashMap<>();
        this.isModifyingBoard = new HashMap<>();
        this.isSelectingItems = new HashMap<>();
        this.activeTitles = new HashMap<>();
        this.uncollectedRewards = new HashMap<>();
    }

    public static ERRewardService getInstance() {
        if(rewardServiceInstance == null) {
            rewardServiceInstance = new ERRewardService();
        }
        return rewardServiceInstance;
    }

    @Override
    public void registerBoard(Board board) {
        if( !rewardData.containsKey(board) ) {
            rewardData.put(board, new ArrayList<>());
        }
    }

    @Override
    public void newItemReward(ItemStack itemStack, Board board, int position) {
        Reward reward = new ERItemReward(itemStack, position);
        rewardData.get(board).add(reward);
    }

    @Override
    public void newMoneyReward(Double money, Board board, int position) {
        Reward reward = new ERMoneyReward(money, position);
        rewardData.get(board).add(reward);
    }

    @Override
    public void newTitleReward(String title, Board board, int position) {
        Reward reward = new ERTitleReward(title, position);
        rewardData.get(board).add(reward);

    }

    @Override
    public void clearItemReward(Board board, int rankPosition) {
        if(!rewardData.containsKey(board)) {
            return;
        }
        List<Reward> rewardsList = rewardData.get(board).stream()
                .filter(r -> (r.getRankingPosition() == rankPosition && r.getRewardType() == GUIUtil.ITEM_TYPE))
                .collect(Collectors.toList());
        rewardData.get(board).removeAll(rewardsList);
    }

    @Override
    public void clearMoneyReward(Board board, int rankPosition) {
        if(!rewardData.containsKey(board)) {
            return;
        }
        List<Reward> rewardsList = rewardData.get(board).stream().filter(r -> (r.getRankingPosition() == rankPosition && r.getRewardType() == GUIUtil.MONEY_TYPE)).collect(Collectors.toList());
        rewardData.get(board).removeAll(rewardsList);
    }

    @Override
    public void clearTitleReward(Board board, int rankPosition) {
        if(!rewardData.containsKey(board)) {
            return;
        }
        List<Reward> rewardsList = rewardData.get(board).stream()
                .filter(r -> (r.getRankingPosition() == rankPosition && r.getRewardType() == GUIUtil.TITLE_TYPE))
                .collect(Collectors.toList());
        rewardData.get(board).removeAll(rewardsList);
    }

    @Override
    public void collectRewards() {
        BoardService boardService = ERBoardService.getInstance();
        Set<Board> boardsList = boardService.getBoards();

        for (Board board : boardsList) {

            Bukkit.getServer().broadcastMessage("\n" + ChatColor.DARK_AQUA + board.getName());
            List<UUID> userScores = boardService.sortScores(board);

            boolean dataEmpty = true;

            for (int i = 0; i < 3; i ++) {

                int playerRankPosition = i + 1;

                if (userScores.size() < playerRankPosition)
                    break;

                dataEmpty = false;

                UUID playerUUID = userScores.get(i);

                Player player = Bukkit.getPlayer(playerUUID);
                OfflinePlayer offlinePlayer = null;

                if (player == null) {
                    offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                    if (offlinePlayer.hasPlayedBefore()) {
                        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + String.valueOf(playerRankPosition) +
                                "." + ChatColor.GOLD + " " + offlinePlayer.getName() + ChatColor.DARK_GRAY +
                                " (" + ChatColor.GREEN +
                                ChatFormatter.thousandSeparator(board.getUserScore(playerUUID).orElse(0f).longValue()) +
                                " " + board.getUserScoreName() + ChatColor.DARK_GRAY + ")");
                    }
                } else {
                    Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + String.valueOf(playerRankPosition) +
                            "." + ChatColor.GOLD + " " + player.getName() + ChatColor.DARK_GRAY + " (" +
                            ChatColor.GREEN +
                            ChatFormatter.thousandSeparator(board.getUserScore(playerUUID).orElse(0f).longValue()) +
                            " " + board.getUserScoreName() + ChatColor.DARK_GRAY + ")");
                }

                List<Reward> rewardsList = getRewardsByPosition(board, playerRankPosition);

                if (rewardsList == null)
                    continue;

                for (Reward reward : rewardsList) {
                    if (reward instanceof ERItemReward) {
                        ItemStack itemType = ((ERItemReward)reward).getReward();

                        if (player == null)
                            addUncollectedItem(playerUUID, itemType);
                        else {
                            if (player.getInventory().addItem(itemType).size() != 0)
                                player.getWorld().dropItem(player.getLocation(), itemType);
                        }
                    }

                    if (reward instanceof ERMoneyReward) {
                        Double amount = ((ERMoneyReward)reward).getReward();

                        if( player != null)
                            Easyranking.getEconomy().depositPlayer(player,amount);
                        else
                            Easyranking.getEconomy().depositPlayer(offlinePlayer,amount);
                    }

                    if (reward instanceof ERTitleReward) {
                        String title = ((ERTitleReward)reward).getReward();
                        setUserTitle(playerUUID,title);
                    }
                }

                if (player != null) {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                    //scoreboardService.updateScoreBoard(player.getUniqueId());
                }
            }

            if (dataEmpty)
                Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "No data found");

            // Clear all data from memory
            if (board.getFgReset())
                board.clearAllScores();

        }

        // Clear all data from database
        StorageMethod storage = StorageFactory.getInstance().getStorageMethod();
        CompletableFuture.runAsync(storage::clearBoardsData);

    }

    @Override
    public void deleteBoardRewards(Board board) {
        rewardData.remove(board);
    }

    @Override
    public List<Reward> getRewardsByPosition(Board board, int position) {
        if( !rewardData.containsKey(board) ) {
            return null;
        }
        return rewardData.get(board).stream().filter(r -> r.getRankingPosition() == position).collect(Collectors.toList());
    }

    @Override
    public List<Reward> getItemRewardsByPosition(Board board, int position) {
        if( !rewardData.containsKey(board) ) {
            return null;
        }
        return rewardData.get(board).stream().filter(r -> (r.getRewardType() == GUIUtil.ITEM_TYPE && r.getRankingPosition() == position )).collect(Collectors.toList());
    }

    @Override
    public Optional<Reward> getMoneyRewardByPosition(Board board, int position) {
        if( !rewardData.containsKey(board) ) {
            return Optional.empty();
        }
        return rewardData.get(board).stream().filter(r -> (r.getRewardType() == GUIUtil.MONEY_TYPE && r.getRankingPosition() == position )).findFirst();
    }

    @Override
    public Optional<Reward> getTitleRewardByPosition(Board board, int position) {
        if( !rewardData.containsKey(board) ) {
            return Optional.empty();
        }
        return rewardData.get(board).stream().filter(r -> (r.getRewardType() == GUIUtil.TITLE_TYPE && r.getRankingPosition() == position )).findFirst();
    }

    @Override
    public List<ItemStack> getUncollectedRewardForUser(UUID uuid) {
        return this.uncollectedRewards.get(uuid);
    }

    @Override
    public Map<Board, List<Reward>> getRewardsList() {
        return this.rewardData;
    }

    @Override
    public Map<UUID, List<ItemStack>> getUncollectedRewards() {
        return this.uncollectedRewards;
    }

    @Override
    public void removeUncollectedItemsForPlayer(UUID uuid) {
        this.uncollectedRewards.remove(uuid);
    }

    @Override
    public void addUncollectedItem(UUID uuid, ItemStack itemStack) {
        this.uncollectedRewards.computeIfAbsent(uuid, k -> new ArrayList<>());
        this.uncollectedRewards.get(uuid).add(itemStack);
    }

    @Override
    public void addModifyingPlayer(UUID player, Board board) {
        isModifyingBoard.put(player,board);
    }

    @Override
    public void removeModifyingPlayer(UUID player) {
        isModifyingBoard.remove(player);
    }

    @Override
    public void addItemSelectionRank(UUID player, int rankPlace) {
        isSelectingItems.put(player,rankPlace);
    }

    @Override
    public void removeItemSelectionRank(UUID player) {
        isSelectingItems.remove(player);
    }

    @Override
    public Board getBoardFromModifyingPlayer(UUID playerUniqueId) {
        return isModifyingBoard.getOrDefault(playerUniqueId, null);
    }

    @Override
    public int getItemSelectionRankFromModifyingPlayer(UUID playerUniqueId) {
        return isSelectingItems.getOrDefault(playerUniqueId, 0);
    }

    @Override
    public Optional<String> getUserTitleIfActive(UUID player) {
        return Optional.ofNullable(this.activeTitles.get(player));
    }

    @Override
    public void setUserTitle(UUID playerUUID, String title) {
        this.activeTitles.put(playerUUID, title);

        /*
        // If Towny is enabled, easyranking will hook into it
        if (Bukkit.getPluginManager().getPlugin("Towny") != null) {
            TownyAPI townyAPI = TownyAPI.getInstance();

            try{
                Player player = Bukkit.getPlayer(playerUUID);

                if (player != null) {
                    Resident resident = townyAPI.getDataSource().getResident(player.getName());
                    resident.setSurname(title);
                }

            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

         */
    }

    @Override
    public void removeUserTitle(UUID uuid) {
        this.activeTitles.remove(uuid);
    }

}
