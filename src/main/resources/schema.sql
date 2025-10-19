CREATE TABLE IF NOT EXISTS  skills (
  id INT AUTO_INCREMENT PRIMARY KEY,
  skilluuid VARCHAR(255),
  skillname VARCHAR(255),
  skillicon VARCHAR(255),
  upvote INT,
  downvote INT,
  rating INT,
  version INT
);
CREATE TABLE IF NOT EXISTS  users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    useruuid VARCHAR(255) UNIQUE,
    username VARCHAR(255),
    password VARCHAR(255),
    created_at TIMESTAMP
);
