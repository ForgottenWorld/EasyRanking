package me.kaotich00.easyranking.api.board;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Board {

    String getId();

    String getName();

    String getDescription();

    int getMaxShownPlayers();

    String getUserScoreName();

    void setName(String name);

    void setDescription(String description);

    void setMaxShownPlayers(int maxShownplayers);

    void setUserScoreName(String userScoreName);

    boolean isDefault();

    void clearUserScore(UUID player);

    void clearAllScores();

    Optional<Float> getUserScore(UUID player);

    Map<UUID,Float> getAllScores();

    void setUserScore(UUID uuid, Float amount);

    void addUser(UUID player);

    void addUser(UUID player, Float amount);

    boolean getFgReset();

    void setFgReset(boolean fgReset);

}
