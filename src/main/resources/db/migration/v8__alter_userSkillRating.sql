ALTER TABLE user_skill_ratings
ADD CONSTRAINT fk_user FOREIGN KEY (userid) REFERENCES users(id),
ADD CONSTRAINT fk_skill FOREIGN KEY (skillid) REFERENCES skills(id);

ALTER TABLE user_skill_ratings ADD CONSTRAINT unique_user_skill UNIQUE (userid, skillid);
ALTER TABLE user_skill_ratings ADD COLUMN proficiency int DEFAULT 0 NOT NULL;

