CREATE DATABASE philosophy_db;

USE philosophy_db;

CREATE TABLE IF NOT EXISTS articles (
    title VARCHAR(255) NOT NULL,
    last_update DATE,
    next_article_title VARCHAR(255),
    
    PRIMARY KEY (title),
    FOREIGN KEY (next_article_title) REFERENCES articles (title)
);
