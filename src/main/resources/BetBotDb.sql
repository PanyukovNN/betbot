-- DROP TABLE IF EXISTS league CASCADE ;
CREATE TABLE IF NOT EXISTS league (
    id      BIGSERIAL NOT NULL PRIMARY KEY,
    name    VARCHAR(200) NOT NULL,
    link    VARCHAR(200) NOT NULL
);
-- DROP TABLE IF EXISTS game_temp CASCADE ;
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

-- DROP TABLE IF EXISTS game_info CASCADE ;
CREATE TABLE IF NOT EXISTS game_info (
    id                BIGSERIAL NOT NULL PRIMARY KEY,
    game_id           BIGINT,
    first_win         FLOAT NOT NULL,
    tie               FLOAT NOT NULL,
    second_win        FLOAT NOT NULL,
    first_win_or_tie  FLOAT NOT NULL,
    second_win_or_tie FLOAT NOT NULL,
    FOREIGN KEY (game_id) REFERENCES game(id)
);

-- ALTER TABLE game_info
--     RENAME first_win_or_tie TO one_x;
-- ALTER TABLE game_info
--     RENAME second_win_or_tie TO x_two;

CREATE TABLE IF NOT EXISTS selected_league (
    id          SERIAL NOT NULL PRIMARY KEY,
    league_link VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS exclude_league (
    id          SERIAL NOT NULL PRIMARY KEY,
    league_link VARCHAR(200) NOT NULL,
    rule_number VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS bet_info (
    id          SERIAL NOT NULL PRIMARY KEY,
    bet_time    TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS bank (
    id          SERIAL NOT NULL PRIMARY KEY,
    amount      INT NOT NULL,
    bank_date   DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS game_rule (
    game_id BIGINT NOT NULL,
    rule_id BIGINT NOT NULL,
    PRIMARY KEY (game_id, rule_id),
    FOREIGN KEY (game_id) REFERENCES game(id),
    FOREIGN KEY (rule_id) REFERENCES rule(id)
);

-- DROP TABLE IF EXISTS game_rule_bet;
CREATE TABLE IF NOT EXISTS game_rule_bet (
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    game_id     BIGINT NOT NULL,
    rule_id     BIGINT NOT NULL,
    bet_made    BOOLEAN NOT NULL,
    FOREIGN KEY (game_id) REFERENCES game(id),
    FOREIGN KEY (rule_id) REFERENCES rule(id)
);

-- DELETE FROM game_info WHERE id > ;
-- DELETE FROM game_rule WHERE game_id >  ;
-- DELETE FROM game WHERE id >  ;
-- alter sequence game_id_seq restart with 730;
-- alter sequence game_info_id_seq restart with 730;