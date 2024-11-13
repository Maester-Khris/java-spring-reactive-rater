CREATE TABLE users(
    id int AUTO_INCREMENT NOT NULL PRIMARY KEY,
    useruuid VARCHAR(255),
    username VARCHAR(255),
    password VARCHAR(255),
    created_at timestamp,
    CONSTRAINT unique_user_uuid UNIQUE(useruuid)
);