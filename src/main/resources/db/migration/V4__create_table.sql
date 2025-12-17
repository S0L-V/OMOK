	-- USERS 테이블
CREATE TABLE users (
    id          VARCHAR2(36)   NOT NULL,
    email       VARCHAR2(50)   NOT NULL UNIQUE,
    pwd         VARCHAR2(100)  NOT NULL,
    created_at  TIMESTAMP      DEFAULT SYSDATE NOT NULL,
    login_type  VARCHAR2(1)    DEFAULT '2' NULL
);

COMMENT ON COLUMN users.login_type IS '카카오1 일반2';

ALTER TABLE users
ADD CONSTRAINT PK_USERS PRIMARY KEY (id);


-- USER_INFO 테이블
CREATE TABLE user_info (
    user_id          VARCHAR2(36) NOT NULL,
    nickname         VARCHAR2(10) NOT NULL UNIQUE,
    total_win        NUMBER       DEFAULT 0 NOT NULL,
    total_lose       NUMBER       DEFAULT 0 NOT NULL,
    total_draw       NUMBER       DEFAULT 0 NOT NULL,
    current_streak   NUMBER       DEFAULT 0 NOT NULL,
    max_win_streak   NUMBER       DEFAULT 0 NOT NULL,
    win_rate         NUMBER(5,2)  DEFAULT 0 NOT NULL,
    coin             NUMBER       DEFAULT 0 NOT NULL,
    last_game_date   TIMESTAMP    NULL
);

COMMENT ON COLUMN user_info.current_streak IS '연승(양수)/연패(음수)';
COMMENT ON COLUMN user_info.max_win_streak IS '최대 연승 기록';
COMMENT ON COLUMN user_info.win_rate IS '승률(%)';
COMMENT ON COLUMN user_info.coin IS '게임 포인트';

ALTER TABLE user_info
ADD CONSTRAINT PK_USER_INFO PRIMARY KEY (user_id);

ALTER TABLE user_info
ADD CONSTRAINT FK_USERS_TO_USER_INFO_1
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;


-- FRIEND 테이블
CREATE TABLE friend (
    id          VARCHAR2(36) NOT NULL,
    user_id     VARCHAR2(36) NOT NULL,
    friend_id   VARCHAR2(36) NOT NULL,
    status      VARCHAR2(20) DEFAULT 'PENDING' NOT NULL,
    created_at  TIMESTAMP    DEFAULT SYSDATE NOT NULL
);

COMMENT ON COLUMN friend.status IS 'PENDING, ACCEPTED, BLOCKED';

ALTER TABLE friend
ADD CONSTRAINT PK_FRIEND PRIMARY KEY (id);

ALTER TABLE friend
ADD CONSTRAINT FK_FRIEND_USER
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE friend
ADD CONSTRAINT FK_FRIEND_FRIEND
FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE friend
ADD CONSTRAINT UK_FRIEND_PAIR UNIQUE (user_id, friend_id);


-- ROOM 테이블
CREATE TABLE room (
    id                VARCHAR2(36) NOT NULL,
    host_user_id       VARCHAR2(36) NOT NULL,
    room_name          VARCHAR2(50) NOT NULL,
    room_pwd           VARCHAR2(36) NULL,
    is_public          VARCHAR2(1)  DEFAULT '1' NOT NULL,
    play_type          VARCHAR2(1)  DEFAULT '1' NOT NULL,
    current_user_cnt   NUMBER       DEFAULT 0 NOT NULL,
    total_user_cnt     NUMBER       DEFAULT 2 NOT NULL,
    created_at         TIMESTAMP    DEFAULT SYSDATE NOT NULL
);

COMMENT ON COLUMN room.is_public IS '공개1 비공개2';
COMMENT ON COLUMN room.play_type IS '개인전1 팀전2';

ALTER TABLE room
ADD CONSTRAINT PK_ROOM PRIMARY KEY (id);

ALTER TABLE room
ADD CONSTRAINT FK_ROOM_HOST
FOREIGN KEY (host_user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE room
ADD CONSTRAINT CHK_ROOM_CNT CHECK (current_user_cnt <= total_user_cnt);


-- ROOM_PLAYER 테이블
CREATE TABLE room_player (
    id           VARCHAR2(36) NOT NULL,
    room_id      VARCHAR2(36) NOT NULL,
    user_id      VARCHAR2(36) NOT NULL,
    stone_color  VARCHAR2(1)  NULL,
    joined_at    TIMESTAMP    DEFAULT SYSDATE NOT NULL
);

COMMENT ON COLUMN room_player.stone_color IS '흑1 백2';

ALTER TABLE room_player
ADD CONSTRAINT PK_ROOM_PLAYER PRIMARY KEY (id);

ALTER TABLE room_player
ADD CONSTRAINT FK_ROOM_PLAYER_ROOM
FOREIGN KEY (room_id) REFERENCES room (id) ON DELETE CASCADE;

ALTER TABLE room_player
ADD CONSTRAINT FK_ROOM_PLAYER_USER
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE room_player
ADD CONSTRAINT UK_ROOM_USER UNIQUE (room_id, user_id);


-- GAME_RESULT 테이블
CREATE TABLE game_result (
    id           VARCHAR2(36) NOT NULL,
    game_id      VARCHAR2(36) NOT NULL,
    room_id      VARCHAR2(36) NOT NULL,
    user_id      VARCHAR2(36) NOT NULL,
    stone_color  VARCHAR2(1)  NOT NULL,
    game_result  VARCHAR2(1)  NOT NULL,
    play_type    VARCHAR2(1)  NOT NULL,
    finished_at  TIMESTAMP    DEFAULT SYSDATE NOT NULL
);

COMMENT ON COLUMN game_result.stone_color IS '흑1 백2';
COMMENT ON COLUMN game_result.game_result IS 'W승 L패 D무';
COMMENT ON COLUMN game_result.play_type IS '개인전1 팀전2';

ALTER TABLE game_result
ADD CONSTRAINT PK_GAME_RESULT PRIMARY KEY (id);

ALTER TABLE game_result
ADD CONSTRAINT FK_GAME_RESULT_ROOM
FOREIGN KEY (room_id) REFERENCES room (id);

ALTER TABLE game_result
ADD CONSTRAINT FK_GAME_RESULT_USER
FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX IDX_GAME_RESULT_USER ON game_result(user_id, finished_at);
CREATE INDEX IDX_GAME_RESULT_GAME ON game_result(game_id);
CREATE INDEX IDX_GAME_RESULT_ROOM ON game_result(room_id);