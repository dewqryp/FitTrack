CREATE TABLE exercises (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    muscle_group VARCHAR(255) NOT NULL,
    equipment VARCHAR(255),
    created_by_id UUID NOT NULL,
    CONSTRAINT fk_exercises_user,
    FOREIGN KEY (created_by_id)
        REFERENCES users(id)
);