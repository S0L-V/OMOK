CREATE TABLE flyway_test (
  id NUMBER PRIMARY KEY,
  msg VARCHAR2(100)
);

INSERT INTO flyway_test (id, msg) VALUES (1, 'hello flyway');
