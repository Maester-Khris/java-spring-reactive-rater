CREATE TABLE user_skill_ratings(
	id int AUTO_INCREMENT NOT NULL PRIMARY KEY, 
	userId int,
	skillId int,
	rating int
);