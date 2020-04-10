CREATE TABLE IF NOT EXISTS bet_info (
    id          SERIAL NOT NULL PRIMARY KEY,
    bet_time    TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS bet (
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    date_time   TIMESTAMP NOT NULL,
    game_id     BIGINT NOT NULL,
    rule_id     BIGINT NOT NULL,
    status      VARCHAR(20),
    amount      INT NOT NULL,
    coefficient VARCHAR(50),
    FOREIGN KEY (game_id) REFERENCES game(id),
    FOREIGN KEY (rule_id) REFERENCES rule(id)
);

-- ALTER TABLE bet
--     ADD amount INT NOT NULL default 0;
-- ALTER TABLE bet
--     ADD coefficient VARCHAR(50) default 'X_TWO';

-- итог (WIN, LOSE) ??
-- размер коэффициента ??
