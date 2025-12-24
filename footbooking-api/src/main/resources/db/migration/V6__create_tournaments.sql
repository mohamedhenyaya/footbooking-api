CREATE TABLE tournaments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    type VARCHAR(255),
    terrain VARCHAR(255),
    cost DOUBLE PRECISION,
    prize DOUBLE PRECISION,
    start_date DATE,
    end_date DATE,
    max_teams INTEGER,
    remaining_teams INTEGER
);
