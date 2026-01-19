package com.phonepe.leaderboard.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateLeaderboardRequest {
    private String gameId;
    private long startTime;
    private long endTime;
}
