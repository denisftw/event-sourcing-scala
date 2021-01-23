# --- !Ups

INSERT INTO logs(record_id, action_name, event_data, timestamp)
VALUES ('f4f4545a-29ae-4dd1-b480-d6a8d156ebca'::uuid, 'user-activated',
  '{"id":"a29ad6b6-09f8-4754-b9fb-04f8b8f19a7f"}',
  '2000-01-01 12:00:00.000000') ON CONFLICT DO NOTHING;

# --- !Downs

DELETE FROM logs WHERE record_id =
   'f4f4545a-29ae-4dd1-b480-d6a8d156ebca'::uuid;
