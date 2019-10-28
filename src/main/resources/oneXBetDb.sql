CREATE TABLE IF NOT EXISTS game (
    id                SERIAL NOT NULL PRIMARY KEY,
    league            VARCHAR(100) NOT NULL,
    league_link       VARCHAR(100) NOT NULL,
    date_time         TIMESTAMP NOT NULL,
    first_team        VARCHAR(50) NOT NULL,
    second_team       VARCHAR(50) NOT NULL,
    first_win         FLOAT,
    tie               FLOAT,
    second_win        FLOAT,
    first_win_or_tie  FLOAT,
    second_win_or_tie FLOAT,
    result            INT,
    recording_time    TIMESTAMP NOT NULL
);