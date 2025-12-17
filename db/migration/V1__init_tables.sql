CREATE TABLE "game_history" (
	"game_id"	VARCHAR2(36)		NOT NULL,
	"room_id"	VARCHAR2(36)		NOT NULL,
	"play_type"	VARCHAR2(10)		NOT NULL,
	"winner_team"	VARCHAR2(10)		NOT NULL,
	"started_at"	TIMESTAMP		NOT NULL,
	"finished_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "game_history"."play_type" IS '개인1 팀2';

COMMENT ON COLUMN "game_history"."winner_team" IS '흑1 백2';

CREATE TABLE "user_info" (
	"user_id"	VARCHAR2(36)		NOT NULL,
	"nickname"	VARCHAR2(10)		NOT NULL,
	"total_win"	NUMBER		NOT NULL,
	"total_lose"	NUMBER		NOT NULL,
	"coin"	NUMBER		NOT NULL
);

CREATE TABLE "friend" (
	"id"	VARCHAR2(36)		NOT NULL,
	"user_id"	VARCHAR2(36)		NOT NULL,
	"friend_id"	VARCHAR2(36)		NOT NULL,
	"status"	VARCHAR2(36)		NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "friend"."status" IS 'PENDING, ACCEPTED, BLOCKED';

CREATE TABLE "room_player" (
	"id"	VARCHAR2(36)		NOT NULL,
	"user_id"	VARCHAR2(36)		NOT NULL,
	"joined_at"	TIMESTAMP		NOT NULL,
	"stone_color"	VARCHAR2(36)		NOT NULL,
	"room_id"	VARCHAR2(36)		NOT NULL
);

COMMENT ON COLUMN "room_player"."stone_color" IS '흑1 / 백2';

CREATE TABLE "user" (
	"id"	VARCHAR2(36)		NOT NULL,
	"email"	VARCHAR2(36)		NOT NULL,
	"pwd"	VARCHAR2(36)		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"login_type"	VARCHAR2(1)		NULL
);

COMMENT ON COLUMN "user"."login_type" IS '카카오는1 일반2';

CREATE TABLE "room" (
	"id"	VARCHAR2(36)		NOT NULL,
	"host_user_id"	VARCHAR2(36)		NOT NULL,
	"room_name"	VARCHAR2(36)		NOT NULL,
	"room_pwd"	VARCHAR2(36)		NULL,
	"is_public"	VARCHAR2(1)		NOT NULL,
	"play_type"	VARCHAR2(1)		NOT NULL,
	"current_user_cnt"	NUMBER		NOT NULL,
	"total_user_cnt"	NUMBER		NOT NULL
);

COMMENT ON COLUMN "room"."is_public" IS '방 공개 여부 public 1private 2';

COMMENT ON COLUMN "room"."play_type" IS '개인전 1  팀전 2';

CREATE TABLE "point_history" (
	"id"	VARCHAR(36)		NOT NULL,
	"game_id"	VARCHAR2(36)		NOT NULL,
	"user_id"	VARCHAR2(36)		NOT NULL,
	"created_at"	TIMESTAMP		NULL
);

CREATE TABLE "game_players" (
	"id"	VARCHAR2(36)		NOT NULL,
	"game_id"	VARCHAR2(36)		NOT NULL,
	"user_id"	VARCHAR2(36)		NOT NULL,
	"stone_color"	VARCHAR2(36)		NOT NULL,
	"is_winner"	VARCHAR2(1)		NOT NULL,
	"joined_at"	TIMESTAMP		NULL
);

ALTER TABLE "game_history" ADD CONSTRAINT "PK_GAME_HISTORY" PRIMARY KEY (
	"game_id"
);

ALTER TABLE "user_info" ADD CONSTRAINT "PK_USER_INFO" PRIMARY KEY (
	"user_id"
);

ALTER TABLE "friend" ADD CONSTRAINT "PK_FRIEND" PRIMARY KEY (
	"id"
);

ALTER TABLE "room_player" ADD CONSTRAINT "PK_ROOM_PLAYER" PRIMARY KEY (
	"id"
);

ALTER TABLE "user" ADD CONSTRAINT "PK_USER" PRIMARY KEY (
	"id"
);

ALTER TABLE "room" ADD CONSTRAINT "PK_ROOM" PRIMARY KEY (
	"id"
);

ALTER TABLE "point_history" ADD CONSTRAINT "PK_POINT_HISTORY" PRIMARY KEY (
	"id"
);

ALTER TABLE "game_players" ADD CONSTRAINT "PK_GAME_PLAYERS" PRIMARY KEY (
	"id"
);

ALTER TABLE "user_info" ADD CONSTRAINT "FK_user_TO_user_info_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "user" (
	"id"
);