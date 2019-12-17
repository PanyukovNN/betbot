DROP TABLE IF EXISTS game;
CREATE TABLE IF NOT EXISTS game (
    id                SERIAL NOT NULL PRIMARY KEY,
    league            VARCHAR(200) NOT NULL,
    league_link       VARCHAR(200) NOT NULL,
    date_time         TIMESTAMP NOT NULL,
    first_team        VARCHAR(50) NOT NULL,
    second_team       VARCHAR(50) NOT NULL,
    first_win         FLOAT NOT NULL,
    tie               FLOAT NOT NULL,
    second_win        FLOAT NOT NULL,
    first_win_or_tie  FLOAT NOT NULL,
    second_win_or_tie FLOAT NOT NULL,
    result            INT,
    bet_made          INT,
    rule_number       VARCHAR(100)
);

DROP TABLE IF EXISTS selected_league;
CREATE TABLE IF NOT EXISTS selected_league (
    id          SERIAL NOT NULL PRIMARY KEY,
    league_link VARCHAR(200) NOT NULL
);

DROP TABLE IF EXISTS exclude_league;
CREATE TABLE IF NOT EXISTS exclude_league (
    id          SERIAL NOT NULL PRIMARY KEY,
    league_link VARCHAR(200) NOT NULL,
    rule_number VARCHAR(100) NOT NULL
);

DROP TABLE IF EXISTS bet_info;
CREATE TABLE IF NOT EXISTS bet_info (
    id          SERIAL NOT NULL PRIMARY KEY,
    bet_time    TIMESTAMP NOT NULL
);