package com.phonepe.leaderboard.service;

import com.phonepe.leaderboard.data.response.CreateLeaderboardResponse;
import com.phonepe.leaderboard.data.response.GetLeaderboardResponse;
import com.phonepe.leaderboard.data.response.LeaderboardEntryResponse;
import com.phonepe.leaderboard.data.response.ScoreSubmissionResponse;

import java.util.List;

public interface LeaderboardService {

    CreateLeaderboardResponse createLeaderboard(String gameId, long startTime, long endTime);

    GetLeaderboardResponse getLeaderboard(String id);

    ScoreSubmissionResponse submitScore(String gameId, String playerId, int score);

    List<LeaderboardEntryResponse> getTopNPlayers(String leaderboardId,
                                                  int nPlayers);

    List<LeaderboardEntryResponse> getNextNPlayers(String leaderboardId,
                                                   String playerId,
                                                   int nPlayers);

    List<LeaderboardEntryResponse> getPrevNPlayers(String leaderboardId,
                                                   String playerId,
                                                   int nPlayers);

}
