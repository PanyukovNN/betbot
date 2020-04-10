CREATE TABLE IF NOT EXISTS league (
    id      BIGSERIAL NOT NULL PRIMARY KEY,
    name    VARCHAR(200) NOT NULL,
    link    VARCHAR(200) NOT NULL
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