CREATE TABLE IF NOT EXISTS bank (
    id          SERIAL NOT NULL PRIMARY KEY,
    amount      INT NOT NULL,
    bank_date   DATE NOT NULL
);