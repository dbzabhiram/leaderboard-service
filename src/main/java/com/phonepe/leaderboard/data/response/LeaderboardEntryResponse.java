package com.phonepe.leaderboard.data.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LeaderboardEntryResponse {
    private String playerId;
    private int score;
}
