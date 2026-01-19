<h1>Leaderboard Service</h1>

A high-performance, concurrent leaderboard system built using Spring Boot and JPA, designed to handle real-time score submissions and ranking queries efficiently.

<h2>Features</h2>

- Create and manage leaderboards per game
- Submit scores with atomic update-if-higher semantics
- Supports high concurrency safely using database-level updates
- Fetch:
  - Top N players
  - Next N players relative to a user
  - Previous N players relative to a user
- Handles active vs inactive leaderboards
- Built with H2 for local development/testing
- Fully unit-tested with concurrency and pagination scenarios

<h2>Tech Stack</h2>
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- H2 Database
- Lombok
- JUnit 5, Mockito


<h2>Project Structure</h2>
```
leaderboard-service/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
└── test/
```

<h2>Setup & Run</h2>

<h3>Clone the repo</h3>
```
git clone https://github.com/dbzabhiram/leaderboard-service.git
cd leaderboard-service
```


<h3>Run the app</h3>
```
./gradlew bootRun
```


<h3>Access H2 Console</h3>
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (blank)
```

<h2>API Endpoints</h2>

- Create Leaderboard
```
POST /api/leaderboard
```

- Get Leaderboard by ID
```
POST /api/leaderboard/{id}
```

- Submit Score
```
POST /api/leaderboard/game/{gameId}/player/{playerId}/submit
```

- Get Top N Players
```
GET /api/leaderboard/{id}/top?nPlayers={}
```

- Get Next N Players relative to given user
```
GET /api/leaderboard/{id}/player/{playerId}/next?nPlayers={}
```

- Get Previous N Players relative to given user
```
GET /api/leaderboard/{id}/player/{playerId}/prev?nPlayers={}
```