package com.phonepe.leaderboard.util;

import com.phonepe.leaderboard.data.model.Leaderboard;
import com.phonepe.leaderboard.data.model.LeaderboardEntry;
import com.phonepe.leaderboard.data.response.GetLeaderboardResponse;
import com.phonepe.leaderboard.data.response.LeaderboardEntryResponse;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class LeaderboardUtil {

    public static Leaderboard createLeaderboardEntity(String gameId, long startTime, long endTime) {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setGameId(gameId);
        leaderboard.setStartTime(startTime);
        leaderboard.setEndTime(endTime);
        return leaderboard;
    }

    public static GetLeaderboardResponse getLeaderboardResponse(List<LeaderboardEntry> leaderboardEntries) {
        List<GetLeaderboardResponse.Player> players = new ArrayList<>();
        for (LeaderboardEntry entry: leaderboardEntries) {
            players.add(
                    new GetLeaderboardResponse.Player(
                            entry.getPlayerId(),
                            entry.getScore()
                    )
            );
        }
        return GetLeaderboardResponse.builder().scores(players).build();
    }

    public static List<LeaderboardEntryResponse> getLeaderboardEntryResponse(List<LeaderboardEntry> entries) {
        return entries.stream()
                .map(e -> LeaderboardEntryResponse.builder().playerId(e.getPlayerId()).score(e.getScore()).build())
                .toList();
    }

}
