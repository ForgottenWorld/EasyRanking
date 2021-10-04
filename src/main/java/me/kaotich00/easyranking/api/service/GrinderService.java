package me.kaotich00.easyranking.api.service;

import java.util.UUID;

public interface GrinderService {
    void scheduleGrinderTask();

    void stopGrinderTask();

    boolean addGrinderUUID(UUID uuid, Long mills);

}
