CREATE TABLE IF NOT EXISTS users(
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS tasklist (
    id   VARCHAR(8)  PRIMARY KEY,
    task VARCHAR(256),
    task_user VARCHAR(256),
    deadline VARCHAR(10),
    done INT
);
