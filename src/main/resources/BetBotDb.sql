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
    result_id         INT NOT NULL,
    bet_made          INT,
    rule_number       VARCHAR(100),
    FOREIGN KEY (result_id) REFERENCES result (id)
);

DROP TABLE IF EXISTS game_temp;
CREATE TABLE IF NOT EXISTS game_temp (
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
    result_id         INT NOT NULL,
    bet_made          INT,
    rule_id           INT NOT NULL,
    FOREIGN KEY (result_id) REFERENCES result (id),
    FOREIGN KEY (rule_id) REFERENCES rule (id)
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

DROP TABLE IF EXISTS bank;
CREATE TABLE IF NOT EXISTS bank (
    id          SERIAL NOT NULL PRIMARY KEY,
    amount      INT NOT NULL,
    bank_date   DATE NOT NULL
);

DROP TABLE IF EXISTS game_link;
CREATE TABLE IF NOT EXISTS game_link (
    game_id     INT NOT NULL,
    link        VARCHAR(300) NOT NULL,
    FOREIGN KEY (game_id) REFERENCES game (id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS result;
CREATE TABLE IF NOT EXISTS result (
    id          SERIAL NOT NULL PRIMARY KEY,
    value       VARCHAR(50) NOT NULL
);
INSERT INTO result (id, value)
VALUES (DEFAULT, 'FIRST_WIN'),
       (DEFAULT, 'TIE'),
       (DEFAULT, 'SECOND_WIN'),
       (DEFAULT, 'NO_RESULT'),
       (DEFAULT, 'NOT_PLAYED');

DROP TABLE IF EXISTS rule;
CREATE TABLE IF NOT EXISTS rule (
    id              SERIAL NOT NULL PRIMARY KEY,
    rule_number     VARCHAR(100) NOT NULL
);
INSERT INTO rule (id, rule_number)
VALUES (DEFAULT, 'RULE_ONE'),
       (DEFAULT, 'RULE_TEST');

DROP TABLE IF EXISTS game_rule;
CREATE TABLE IF NOT EXISTS game_rule (
    game_id     INT NOT NULL,
    rule_id     INT NOT NULL,
    FOREIGN KEY (game_id) REFERENCES game (id) ON DELETE CASCADE,
    FOREIGN KEY (rule_id) REFERENCES rule (id) ON DELETE CASCADE
);