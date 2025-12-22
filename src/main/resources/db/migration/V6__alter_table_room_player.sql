ALTER TABLE room_player ADD status VARCHAR2(1) DEFAULT '0' NOT NULL;

COMMENT ON COLUMN room_player.status IS 'in_room 0 , in_game 1, left 2';