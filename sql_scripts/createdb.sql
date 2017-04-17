CREATE TABLE yelp_user (
	yelping_since	DATE        NOT NULL,
	name          	VARCHAR(30) NOT NULL,
	user_id       	VARCHAR(50),
	average_stars   FLOAT(5),
	PRIMARY KEY   	(user_id)
);

CREATE TABLE friends (
	user_1			VARCHAR(50),
	user_2          VARCHAR(50),
	PRIMARY KEY 	(user_1, user_2),
	FOREIGN KEY 	(user_1) REFERENCES yelp_user (user_id),
	FOREIGN KEY 	(user_2) REFERENCES yelp_user (user_id) ON DELETE NO ACTION
);

CREATE TABLE business(
	business_id		VARCHAR(50),
	city            VARCHAR(50),
	state   		VARCHAR(50),
	name			VARCHAR(100) NOT NULL,
	stars           FLOAT(1)    NOT NULL DEFAULT 0.0,
	PRIMARY KEY     (business_id)
);

CREATE TABLE category(
	business_id  	VARCHAR(50),
	name 			VARCHAR(50),
	PRIMARY KEY 	(business_id, name),
	FOREIGN KEY 	(business_id) REFERENCES business (business_id)
);

CREATE TABLE attributes(
	business_id     VARCHAR(50),
	name			VARCHAR(50),
	PRIMARY KEY 	(business_id, name),
	FOREIGN KEY 	(business_id) REFERENCES business (business_id)
);

CREATE TABLE review(
	user_id 		VARCHAR(50),
	review_id		VARCHAR(50),
	stars   		INTEGER     NOT NULL,
	entry_date		DATE        NOT NULL,
	review_text     TEXT,
	business_id  	VARCHAR(50),
	vote_count	    INTEGER     NOT NULL,
	PRIMARY KEY		(review_id),
	FOREIGN KEY		(user_id) REFERENCES yelp_user (user_id),
	FOREIGN KEY     (business_id)  REFERENCES business (business_id)
);


