package com.phonepe.leaderboard.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "leaderboard_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"leaderboard_id", "playerId"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leaderboard_id", nullable = false)
    private Leaderboard leaderboard;

    @Column(nullable = false)
    private String playerId;

    @Column(nullable = false)
    private int score;

    @Version
    private Long version;

}
