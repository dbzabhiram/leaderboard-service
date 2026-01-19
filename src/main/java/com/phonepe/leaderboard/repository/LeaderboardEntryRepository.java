package com.phonepe.leaderboard.repository;

import com.phonepe.leaderboard.data.model.LeaderboardEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, String> {

    Optional<LeaderboardEntry> findByLeaderboardIdAndPlayerId(String leaderboardId, String playerId);

    List<LeaderboardEntry> findByLeaderboardIdOrderByScoreDesc(String leaderboardId);

    @Modifying
    @Query("""
        update LeaderboardEntry e
        set e.score = :newScore,
            e.version = e.version + 1
        where e.leaderboard.id = :leaderboardId
          and e.playerId = :playerId
          and :newScore > e.score
    """)
    int updateIfHigherScore(
            String leaderboardId,
            String playerId,
            int newScore
    );

    @Query("""
        select e
        from LeaderboardEntry e
        where e.leaderboard.id = :leaderboardId
        order by e.score desc, e.playerId asc
    """)
    List<LeaderboardEntry> findTopNPlayers(
            String leaderboardId,
            Pageable pageable
    );

    @Query("""
        select e
        from LeaderboardEntry e
        where e.leaderboard.id = :leaderboardId
          and (
                e.score < :score OR
                (e.score = :score AND e.playerId > :playerId)
              )
        order by e.score desc, e.playerId asc
    """)
    List<LeaderboardEntry> findNextNPlayers(
            String leaderboardId,
            int score,
            String playerId,
            Pageable pageable
    );

    @Query("""
        select e
        from LeaderboardEntry e
        where e.leaderboard.id = :leaderboardId
          and (
                e.score > :score OR
                (e.score = :score AND e.playerId < :playerId)
              )
        order by e.score asc, e.playerId desc
    """)
    List<LeaderboardEntry> findPrevNPlayers(
            String leaderboardId,
            int score,
            String playerId,
            Pageable pageable
    );

}