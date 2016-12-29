# --- !Ups

CREATE TABLE answer_user
(
  answer_id UUID PRIMARY KEY,
  user_id UUID NOT NULL
);

CREATE TABLE question_answer
(
  question_id UUID NOT NULL,
  answer_id UUID NOT NULL,
  FOREIGN KEY (question_id) REFERENCES question_user(question_id) ON DELETE CASCADE,
  FOREIGN KEY (answer_id) REFERENCES answer_user(answer_id) ON DELETE CASCADE,
  PRIMARY KEY (question_id, answer_id)
);

CREATE TABLE answer_upvoter
(
  answer_id UUID NOT NULL,
  upvoted_by_user_id UUID NOT NULL,
  FOREIGN KEY (answer_id) REFERENCES answer_user(answer_id) ON DELETE CASCADE,
  PRIMARY KEY (answer_id, upvoted_by_user_id)
)

# --- !Downs

DROP TABLE answer_upvoter;
DROP TABLE question_answer;
DROP TABLE answer_user;
