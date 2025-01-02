CREATE TABLE avatars (
    name TEXT PRIMARY KEY
              UNIQUE
              NOT NULL,
    data BLOB
);
CREATE TABLE comments (rpid INTEGER NOT NULL PRIMARY KEY, oid INTEGER NOT NULL, type INTEGER NOT NULL, mid INTEGER NOT NULL, root INTEGER NOT NULL, parent INTEGER NOT NULL, uname TEXT NOT NULL, current_level INTEGER NOT NULL, location TEXT, message TEXT NOT NULL, "like" INTEGER NOT NULL, ctime INTEGER NOT NULL, pictures TEXT, avatar_url TEXT NOT NULL, vip_type INTEGER NOT NULL DEFAULT (0), floor INTEGER);
CREATE INDEX avatar_url_index ON comments (avatar_url);
CREATE INDEX ctime_index ON comments (ctime DESC);
CREATE INDEX idx_oid_root ON comments (oid, root);
CREATE INDEX like_index ON comments ("like" DESC);
CREATE INDEX oid_index ON comments (oid);
CREATE INDEX root_index ON comments (root);
CREATE INDEX type_index ON comments (type);
