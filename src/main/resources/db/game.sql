CREATE TABLE IF NOT EXISTS game (
    id                BIGSERIAL NOT NULL PRIMARY KEY,
    date_time         TIMESTAMP NOT NULL,
    league_id         BIGINT,
    first_team        VARCHAR(100) NOT NULL,
    second_team       VARCHAR(100) NOT NULL,
    result            VARCHAR(50),
    link              VARCHAR(300),
    FOREIGN KEY (league_id) REFERENCES league(id)
);

CREATE TABLE IF NOT EXISTS game_info (
    id                BIGSERIAL NOT NULL PRIMARY KEY,
    game_id           BIGINT,
    first_win         FLOAT NOT NULL,
    tie               FLOAT NOT NULL,
    second_win        FLOAT NOT NULL,
    one_x             FLOAT NOT NULL,
    x_two             FLOAT NOT NULL,
    FOREIGN KEY (game_id) REFERENCES game(id)
);

DELETE FROM game_info WHERE id >= 1909;
DELETE FROM game_rule WHERE game_id >= 1909;
DELETE FROM bet WHERE game_id >= 1909;
DELETE FROM game WHERE id >= 1909;
alter sequence game_id_seq restart with 1909;
alter sequence game_info_id_seq restart with 1909;
alter sequence bet_id_seq restart with 1909;