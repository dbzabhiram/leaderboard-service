package com.phonepe.leaderboard.service;

import com.phonepe.leaderboard.data.model.Leaderboard;
import com.phonepe.leaderboard.data.model.LeaderboardEntry;
import com.phonepe.leaderboard.data.response.CreateLeaderboardResponse;
import com.phonepe.leaderboard.data.response.GetLeaderboardResponse;
import com.phonepe.leaderboard.data.response.LeaderboardEntryResponse;
import com.phonepe.leaderboard.data.response.ScoreSubmissionResponse;
import com.phonepe.leaderboard.repository.LeaderboardEntryRepository;
import com.phonepe.leaderboard.repository.LeaderboardRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.phonepe.leaderboard.constants.Constants.*;
import static com.phonepe.leaderboard.util.LeaderboardUtil.*;
import static java.lang.System.currentTimeMillis;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final LeaderboardEntryRepository leaderboardEntryRepository;

    @Override
    @Transactional
    public CreateLeaderboardResponse createLeaderboard(String gameId, long startTime, long endTime) {
        validateCreateLeaderboardRequest(gameId, startTime, endTime);
        Leaderboard leaderboard = createLeaderboardEntity(gameId, startTime, endTime);
        Leaderboard savedEntity = leaderboardRepository.save(leaderboard);
        return CreateLeaderboardResponse.builder().leaderboardId(savedEntity.getId()).build();
    }

    private void validateCreateLeaderboardRequest(String gameId, long startTime, long endTime) {
        if (gameId == null || gameId.isEmpty()) {
            throw new RuntimeException("GameId is invalid.");
        }
        if (startTime >= endTime) {
            throw new RuntimeException("Leaderboard startTime is greater than or equal to endTime, which is not possible.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GetLeaderboardResponse getLeaderboard(String id) {
        validateGetLeaderboardRequest(id);
        Optional<Leaderboard> leaderboard = leaderboardRepository.findById(id);
        if (leaderboard.isEmpty()) {
            throw new RuntimeException("No leaderboard found for given id.");
        }
        List<LeaderboardEntry> leaderboardEntries = leaderboardEntryRepository.findByLeaderboardIdOrderByScoreDesc(
                leaderboard.get().getId()
        );
        return getLeaderboardResponse(leaderboardEntries);
    }

    private void validateGetLeaderboardRequest(String leaderboardId) {
        if (leaderboardId == null || leaderboardId.isEmpty()) {
            throw new RuntimeException("LeaderboardId is invalid.");
        }
    }

    @Override
    @Transactional
    public ScoreSubmissionResponse submitScore(String gameId, String playerId, int newScore) {
        validateSubmitScoreRequest(gameId, playerId, newScore);
        List<Leaderboard> leaderboards = leaderboardRepository.findByGameId(gameId);
        if (leaderboards.isEmpty()) {
            throw new RuntimeException("No leaderboard found for given gameId.");
        }
        AtomicInteger updatedCount = updateScoresInActiveLeaderboardsIfEligible(playerId, newScore, leaderboards, currentTimeMillis());
        return ScoreSubmissionResponse.builder()
                .updatedLeaderboardCount(updatedCount.get())
                .dataUpdated(updatedCount.get() != 0)
                .build();
    }

    private void validateSubmitScoreRequest(String gameId, String playerId, int newScore) {
        if (gameId == null || gameId.isEmpty()) {
            throw new RuntimeException("GameId is invalid.");
        }
        if (playerId == null || playerId.isEmpty()) {
            throw new RuntimeException("PlayerId is invalid.");
        }
        if (newScore < LOWEST_SCORE_LIMIT || newScore > HIGHEST_SCORE_LIMIT) {
            throw new RuntimeException("Game score is invalid.");
        }
    }

    private AtomicInteger updateScoresInActiveLeaderboardsIfEligible(String playerId, int newScore, List<Leaderboard> leaderboards, long currentTime) {
        AtomicInteger updatedCount = new AtomicInteger(0);

        for (Leaderboard leaderboard: leaderboards) {
            // we need to ignore updates to INACTIVE leaderboards
            if (currentTime > leaderboard.getEndTime()) {
                log.warn("leaderboardId: {} is INACTIVE, ignoring playerId: {} newScore: {} for it", leaderboard.getId(), playerId, newScore);
                continue;
            }

            // First try atomic update
            int updated = leaderboardEntryRepository.updateIfHigherScore(
                    leaderboard.getId(), playerId, newScore
            );

            if (updated == 0) {
                // Either no row exists OR newScore <= existing score
                leaderboardEntryRepository.findByLeaderboardIdAndPlayerId(leaderboard.getId(), playerId)
                        .ifPresentOrElse(
                                entry -> {
                                    // Score exists but not higher → ignore
                                },
                                () -> {
                                    // No entry exists → insert new
                                    LeaderboardEntry entry = new LeaderboardEntry();
                                    entry.setLeaderboard(leaderboard);
                                    entry.setPlayerId(playerId);
                                    entry.setScore(newScore);
                                    entry.setVersion(1L);
                                    leaderboardEntryRepository.save(entry);
                                    updatedCount.incrementAndGet();
                                }
                        );
            } else {
                updatedCount.incrementAndGet();
            }
        }
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getTopNPlayers(String leaderboardId,
                                                         int nPlayers) {
        validateGetTopNPlayersRequest(leaderboardId, nPlayers);
        List<LeaderboardEntry> topNPlayers = leaderboardEntryRepository.findTopNPlayers(
                leaderboardId,
                PageRequest.of(DEFAULT_PAGE_NUMBER, nPlayers)
        );
        return getLeaderboardEntryResponse(topNPlayers);
    }

    private void validateGetTopNPlayersRequest(String leaderboardId, int nPlayers) {
        if (leaderboardId == null || leaderboardId.isEmpty()) {
            throw new RuntimeException("LeaderboardId is invalid.");
        }
        if (nPlayers < 0) {
            throw new RuntimeException("N count is invalid.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getNextNPlayers(String leaderboardId,
                                                          String playerId,
                                                          int nPlayers) {
        validateGetNextOrPrevNPlayersRequest(leaderboardId, playerId, nPlayers);
        LeaderboardEntry playerEntry = getPlayerEntry(leaderboardId, playerId);
        List<LeaderboardEntry> nextNPlayers = leaderboardEntryRepository.findNextNPlayers(
                leaderboardId,
                playerEntry.getScore(),
                playerEntry.getPlayerId(),
                PageRequest.of(DEFAULT_PAGE_NUMBER, nPlayers)
        );
        return getLeaderboardEntryResponse(nextNPlayers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getPrevNPlayers(String leaderboardId,
                                                          String playerId,
                                                          int nPlayers) {
        validateGetNextOrPrevNPlayersRequest(leaderboardId, playerId, nPlayers);
        LeaderboardEntry playerEntry = getPlayerEntry(leaderboardId, playerId);
        List<LeaderboardEntry> prevNPlayers = leaderboardEntryRepository.findPrevNPlayers(
                leaderboardId,
                playerEntry.getScore(),
                playerEntry.getPlayerId(),
                PageRequest.of(DEFAULT_PAGE_NUMBER, nPlayers)
        );
        // Reverse to maintain correct ranking order (highest score first)
        Collections.reverse(prevNPlayers);
        return getLeaderboardEntryResponse(prevNPlayers);
    }

    private void validateGetNextOrPrevNPlayersRequest(String leaderboardId, String playerId, int nPlayers) {
        if (leaderboardId == null || leaderboardId.isEmpty()) {
            throw new RuntimeException("LeaderboardId is invalid.");
        }
        if (playerId == null || playerId.isEmpty()) {
            throw new RuntimeException("PlayerId is invalid.");
        }
        if (nPlayers < 0) {
            throw new RuntimeException("N count is invalid.");
        }
    }

    private LeaderboardEntry getPlayerEntry(String leaderboardId,
                                            String playerId) {
        return leaderboardEntryRepository.findByLeaderboardIdAndPlayerId(leaderboardId, playerId)
                .orElseThrow(() -> new RuntimeException("Player not found in leaderboard."));
    }

}
