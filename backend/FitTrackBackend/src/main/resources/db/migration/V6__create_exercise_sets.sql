CREATE TABLE exercise_sets
(
    id UUID PRIMARY KEY,
    workout_session_id UUID NOT NULL,
    exercise_id UUID NOT NULL,
    set_number INTEGER NOT NULL,
    reps INTEGER NOT NULL,
    weight NUMERIC(7, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_exercise_sets_workout_session
        FOREIGN KEY (workout_session_id)
            REFERENCES workout_sessions(id),

    CONSTRAINT fk_exercise_sets_exercise
        FOREIGN KEY (exercise_id)
            REFERENCES exercises(id),

    CONSTRAINT chk_exercise_sets_set_number
        CHECK (set_number > 0),

    CONSTRAINT chk_exercise_sets_reps
        CHECK (reps > 0)
);

CREATE INDEX idx_exercise_sets_workout_session_id
    ON exercise_sets(workout_session_id);