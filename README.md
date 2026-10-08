# Connect Four

A multiplayer, real-time Connect Four web app built for the *Verteilte Systeme* (Distributed Systems) course at Hochschule Osnabrück.

Players register, log in, host or join a game from a lobby, and play against each other live over WebSockets. Admins can ban and unban users.

## Features

- **Real-time gameplay** over Jakarta WebSocket (`/ws/game`), 7×6 board, win detection for horizontal, vertical and diagonal lines
- **Lobby** with hosting and joining of game rooms
- **Authentication** with salted SHA-256 password hashes and JWT session cookies
- **Admin panel** (`/admin/users`) to ban and unban users
- **Persistence** of users in an embedded H2 database

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 16+ (built and tested with JDK 17) |
| Web | Jakarta Servlet 5 / JSP, Jakarta WebSocket 2.2 |
| Auth | [java-jwt](https://github.com/auth0/java-jwt) 4.2.1 |
| Database | H2 2.2.224 (file mode) |
| Frontend | Bootstrap, vanilla JavaScript |
| Build | Maven (wrapper included), WAR packaging |

## Project structure

```
src/main/java/de/hsos/connectfour/
├── auth/        AuthService, JwtService, PasswordUtil, AuthHelper
├── db/          DbUtil (JDBC connection), UserRepository
├── game/        ConnectFourBoard, GameRoom, GameManager, GameStatus
├── model/       User
├── web/         Servlets: /auth, /lobby, /host-game, /join, /admin/users
└── websocket/   GameEndpoint, GameCommandHandler, HttpSessionConfigurator
src/main/webapp/
├── WEB-INF/     JSP views, web.xml
├── css/ js/     Frontend assets (game.js, game-ui.js, ws-client.js, auth.js)
└── images/
```

## Getting started

### Prerequisites

- JDK 17
- A Jakarta EE 9+ servlet container, e.g. Apache Tomcat 10.1 or later (WebSocket support required)

### Build

```bash
./mvnw clean package        # Windows: mvnw.cmd clean package
```

This produces `target/Connect-Four-1.0-SNAPSHOT.war`.

### Run

Deploy the WAR to Tomcat 10+ (copy it into `webapps/`, or deploy from your IDE) and open the context path, e.g. `http://localhost:8080/Connect-Four-1.0-SNAPSHOT/`.

The H2 database file (`connect4db.mv.db` in your home directory by default) is created on first start.

### Default admin account

On first start an admin user is created with username `admin`. Its password is taken from the `ADMIN_PASSWORD` environment variable and defaults to `admin`. **Set it before running anywhere other than your own machine.**

## How to play

1. Register an account (or log in) at `/auth`.
2. In the lobby, host a new game or join an open one.
3. Take turns dropping discs into a column. The first player to connect four in a row wins.

## Configuration notes

| Environment variable | Purpose | Default |
|---|---|---|
| `CONNECT4_DB_PATH` | H2 database file path (without extension) | `~/connect4db` |
| `JWT_SECRET` | JWT signing secret | random per start (logins reset on restart) |
| `ADMIN_PASSWORD` | Initial admin password | `admin` |

## Course context

Developed as a semester project for *Verteilte Systeme*, Hochschule Osnabrück, Semester 4.

## License

No license specified. All rights reserved by the author unless a license is added.
