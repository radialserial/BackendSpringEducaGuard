CREATE TABLE IF NOT EXISTS user (
    id_user bigint PRIMARY KEY AUTO_INCREMENT,
    about text,
    attempts int NOT NULL,
    email varchar(255) UNIQUE,
    image text,
    name varchar(255),
    password varchar(255),
    release_login datetime,
    role enum('ROLE_ADMIN', 'ROLE_USER'),
    status boolean NOT NULL,
    token text,
    username varchar(255) UNIQUE
);

INSERT INTO user (
    about,
    attempts,
    email,
    image,
    name,
    password,
    release_login,
    role,
    status,
    token,
    username
) VALUES (
    'sobre',
    '10',
    'email@gmail.com',
    'imagem',
    'nome',
    '$2y$12$FknAG6jxiH3jW4JHK7a8WuRWqkU/hXZNWIey3/AQErYebaRh1GOjq',
    '2024-07-24',
    'ROLE_ADMIN',
    true,
    'algum token',
    'nomedousuario'
);

