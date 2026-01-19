package com.phonepe.leaderboard.data.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ScoreSubmissionResponse {
    private int updatedLeaderboardCount;
    private boolean dataUpdated;
}
