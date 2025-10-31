create table users (
	id_user SERIAL PRIMARY KEY,
	email VARCHAR(255) not null,
	password_hash VARCHAR(100) not null,
	full_name VARCHAR(255),
	created_at timestamp default current_timestamp
);
