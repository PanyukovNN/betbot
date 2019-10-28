CREATE TABLE IF NOT EXISTS game (
    id             SERIAL NOT NULL PRIMARY KEY,
    country        VARCHAR(50) NOT NULL,
    leagueName     VARCHAR(50) NOT NULL,
    season         VARCHAR(50) NOT NULL,
    gameDate       TIMESTAMP NOT NULL,
    firstCommand   VARCHAR(50) NOT NULL,
    secondCommand  VARCHAR(50) NOT NULL,
    firstBalls     INT NOT NULL,
    secondBalls    INT NOT NULL,
    coefHref       VARCHAR(50),
    coefficient_id INT UNIQUE NOT NULL
);
