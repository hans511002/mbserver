
CREATE TABLE mb_system(
id INT AUTO_INCREMENT,
system_name VARCHAR(32),
system_state int,
system_desc VARCHAR(64),
UNIQUE INDEX `idx_system_name` (`system_name`),
PRIMARY KEY (`id`)
);


CREATE TABLE mb_user(
id BIGINT AUTO_INCREMENT,
user_name VARCHAR(32),
user_pass VARCHAR(32),
user_nick VARCHAR(32),
user_mob VARCHAR(16),
user_email VARCHAR(64),
user_province VARCHAR(32),
user_city VARCHAR(32),
user_address VARCHAR(32),
reg_time DATETIME DEFAULT NOW(),
user_state int,
PRIMARY KEY (`id`),
UNIQUE INDEX `idx_user_mob` (`user_mob`),
UNIQUE INDEX `idx_user_email` (`user_email`)
);


CREATE TABLE mb_user_system(
rel_id BIGINT AUTO_INCREMENT,
user_id BIGINT,
system_id BIGINT,
active_time DATETIME DEFAULT NOW(),
close_time DATETIME DEFAULT null,
cur_state int,
PRIMARY KEY (`rel_id`)
);


CREATE TABLE mb_user_data(
data_id BIGINT AUTO_INCREMENT,
user_id BIGINT,
system_id BIGINT, 
data_type VARCHAR(32),
data_value text,
UNIQUE INDEX `idx_user_email` (`user_id`,`system_id`,`data_type`),
PRIMARY KEY (`data_id`)
);




