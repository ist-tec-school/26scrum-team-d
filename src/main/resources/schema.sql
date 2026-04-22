CREATE TABLE IF NOT EXISTS users(
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS tasklist (
    id   VARCHAR(8)  PRIMARY KEY,
    task VARCHAR(256),
    task_user_id INT,
    description VARCHAR(512),
    deadline VARCHAR(10),
    done INT,
    FOREIGN KEY(task_user_id) REFERENCES users(user_id)
);
