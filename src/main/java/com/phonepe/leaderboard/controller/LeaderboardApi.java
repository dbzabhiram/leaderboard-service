package com.phonepe.leaderboard.controller;

import com.phonepe.leaderboard.data.request.CreateLeaderboardRequest;
import com.phonepe.leaderboard.data.response.CreateLeaderboardResponse;
import com.phonepe.leaderboard.data.response.GetLeaderboardResponse;
import com.phonepe.leaderboard.data.response.LeaderboardEntryResponse;
import com.phonepe.leaderboard.data.response.ScoreSubmissionResponse;
import com.phonepe.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping("/api/leaderboard")
@RestController
@RequiredArgsConstructor
public class LeaderboardApi {

    private final LeaderboardService leaderboardService;

    @PostMapping
    public ResponseEntity<CreateLeaderboardResponse> createLeaderboard(@RequestBody CreateLeaderboardRequest request) {
        return new ResponseEntity<>(
                leaderboardService.createLeaderboard(request.getGameId(), request.getStartTime(), request.getEndTime()),
                CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetLeaderboardResponse> getLeaderboard(@PathVariable String id) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(id));
    }

    @PutMapping("/game/{gameId}/player/{playerId}")
    public ResponseEntity<ScoreSubmissionResponse> submitScore(@PathVariable String gameId,
                                                               @PathVariable String playerId,
                                                               @RequestParam int score) {
        return ResponseEntity.ok(leaderboardService.submitScore(gameId, playerId, score));
    }

    @GetMapping("/{id}/top")
    public ResponseEntity<List<LeaderboardEntryResponse>> getTopNPlayers(@PathVariable String id,
                                                                         @RequestParam int nPlayers) {
        return ResponseEntity.ok(leaderboardService.getTopNPlayers(id, nPlayers));
    }

    @GetMapping("/{id}/player/{playerId}/next")
    public ResponseEntity<List<LeaderboardEntryResponse>> getNextNPlayers(@PathVariable String id,
                                                                          @PathVariable String playerId,
                                                                          @RequestParam int nPlayers) {
        return ResponseEntity.ok(leaderboardService.getNextNPlayers(id, playerId, nPlayers));
    }

    @GetMapping("{id}/player/{playerId}/prev")
    public ResponseEntity<List<LeaderboardEntryResponse>> getPrevNPlayers(@PathVariable String id,
                                                                          @PathVariable String playerId,
                                                                          @RequestParam int nPlayers) {
        return ResponseEntity.ok(leaderboardService.getPrevNPlayers(id, playerId, nPlayers));
    }

}
