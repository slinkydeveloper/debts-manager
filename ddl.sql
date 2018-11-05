CREATE TABLE IF NOT EXISTS "User"(
  username VARCHAR(100) UNIQUE NOT NULL PRIMARY KEY,
  password CHAR(64) UNIQUE NOT NULL
  );

CREATE TABLE IF NOT EXISTS UserRelationship(
  "from" VARCHAR(100) NOT NULL REFERENCES "User" (username),
  "to" VARCHAR(100) NOT NULL REFERENCES "User" (username)
  );

CREATE TABLE IF NOT EXISTS Transaction(
                                        id SERIAL UNIQUE NOT NULL PRIMARY KEY,
                                        description TEXT NOT NULL,
                                        "from" VARCHAR(100) NOT NULL REFERENCES "User" (username),
  "to" VARCHAR(100) NOT NULL REFERENCES "User" (username),
  at TIMESTAMP NOT NULL,
  value DOUBLE PRECISION
  );
