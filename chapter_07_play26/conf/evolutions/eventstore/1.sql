# --- !Ups

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE logs
(
  record_id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  action_name VARCHAR(250) NOT NULL,
  event_data TEXT NOT NULL,
  timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);

# --- !Downs

DROP TABLE logs;

DROP EXTENSION "uuid-ossp";