CREATE TABLE workout_plan_exercises (
     id UUID PRIMARY KEY,
     workout_plan_id UUID NOT NULL,
     exercise_id UUID NOT NULL,
     order_index INTEGER NOT NULL,
     sets INTEGER NOT NULL,
     reps INTEGER NOT NULL,
     rest_seconds INTEGER NOT NULL,
     CONSTRAINT fk_workout_plan_exercises_plan
         FOREIGN KEY (workout_plan_id)
             REFERENCES workout_plans(id),
     CONSTRAINT fk_workout_plan_exercises_exercise
         FOREIGN KEY (exercise_id)
             REFERENCES exercises(id),
     CONSTRAINT chk_workout_plan_exercises_sets
         CHECK (sets > 0),
     CONSTRAINT chk_workout_plan_exercises_reps
         CHECK (reps > 0),
     CONSTRAINT chk_workout_plan_exercises_rest
         CHECK (rest_seconds >= 0),
     CONSTRAINT chk_workout_plan_exercises_order
         CHECK (order_index >= 0),
     CONSTRAINT uq_workout_plan_exercises_order
         UNIQUE (workout_plan_id, order_index)
);