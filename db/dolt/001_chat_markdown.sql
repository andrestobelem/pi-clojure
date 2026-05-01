CREATE TABLE IF NOT EXISTS rooms (
  id VARCHAR(36) NOT NULL,
  slug VARCHAR(120) NOT NULL,
  title VARCHAR(200) NOT NULL,
  description_markdown TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY rooms_slug_unique (slug)
);

CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(36) NOT NULL,
  handle VARCHAR(80) NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY users_handle_unique (handle)
);

CREATE TABLE IF NOT EXISTS messages (
  id VARCHAR(36) NOT NULL,
  room_id VARCHAR(36) NOT NULL,
  author_id VARCHAR(36) NOT NULL,
  client_txn_id VARCHAR(120) NOT NULL,
  sequence BIGINT NOT NULL,
  body_markdown TEXT NOT NULL,
  body_html TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  edited_at TIMESTAMP NULL,
  deleted_at TIMESTAMP NULL,
  PRIMARY KEY (id),
  UNIQUE KEY messages_room_sequence_unique (room_id, sequence),
  UNIQUE KEY messages_author_txn_unique (author_id, client_txn_id),
  KEY messages_room_created_idx (room_id, created_at),
  CONSTRAINT messages_room_fk FOREIGN KEY (room_id) REFERENCES rooms (id),
  CONSTRAINT messages_author_fk FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS message_events (
  id VARCHAR(36) NOT NULL,
  room_id VARCHAR(36) NOT NULL,
  message_id VARCHAR(36) NOT NULL,
  actor_id VARCHAR(36) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  payload_json JSON NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY message_events_message_created_idx (message_id, created_at),
  KEY message_events_room_created_idx (room_id, created_at),
  CONSTRAINT message_events_room_fk FOREIGN KEY (room_id) REFERENCES rooms (id),
  CONSTRAINT message_events_message_fk FOREIGN KEY (message_id) REFERENCES messages (id),
  CONSTRAINT message_events_actor_fk FOREIGN KEY (actor_id) REFERENCES users (id)
);
