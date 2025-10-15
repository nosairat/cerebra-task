create table users(
    id bigint PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(50) UNIQUE NOT NULL
);