package com.phonepe.leaderboard.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leaderboard")
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // leaderboardId

    private String gameId; // gameId to which the leaderboard is linked

    private long startTime; // leaderboard activation time

    private long endTime; // leaderboard de-activation time

}
