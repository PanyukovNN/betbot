CREATE TABLE IF NOT EXISTS game_rule (
    game_id BIGINT NOT NULL,
    rule_id BIGINT NOT NULL,
    PRIMARY KEY (game_id, rule_id),
    FOREIGN KEY (game_id) REFERENCES game(id),
    FOREIGN KEY (rule_id) REFERENCES rule(id)
);

CREATE TABLE IF NOT EXISTS rule (
    id               SERIAL NOT NULL PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    selected_leagues BOOLEAN NOT NULL default false,
    percent          FLOAT NOT NULL DEFAULT 0,
    bet_coefficient  VARCHAR(50) NOT NULL default '',
    activate         BOOLEAN
);

CREATE TABLE IF NOT EXISTS rule_condition (
    id          SERIAL NOT NULL PRIMARY KEY,
    rule_id     INT,
    coefficient VARCHAR(50),
    operator    VARCHAR(10),
    value       FLOAT,
    enabled     boolean,
    FOREIGN KEY (rule_id) REFERENCES rule(id)
);
