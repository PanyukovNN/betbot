CREATE TABLE IF NOT EXISTS bank (
    id          SERIAL NOT NULL PRIMARY KEY,
    amount      INT NOT NULL,
    date_time   DATE NOT NULL
);
