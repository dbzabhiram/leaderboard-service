package com.phonepe.leaderboard.repository;

import com.phonepe.leaderboard.data.model.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, String> {

    List<Leaderboard> findByGameId(String gameId);

}
