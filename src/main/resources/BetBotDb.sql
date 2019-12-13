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