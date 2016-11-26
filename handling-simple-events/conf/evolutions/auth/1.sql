# --- !Ups

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users
(
    user_id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_code VARCHAR(250) NOT NULL UNIQUE,
    full_name VARCHAR(250) NOT NULL,
    password VARCHAR(250) NOT NULL,
    is_admin BOOL NOT NULL DEFAULT FALSE
);

INSERT INTO users VALUES ('a29ad6b6-09f8-4754-b9fb-04f8b8f19a7f'::uuid,
  'user@example.com', 'Joe Average',
  '$2a$10$niF.amAexQMHaevqlkganeSjvMHfTq/OdISyj8/5BQy1FHvlbi3Ne', true);

CREATE TABLE sessions
(
    session_id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    created TIMESTAMP WITH TIME ZONE NOT NULL,
    updated TIMESTAMP WITH TIME ZONE NOT NULL,
    ttl BIGINT NOT NULL
);

# --- !Downs

DROP TABLE sessions;

DROP TABLE users;

DROP EXTENSION "uuid-ossp";
