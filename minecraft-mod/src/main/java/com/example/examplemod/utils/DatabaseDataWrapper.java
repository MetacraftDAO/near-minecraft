package com.example.examplemod.utils;

import java.util.ArrayList;

final public class DatabaseDataWrapper {
    public static class VerifiedUser {
        public String objectId;
        public String createdAt;
        public String updatedAt;
        public Boolean isVerified;
        public String username;
        public String uuid;
        public String nearAccountId;
    }

    public static class PlayTime {
        public String objectId;
        public String createdAt;
        public String updatedAt;
        public String tikTime;
        public String tokTime;
        public long accumulatedPlayTime;
    }

    public static class VerifiedUserRows {
        public ArrayList<VerifiedUser> results;

        VerifiedUser getFirst() {
            return (results == null || results.isEmpty()) ? null : results.get(0);
        }
    }

    public static class PlayTimeRows {
        public ArrayList<PlayTime> results;

        PlayTime getFirst() {
            return (results == null || results.isEmpty()) ? null : results.get(0);
        }
    }
}
