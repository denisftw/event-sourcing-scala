# --- !Ups

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE tags
(
  tag_id UUID PRIMARY KEY,
  tag_text TEXT NOT NULL
);

CREATE TABLE active_users
(
  user_id UUID PRIMARY KEY
);

CREATE TABLE question_user
(
  question_id UUID PRIMARY KEY,
  user_id UUID NOT NULL
);

CREATE TABLE tag_question
(
  tag_id UUID NOT NULL,
  question_id UUID NOT NULL,
  FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE,
  FOREIGN KEY (question_id) REFERENCES question_user(question_id) ON DELETE CASCADE,
  PRIMARY KEY (tag_id, question_id)
);

# --- !Downs

DROP TABLE tag_question;
DROP TABLE question_user;
DROP TABLE active_users;
DROP TABLE tags;
DROP EXTENSION "uuid-ossp";

