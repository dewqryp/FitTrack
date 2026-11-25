CREATE TABLE workout_plans (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workout_plans_user
    FOREIGN KEY (owner_id)
        REFERENCES users(id)
);