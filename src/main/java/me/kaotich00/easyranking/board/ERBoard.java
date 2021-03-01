package me.kaotich00.easyranking.board;

import me.kaotich00.easyranking.api.board.Board;

import java.util.*;

public class ERBoard implements Board {

    private String id;
    private String name;
    private String description;
    private int maxShownPlayers;
    private String userScoreName;
    private boolean isDefault;
    private Map<UUID,Float> userScore;
    private boolean fgReset;

    public ERBoard(String id, String name, String description, int maxShownPlayers, String userScoreName, boolean isDefault, boolean fgReset) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.maxShownPlayers = maxShownPlayers;
        this.userScoreName = userScoreName;
        this.isDefault = isDefault;
        this.userScore = new HashMap<>();
        this.fgReset = fgReset;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public int getMaxShownPlayers() {
        return this.maxShownPlayers;
    }

    @Override
    public String getUserScoreName() {
        return this.userScoreName;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setMaxShownPlayers(int maxShownPlayers) {
        this.maxShownPlayers = maxShownPlayers;
    }

    @Override
    public void setUserScoreName(String userScoreName) {
        this.userScoreName = userScoreName;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public void clearUserScore(UUID player) {
        this.userScore.remove(player);
    }

    @Override
    public void clearAllScores() {
        this.userScore.clear();
    }

    @Override
    public Optional<Float> getUserScore(UUID player) {
        return Optional.ofNullable(this.userScore.get(player));
    }

    @Override
    public Map<UUID, Float> getAllScores() {
        return this.userScore;
    }

    @Override
    public void setUserScore(UUID uuid, Float amount) {
        this.userScore.put(uuid,amount);
    }

    @Override
    public void addUser(UUID player) {
        this.userScore.put(player, 0f);
    }

    @Override
    public void addUser(UUID player, Float amount) {
        this.userScore.put(player, amount);
    }

    @Override
    public boolean getFgReset() {
        return this.fgReset;
    }

    @Override
    public void setFgReset(boolean fgReset) {
        this.fgReset = fgReset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ERBoard erBoard = (ERBoard) o;
        return getMaxShownPlayers() == erBoard.getMaxShownPlayers() &&
                getId().equals(erBoard.getId()) &&
                getName().equals(erBoard.getName()) &&
                getDescription().equals(erBoard.getDescription()) &&
                getUserScoreName().equals(erBoard.getUserScoreName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getMaxShownPlayers(), getUserScoreName());
    }
}
