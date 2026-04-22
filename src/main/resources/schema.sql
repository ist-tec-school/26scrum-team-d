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

CREATE TABLE IF NOT EXISTS projects (
    project_id INT PRIMARY KEY AUTO_INCREMENT,
    project_name VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS tasklist (
    id   VARCHAR(8)  PRIMARY KEY,
    task VARCHAR(256),
    project_id INT,
    description VARCHAR(512),
    deadline VARCHAR(10),
    done INT,
    FOREIGN KEY(project_id) REFERENCES projects(project_id)
);

CREATE TABLE IF NOT EXISTS task_assignments(
    task_id VARCHAR(8),
    user_id INT,
    PRIMARY KEY (task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES tasklist(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
)

