CREATE TABLE body_measurements
(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    weight NUMERIC(6, 2) NOT NULL,
    body_fat_percentage INTEGER,
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    notes TEXT,

    CONSTRAINT fk_body_measurements_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT chk_body_measurements_weight
        CHECK (weight > 0),

    CONSTRAINT chk_body_measurements_body_fat_percentage
        CHECK (
            body_fat_percentage IS NULL
                OR body_fat_percentage BETWEEN 0 AND 100
            )
);

CREATE INDEX idx_body_measurements_user_id
    ON body_measurements(user_id);