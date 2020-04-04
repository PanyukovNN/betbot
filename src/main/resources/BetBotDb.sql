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
    bet_made          BOOLEAN,
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

CREATE TABLE IF NOT EXISTS rule (
    id          SERIAL NOT NULL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL
);
INSERT INTO rule (id, name)
VALUES (default, 'RULE_ONE'),
       (default, 'X_TWO_RULE');

CREATE TABLE IF NOT EXISTS game_rule (
    game_id BIGINT NOT NULL,
    rule_id BIGINT NOT NULL,
    PRIMARY KEY (game_id, rule_id),
    FOREIGN KEY (game_id) REFERENCES game(id),
    FOREIGN KEY (rule_id) REFERENCES rule(id)
);

-- DELETE FROM game_info WHERE id > 704 ;
-- DELETE FROM game_rule WHERE game_id > 689 ;
-- DELETE FROM game WHERE id > 689 ;
-- alter sequence game_id_seq restart with 690;
-- alter sequence game_info_id_seq restart with 705;

-- DROP TABLE result;
-- CREATE TABLE IF NOT EXISTS result (
--     id          SERIAL NOT NULL PRIMARY KEY,
--     value       VARCHAR(50) NOT NULL
-- );
-- INSERT INTO result (id, value)
-- VALUES (DEFAULT, 'FIRST_WIN'),
--        (DEFAULT, 'TIE'),
--        (DEFAULT, 'SECOND_WIN'),
--        (DEFAULT, 'NO_RESULT'),
--        (DEFAULT, 'NOT_PLAYED');
