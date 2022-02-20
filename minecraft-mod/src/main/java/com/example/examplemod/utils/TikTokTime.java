package com.example.examplemod.utils;

import java.time.Duration;
import java.time.Instant;

import com.example.examplemod.utils.DatabaseDataWrapper.PlayTime;
import com.example.examplemod.utils.DatabaseDataWrapper.VerifiedUser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TikTokTime {
    DatabaseConnector db;
    private static final Logger LOGGER = LogManager.getLogger();

    public TikTokTime(DatabaseConnector database) {
        db = database;
    }

    public PlayTime getPlayTimeIfNeeded(String nearAccountId) {
        PlayTime playTime = db.getPlayTime(nearAccountId);
        if (playTime == null) {
            db.writePlayTime(nearAccountId);
            playTime = db.getPlayTime(nearAccountId);
        }
        return playTime;
    }

    public boolean tik(String uuid) {
        LOGGER.info("Start tik!");
        VerifiedUser user = db.getVerifiedUser(uuid);
        PlayTime playTime = getPlayTimeIfNeeded(user.nearAccountId);
        if (playTime == null) {
            LOGGER.atError().log("Failed to create PlayTime object!");
            return false;
        }
        String tikTime = Instant.now().toString();
        LOGGER.info(String.format("Write nearAccountId: %s, tikTime: %s",
                user.nearAccountId, tikTime));
        return db.updatePlayTimeTikTime(playTime.objectId, tikTime);
    }

    public boolean tok(String uuid) {
        LOGGER.info("Start tok!");
        VerifiedUser user = db.getVerifiedUser(uuid);
        PlayTime playTime = getPlayTimeIfNeeded(user.nearAccountId);
        if (playTime == null) {
            LOGGER.atError().log("Failed to create PlayTime object!");
            return false;
        }

        Instant tik;
        if (playTime.tikTime == null) {
            LOGGER.info(("No tik time is set yet."));
            tik = Instant.now();
        } else {
            tik = Instant.parse(playTime.tikTime);
        }
        Instant tok = Instant.now();
        if (!tik.isBefore(tok)) {
            LOGGER.atError().log("Error! tik time is in the future!");
            tik = tok;
        }

        // Calculate accumulated time.
        Duration timeElapsed = Duration.between(tik, tok);
        long accumulatedPlayTime = playTime.accumulatedPlayTime + timeElapsed.getSeconds();

        LOGGER.info(String.format("Write nearAccountId: %s, tikTime: %s, tokTime: %s, accumulatedPlayTime: %d",
                user.nearAccountId, tik.toString(), tok.toString(), accumulatedPlayTime));
        return db.updatePlayTime(playTime.objectId, tik.toString(), tok.toString(), accumulatedPlayTime);
    }
}
