package me.kaotich00.easyranking.service;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.reward.Reward;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.api.service.RewardService;
import me.kaotich00.easyranking.reward.types.ERItemReward;
import me.kaotich00.easyranking.reward.types.ERMoneyReward;
import me.kaotich00.easyranking.reward.types.ERTitleReward;
import me.kaotich00.easyranking.utils.ChatFormatter;
import me.kaotich00.easyranking.utils.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ERRewardService implements RewardService {

    private static ERRewardService rewardServiceInstance;
    private Map<Board, List<Reward>> rewardData;
    private Map<UUID, String> activeTitles;
    private Map<UUID, Board> isModifyingBoard;
    private Map<UUID, Integer> isSelectingItems;

    private ERRewardService() {
        if (rewardServiceInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        this.rewardData = new HashMap<>();
        this.isModifyingBoard = new HashMap<>();
        this.isSelectingItems = new HashMap<>();
        this.activeTitles = new HashMap<>();
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
        List<Reward> rewardsList = rewardData.get(board).stream().filter(r -> (r.getRankingPosition() == rankPosition && r.getRewardType() == GUIUtil.ITEM_TYPE)).collect(Collectors.toList());
        rewardData.get(board).removeAll(rewardsList);
    }

    @Override
    public void clearMoneyReward(Board board, int rankPosition) {
        List<Reward> rewardsList = rewardData.get(board).stream().filter(r -> (r.getRankingPosition() == rankPosition && r.getRewardType() == GUIUtil.MONEY_TYPE)).collect(Collectors.toList());
        rewardData.get(board).removeAll(rewardsList);
    }

    @Override
    public void clearTitleReward(Board board, int rankPosition) {
        List<Reward> rewardsList = rewardData.get(board).stream().filter(r -> (r.getRankingPosition() == rankPosition && r.getRewardType() == GUIUtil.TITLE_TYPE)).collect(Collectors.toList());
        rewardData.get(board).removeAll(rewardsList);
    }

    @Override
    public void collectRewards() {
        BoardService boardService = ERBoardService.getInstance();
        Set<Board> boardsList = boardService.getBoards();

        for( Board board : boardsList ) {

            Bukkit.getServer().broadcastMessage("\n" + ChatColor.DARK_AQUA + board.getName());

            List<UUID> userScores = boardService.sortScores(board);
            boolean dataEmpty = true;
            for( int i = 0; i < 3; i ++ ) {
                Integer position = i + 1;

                if( userScores.size() < position ) {
                    continue;
                }

                dataEmpty = false;
                UUID playerUUID = userScores.get(i);

                Player player = Bukkit.getPlayer(playerUUID);
                if( player == null ) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                    if( offlinePlayer != null ) {
                        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + String.valueOf(position) + "." + ChatColor.GOLD + " " + offlinePlayer.getName() + ChatColor.DARK_GRAY + " (" + ChatColor.GREEN + ChatFormatter.thousandSeparator(board.getUserScore(playerUUID).get().intValue()) + " " + board.getUserScoreName() + ChatColor.DARK_GRAY + ")");
                    }
                    continue;
                } else {
                    Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + String.valueOf(position) + "." + ChatColor.GOLD + " " + player.getPlayerListName() + ChatColor.DARK_GRAY + " (" + ChatColor.GREEN + ChatFormatter.thousandSeparator(board.getUserScore(playerUUID).get().intValue()) + " " + board.getUserScoreName() + ChatColor.DARK_GRAY + ")");
                }

                List<Reward> rewardsList = getRewardsByPosition(board, position);
                if( rewardsList == null ) {
                    continue;
                }

                for( Reward reward : rewardsList ) {
                    if (reward instanceof ERItemReward) {
                        ItemStack itemType = ((ERItemReward)reward).getReward();
                        if(player.getInventory().addItem(itemType).size() != 0) {
                            player.getWorld().dropItem(player.getLocation(), itemType);
                        }
                    }
                    if (reward instanceof ERMoneyReward) {
                        Double amount = ((ERMoneyReward)reward).getReward();
                        Easyranking.getEconomy().depositPlayer(player,amount);
                    }
                    if (reward instanceof ERTitleReward) {
                        String title = ((ERTitleReward)reward).getReward();
                        setUserTitle(player.getUniqueId(), title);
                    }
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
            }
            if( dataEmpty ) {
                Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "No data found");
            }
        }
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
    public Map<Board, List<Reward>> getRewardsList() {
        return this.rewardData;
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
        return isModifyingBoard.containsKey(playerUniqueId) ? isModifyingBoard.get(playerUniqueId) : null;
    }

    @Override
    public int getItemSelectionRankFromModifyingPlayer(UUID playerUniqueId) {
        return isSelectingItems.containsKey(playerUniqueId) ? isSelectingItems.get(playerUniqueId) : 0;
    }

    @Override
    public Optional<String> getUserTitleIfActive(UUID player) {
        return Optional.ofNullable(this.activeTitles.get(player));
    }

    @Override
    public void setUserTitle(UUID player, String title) {
        this.activeTitles.put(player, title);
    }

    @Override
    public void removeUserTitle(UUID player) {
        this.activeTitles.remove(player);
    }

}
