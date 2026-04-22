CREATE TABLE IF NOT EXISTS departments (
    dept_id INT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS sections (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    section_name VARCHAR(100),
    dept_id INT,
    FOREIGN KEY(dept_id) REFERENCES departments(dept_id)
);

CREATE TABLE IF NOT EXISTS users(
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    section_id INT,
    FOREIGN KEY(section_id) REFERENCES sections(section_id)
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
