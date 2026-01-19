package com.phonepe.leaderboard.service;

import com.phonepe.leaderboard.data.model.Leaderboard;
import com.phonepe.leaderboard.data.model.LeaderboardEntry;
import com.phonepe.leaderboard.data.response.CreateLeaderboardResponse;
import com.phonepe.leaderboard.data.response.GetLeaderboardResponse;
import com.phonepe.leaderboard.data.response.LeaderboardEntryResponse;
import com.phonepe.leaderboard.data.response.ScoreSubmissionResponse;
import com.phonepe.leaderboard.repository.LeaderboardEntryRepository;
import com.phonepe.leaderboard.repository.LeaderboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private LeaderboardEntryRepository leaderboardEntryRepository;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    @Test
    void testCreateLeaderboard_nullGameId() {
        try {
            leaderboardService.createLeaderboard(null, 123, 456);
        } catch (RuntimeException rte) {
            assertEquals("GameId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testCreateLeaderboard_emptyGameId() {
        try {
            leaderboardService.createLeaderboard("", 123, 456);
        } catch (RuntimeException rte) {
            assertEquals("GameId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testCreateLeaderboard_invalidEqualStartTime() {
        try {
            leaderboardService.createLeaderboard("g1", 123, 123);
        } catch (RuntimeException rte) {
            assertEquals("Leaderboard startTime is greater than or equal to endTime, which is not possible.", rte.getMessage());
        }
    }

    @Test
    void testCreateLeaderboard_invalidGreaterStartTime() {
        try {
            leaderboardService.createLeaderboard("g1", 456, 123);
        } catch (RuntimeException rte) {
            assertEquals("Leaderboard startTime is greater than or equal to endTime, which is not possible.", rte.getMessage());
        }
    }

    @Test
    void testCreateLeaderboard_validRequest() {
        Leaderboard saved = new Leaderboard("id1", "g1", 123, 123);
        when(leaderboardRepository.save(any())).thenReturn(saved);

        CreateLeaderboardResponse response = leaderboardService.createLeaderboard("g1", 123, 456);

        assertNotNull(response.getLeaderboardId());
        assertEquals(saved.getId(), response.getLeaderboardId());
    }

    @Test
    void testGetLeaderboard_nullLeaderboardId() {
        try {
            leaderboardService.getLeaderboard(null);
        } catch (RuntimeException rte) {
            assertEquals("LeaderboardId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetLeaderboard_emptyLeaderboardId() {
        try {
            leaderboardService.getLeaderboard("");
        } catch (RuntimeException rte) {
            assertEquals("LeaderboardId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetLeaderboard_noLeaderboardPresent() {
        when(leaderboardRepository.findById(any())).thenReturn(Optional.empty());

        try {
            leaderboardService.getLeaderboard("lb1");
        } catch (RuntimeException rte) {
            assertEquals("No leaderboard found for given id.", rte.getMessage());
        }
    }

    @Test
    void testGetLeaderboard_leaderboardPresent() {
        Leaderboard leaderboard = new Leaderboard("id1", "g1", 123, 123);
        List<LeaderboardEntry> leaderboardEntries = List.of(
                new LeaderboardEntry("lbe1", leaderboard, "p1", 100, 0L),
                new LeaderboardEntry("lbe2", leaderboard, "p2", 90, 0L),
                new LeaderboardEntry("lbe3", leaderboard,"p3", 80, 0L)
        );

        when(leaderboardRepository.findById(any())).thenReturn(Optional.of(leaderboard));
        when(leaderboardEntryRepository.findByLeaderboardIdOrderByScoreDesc(any())).thenReturn(leaderboardEntries);

        GetLeaderboardResponse response = leaderboardService.getLeaderboard("lb1");

        assertNotNull(response);
        assertNotNull(response.getScores());
        assertEquals(3, response.getScores().size());
        assertEquals("p1", response.getScores().get(0).getPlayerId());
        assertEquals(100, response.getScores().get(0).getScore());
        assertEquals("p2", response.getScores().get(1).getPlayerId());
        assertEquals(90, response.getScores().get(1).getScore());
        assertEquals("p3", response.getScores().get(2).getPlayerId());
        assertEquals(80, response.getScores().get(2).getScore());
    }

    @Test
    void testSubmitScore_nullGameId() {
        String gameId = null;
        String playerId = "p1";
        int score = 100;

        try {
            leaderboardService.submitScore(gameId, playerId, score);
        } catch (RuntimeException rte) {
            assertEquals("GameId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testSubmitScore_emptyGameId() {
        String gameId = "";
        String playerId = "p1";
        int score = 100;

        try {
            leaderboardService.submitScore(gameId, playerId, score);
        } catch (RuntimeException rte) {
            assertEquals("GameId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testSubmitScore_nullPlayerId() {
        String gameId = "g1";
        String playerId = null;
        int score = 100;

        try {
            leaderboardService.submitScore(gameId, playerId, score);
        } catch (RuntimeException rte) {
            assertEquals("PlayerId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testSubmitScore_emptyPlayerId() {
        String gameId = "g1";
        String playerId = "";
        int score = 100;

        try {
            leaderboardService.submitScore(gameId, playerId, score);
        } catch (RuntimeException rte) {
            assertEquals("PlayerId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testSubmitScore_lessThanLimitScore() {
        String gameId = "g1";
        String playerId = "p1";
        int score = -1;

        try {
            leaderboardService.submitScore(gameId, playerId, score);
        } catch (RuntimeException rte) {
            assertEquals("Game score is invalid.", rte.getMessage());
        }
    }

    @Test
    void testSubmitScore_greaterThanLimitScore() {
        String gameId = "g1";
        String playerId = "p1";
        int score = 1_000_000_001;

        try {
            leaderboardService.submitScore(gameId, playerId, score);
        } catch (RuntimeException rte) {
            assertEquals("Game score is invalid.", rte.getMessage());
        }
    }

    @Test
    void testSubmitScore_noLeaderboardFound() {
        String gameId = "g1";
        String playerId = "p1";
        int score = 100;

        when(leaderboardRepository.findByGameId(gameId))
                .thenReturn(List.of());

        try {
            leaderboardService.submitScore(gameId, playerId, score);
        } catch (RuntimeException rte) {
            assertEquals("No leaderboard found for given gameId.", rte.getMessage());
        }
    }

    @Test
    void testSubmitScore_concurrentScoreUpdates_shouldKeepHighestScore() throws Exception {
        String leaderboardId = "lb1";
        String gameId = "g1";
        long todayTime = System.currentTimeMillis();
        long tomorrowTime = todayTime + 24 * 60 * 60 * 1000;
        String playerId = "p1";

        Leaderboard leaderboard = new Leaderboard(leaderboardId, gameId, todayTime, tomorrowTime);

        when(leaderboardRepository.findByGameId(gameId))
                .thenReturn(List.of(leaderboard));

        when(leaderboardEntryRepository.updateIfHigherScore(any(), any(), anyInt()))
                .thenReturn(1);

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int score = i * 10;
            executor.submit(() -> {
                try {
                    leaderboardService.submitScore(gameId, playerId, score);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        ArgumentCaptor<Integer> scoreCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(leaderboardEntryRepository, atLeastOnce())
                .updateIfHigherScore(eq(leaderboardId), eq(playerId), scoreCaptor.capture());

        int maxScorePassed = scoreCaptor.getAllValues()
                .stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElseThrow();

        assertEquals(490, maxScorePassed);
    }

    @Test
    void testSubmitScore_shouldIgnoreInactiveLeaderboards() {
        String leaderboardId = "lb1";
        String gameId = "g1";
        long now = System.currentTimeMillis();
        long yesterday = now - 24 * 60 * 60 * 1000; // already ended
        String playerId = "p1";
        int score = 100;

        // Inactive leaderboard (endTime in the past)
        Leaderboard inactiveLeaderboard =
                new Leaderboard(leaderboardId, gameId, now - 2_000, yesterday);

        when(leaderboardRepository.findByGameId(gameId))
                .thenReturn(List.of(inactiveLeaderboard));

        ScoreSubmissionResponse response =
                leaderboardService.submitScore(gameId, playerId, score);

        assertEquals(0, response.getUpdatedLeaderboardCount());
        assertFalse(response.isDataUpdated());

        verifyNoInteractions(leaderboardEntryRepository);
    }

    @Test
    void testSubmitScore_shouldUpdateOnlyActiveLeaderboards() {
        String gameId = "g1";
        String playerId = "p1";
        int score = 200;
        long now = System.currentTimeMillis();

        Leaderboard activeLeaderboard =
                new Leaderboard("lb-active", gameId, now - 1_000, now + 60_000);

        Leaderboard inactiveLeaderboard =
                new Leaderboard("lb-inactive", gameId, now - 60_000, now - 1_000);

        when(leaderboardRepository.findByGameId(gameId))
                .thenReturn(List.of(activeLeaderboard, inactiveLeaderboard));

        when(leaderboardEntryRepository.updateIfHigherScore(eq("lb-active"), eq(playerId), eq(score)))
                .thenReturn(1);

        ScoreSubmissionResponse response =
                leaderboardService.submitScore(gameId, playerId, score);

        assertEquals(1, response.getUpdatedLeaderboardCount());
        assertTrue(response.isDataUpdated());

        verify(leaderboardEntryRepository, times(1))
                .updateIfHigherScore("lb-active", playerId, score);

        verify(leaderboardEntryRepository, never())
                .updateIfHigherScore(eq("lb-inactive"), any(), anyInt());
    }

    @Test
    void testSubmitScore_shouldIgnoreLowerScoreThanExisting() {
        String leaderboardId = "lb1";
        String gameId = "g1";
        String playerId = "p1";
        int existingScore = 300;
        int lowerScore = 200;
        long now = System.currentTimeMillis();

        Leaderboard leaderboard =
                new Leaderboard(leaderboardId, gameId, now - 1_000, now + 60_000);

        when(leaderboardRepository.findByGameId(gameId))
                .thenReturn(List.of(leaderboard));

        // update returns 0 → means newScore <= existingScore
        when(leaderboardEntryRepository.updateIfHigherScore(leaderboardId, playerId, lowerScore))
                .thenReturn(0);

        when(leaderboardEntryRepository.findByLeaderboardIdAndPlayerId(leaderboardId, playerId))
                .thenReturn(Optional.of(new LeaderboardEntry("lbe1", leaderboard, playerId, existingScore, 0L)));

        ScoreSubmissionResponse response =
                leaderboardService.submitScore(gameId, playerId, lowerScore);

        assertEquals(0, response.getUpdatedLeaderboardCount());
        assertFalse(response.isDataUpdated());

        verify(leaderboardEntryRepository, times(1))
                .updateIfHigherScore(leaderboardId, playerId, lowerScore);

        verify(leaderboardEntryRepository, never()).save(any());
    }

    @Test
    void testSubmitScore_insertsNewEntryWhenNoneExists() {
        String leaderboardId = "lb1";
        String gameId = "g1";
        long now = System.currentTimeMillis();
        String playerId = "p1";
        int newScore = 100;

        Leaderboard activeLeaderboard = new Leaderboard(leaderboardId, gameId, now, now + 60_000);

        when(leaderboardRepository.findByGameId(gameId))
                .thenReturn(List.of(activeLeaderboard));

        when(leaderboardEntryRepository.updateIfHigherScore(eq(leaderboardId), eq(playerId), eq(newScore)))
                .thenReturn(0);

        when(leaderboardEntryRepository.findByLeaderboardIdAndPlayerId(eq(leaderboardId), eq(playerId)))
                .thenReturn(Optional.empty());

        ArgumentCaptor<LeaderboardEntry> captor = ArgumentCaptor.forClass(LeaderboardEntry.class);
        when(leaderboardEntryRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScoreSubmissionResponse response =
                leaderboardService.submitScore(gameId, playerId, newScore);

        verify(leaderboardEntryRepository, times(1)).save(any());

        LeaderboardEntry inserted = captor.getValue();
        assertEquals(playerId, inserted.getPlayerId());
        assertEquals(newScore, inserted.getScore());
        assertEquals(activeLeaderboard, inserted.getLeaderboard());
        assertEquals(1, inserted.getVersion()); // version starts at 1

        assertEquals(1, response.getUpdatedLeaderboardCount());
        assertTrue(response.isDataUpdated());
    }

    @Test
    void testGetTopNPlayers_nullLeaderboardId() {
        String leaderboardId = null;
        int nPlayers = 1;

        try {
            leaderboardService.getTopNPlayers(leaderboardId, nPlayers);
        } catch (RuntimeException rte) {
            assertEquals("LeaderboardId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetTopNPlayers_emptyLeaderboardId() {
        String leaderboardId = "";
        int nPlayers = 1;

        try {
            leaderboardService.getTopNPlayers(leaderboardId, nPlayers);
        } catch (RuntimeException rte) {
            assertEquals("LeaderboardId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetTopNPlayers_negativeNPlayers() {
        String leaderboardId = "lb1";
        int nPlayers = -1;

        try {
            leaderboardService.getTopNPlayers(leaderboardId, nPlayers);
        } catch (RuntimeException rte) {
            assertEquals("N count is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetTopNPlayers_shouldReturnTopNPlayers() {
        String leaderboardId = "lb1";
        int nPlayers = 3;
        String gameId = "g1";
        long now = System.currentTimeMillis();

        Leaderboard leaderboard =
                new Leaderboard(leaderboardId, gameId, now, now + 60_000);

        List<LeaderboardEntry> topEntries = List.of(
                new LeaderboardEntry("lbe1", leaderboard, "p1", 100, 0L),
                new LeaderboardEntry("lbe2", leaderboard, "p2", 90, 0L),
                new LeaderboardEntry("lbe3", leaderboard,"p3", 80, 0L)
        );

        when(leaderboardEntryRepository.findTopNPlayers(
                eq(leaderboardId),
                any(Pageable.class)
        )).thenReturn(topEntries);

        List<LeaderboardEntryResponse> result =
                leaderboardService.getTopNPlayers(leaderboardId, nPlayers);

        assertEquals(3, result.size());
        assertEquals("p1", result.get(0).getPlayerId());
        assertEquals(100, result.get(0).getScore());
        assertEquals("p3", result.get(2).getPlayerId());

        verify(leaderboardEntryRepository)
                .findTopNPlayers(eq(leaderboardId), any(Pageable.class));
    }

    @Test
    void testGetNextNPlayers_nullLeaderboardId() {
        String leaderboardId = null;
        String playerId = "p1";
        int nPlayers = 1;

        try {
            leaderboardService.getNextNPlayers(leaderboardId, playerId, nPlayers);
        } catch (RuntimeException rte) {
            assertEquals("LeaderboardId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetNextNPlayers_emptyLeaderboardId() {
        String leaderboardId = "";
        String playerId = "p1";
        int nPlayers = 1;

        try {
            leaderboardService.getNextNPlayers(leaderboardId, playerId, nPlayers);
        } catch (RuntimeException rte) {
            assertEquals("LeaderboardId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetNextNPlayers_shouldReturnNextNPlayers() {
        String leaderboardId = "lb1";
        String playerId = "p3";
        int nPlayers = 2;
        String gameId = "g1";
        long now = System.currentTimeMillis();

        Leaderboard leaderboard =
                new Leaderboard(leaderboardId, gameId, now, now + 60_000);

        LeaderboardEntry playerEntry = new LeaderboardEntry("lbe3", leaderboard, "p3", 80, 0L);

        List<LeaderboardEntry> nextEntries = List.of(
                new LeaderboardEntry("lbe4", leaderboard, "p4", 70, 0L),
                new LeaderboardEntry("lbe5", leaderboard, "p5", 60, 0L)
        );

        when(leaderboardEntryRepository.findByLeaderboardIdAndPlayerId(
                leaderboardId, playerId
        )).thenReturn(Optional.of(playerEntry));

        when(leaderboardEntryRepository.findNextNPlayers(
                eq(leaderboardId),
                eq(80),
                eq("p3"),
                any(Pageable.class)
        )).thenReturn(nextEntries);

        List<LeaderboardEntryResponse> result =
                leaderboardService.getNextNPlayers(leaderboardId, playerId, nPlayers);

        assertEquals(2, result.size());
        assertEquals("p4", result.get(0).getPlayerId());
        assertEquals(70, result.get(0).getScore());

        verify(leaderboardEntryRepository)
                .findByLeaderboardIdAndPlayerId(leaderboardId, playerId);
        verify(leaderboardEntryRepository)
                .findNextNPlayers(eq(leaderboardId), eq(80), eq("p3"), any(Pageable.class));
    }

    @Test
    void testGetPrevNPlayers_nullPlayerId() {
        String leaderboardId = "lb1";
        String playerId = null;
        int nPlayers = 1;

        try {
            leaderboardService.getNextNPlayers(leaderboardId, playerId, nPlayers);
        } catch (RuntimeException rte) {
            assertEquals("PlayerId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetPrevNPlayers_emptyPlayerId() {
        String leaderboardId = "lb1";
        String playerId = "";
        int nPlayers = 1;

        try {
            leaderboardService.getNextNPlayers(leaderboardId, playerId, nPlayers);
        } catch (RuntimeException rte) {
            assertEquals("PlayerId is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetPrevNPlayers_negativeNPlayers() {
        String leaderboardId = "lb1";
        String playerId = "p1";
        int nPlayers = -11;

        try {
            leaderboardService.getNextNPlayers(leaderboardId, playerId, nPlayers);
        } catch (RuntimeException rte) {
            assertEquals("N count is invalid.", rte.getMessage());
        }
    }

    @Test
    void testGetPrevNPlayers_shouldReturnPrevNPlayersInCorrectOrder() {
        String leaderboardId = "lb1";
        String playerId = "p4";
        int nPlayers = 3;
        String gameId = "g1";
        long now = System.currentTimeMillis();

        Leaderboard leaderboard =
                new Leaderboard(leaderboardId, gameId, now, now + 60_000);

        LeaderboardEntry playerEntry = new LeaderboardEntry("lbe4", leaderboard, "p4", 70, 0L);

        // Repo returns ASC order by score (as per your query)
        List<LeaderboardEntry> prevEntriesFromRepo = new ArrayList<>(List.of(
                new LeaderboardEntry("lbe3", leaderboard, "p3", 80, 0L),
                new LeaderboardEntry("lbe2", leaderboard, "p2", 90, 0L),
                new LeaderboardEntry("lbe1", leaderboard, "p1", 100, 0L)
        ));

        when(leaderboardEntryRepository.findByLeaderboardIdAndPlayerId(
                leaderboardId, playerId
        )).thenReturn(Optional.of(playerEntry));

        when(leaderboardEntryRepository.findPrevNPlayers(
                eq(leaderboardId),
                eq(70),
                eq("p4"),
                any(Pageable.class)
        )).thenReturn(prevEntriesFromRepo);

        List<LeaderboardEntryResponse> result =
                leaderboardService.getPrevNPlayers(leaderboardId, playerId, nPlayers);

        // After reverse(), highest score should be first
        assertEquals(3, result.size());
        assertEquals("p1", result.get(0).getPlayerId());
        assertEquals(100, result.get(0).getScore());
        assertEquals("p3", result.get(2).getPlayerId());

        verify(leaderboardEntryRepository)
                .findByLeaderboardIdAndPlayerId(leaderboardId, playerId);
        verify(leaderboardEntryRepository)
                .findPrevNPlayers(eq(leaderboardId), eq(70), eq("p4"), any(Pageable.class));
    }


    @Test
    void testGetNextNPlayers_shouldThrowIfPlayerNotFound() {
        when(leaderboardEntryRepository.findByLeaderboardIdAndPlayerId("lb1", "p404"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> leaderboardService.getNextNPlayers("lb1", "p404", 5));
    }


}
