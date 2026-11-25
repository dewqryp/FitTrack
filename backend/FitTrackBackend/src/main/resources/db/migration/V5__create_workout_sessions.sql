CREATE TABLE workout_sessions (
                                  id UUID PRIMARY KEY,
                                  workout_plan_id UUID,
                                  user_id UUID NOT NULL,
                                  started_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                  finished_at TIMESTAMP WITH TIME ZONE,
                                  notes TEXT,
                                  FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id),
                                  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_workout_sessions_user_id
    ON workout_sessions(user_id);