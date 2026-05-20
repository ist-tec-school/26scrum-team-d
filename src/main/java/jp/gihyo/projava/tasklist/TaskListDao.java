package jp.gihyo.projava.tasklist;

import jp.gihyo.projava.tasklist.HomeController.TaskItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskListDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    TaskListDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void add(TaskItem taskItem) {
        jdbcTemplate.update(
                "INSERT INTO tasklist (id, task, project_id, description, deadline, done,start_date) VALUES (?, ?, ?, ?, ?, ?,?)",
                taskItem.id(), taskItem.task(), taskItem.projectId(),
                taskItem.description(), taskItem.deadline(), taskItem.done(),
                taskItem.start_date()
        );

        if (taskItem.taskUserIds() != null) {
            for (Integer userId : taskItem.taskUserIds()) {
                jdbcTemplate.update(
                        "INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)",
                        taskItem.id(), userId
                );
            }
        }
    }

    public List<TaskItem> findByCondition(String status, String projectId, String deptId, String sectionId, String keyword, String scope, Integer currentUserId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT t.*, p.project_name ");
        sql.append("FROM tasklist t ");
        sql.append("LEFT JOIN projects p ON t.project_id = p.project_id ");
        sql.append("LEFT JOIN task_assignments ta ON t.id = ta.task_id ");
        sql.append("LEFT JOIN users u ON ta.user_id = u.user_id ");
        sql.append("LEFT JOIN sections s ON u.section_id = s.section_id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if ("mine".equals(scope) && currentUserId != null) {
            sql.append(" AND t.id IN (SELECT task_id FROM task_assignments WHERE user_id = ?) ");
            params.add(currentUserId);
        }

        if (!"all".equals(status)) {
            if ("working_group".equals(status)) {
                sql.append(" AND (t.done = 0 OR t.done = 1)");
            } else {
                sql.append(" AND t.done = ?");
                params.add(Integer.parseInt(status));
            }
        }

        if (!"all".equals(projectId) && projectId != null && !projectId.isEmpty()) {
            sql.append(" AND t.project_id = ?");
            params.add(Integer.parseInt(projectId));
        }

        if (!"all".equals(deptId) && deptId != null && !deptId.isEmpty()) {
            sql.append(" AND s.dept_id = ?");
            params.add(Integer.parseInt(deptId));
        }

        if (!"all".equals(sectionId) && sectionId != null && !sectionId.isEmpty()) {
            sql.append(" AND s.section_id = ?");
            params.add(Integer.parseInt(sectionId));
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(t.task) LIKE LOWER(?) OR LOWER(p.project_name) LIKE LOWER(?) OR LOWER(u.name) LIKE LOWER(?))");
            String wildcardKeyword = "%" + keyword + "%";
            params.add(wildcardKeyword);
            params.add(wildcardKeyword);
            params.add(wildcardKeyword);
        }

        sql.append(" ORDER BY t.project_id ASC, t.start_date ASC");

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return mapToTaskItems(result);
    }

    public List<Map<String, Object>> findAllUsers() {
        return jdbcTemplate.queryForList("SELECT user_id, name FROM users");
    }

    public List<Map<String, Object>> findAllProjects() {
        return jdbcTemplate.queryForList("SELECT project_id as id, project_name as name FROM projects");
    }

    public List<Map<String, Object>> findAllDepartments() {
        return jdbcTemplate.queryForList("SELECT * FROM departments");
    }

    public List<Map<String, Object>> findAllSections() {
        return jdbcTemplate.queryForList("SELECT * FROM sections");
    }

    public int delete(String id) {
        return jdbcTemplate.update("DELETE FROM tasklist WHERE id = ?", id);
    }

    @Transactional
    public int update(TaskItem taskItem) {
        int number = jdbcTemplate.update(
                "UPDATE tasklist SET task = ?, project_id = ?, description = ?, deadline = ?, done = ?, start_date = ? WHERE id = ?",
                taskItem.task(), taskItem.projectId(), taskItem.description(), taskItem.deadline(), taskItem.done(), taskItem.start_date(), taskItem.id());

        jdbcTemplate.update("DELETE FROM task_assignments WHERE task_id = ?", taskItem.id());
        if (taskItem.taskUserIds() != null) {
            for (Integer userId : taskItem.taskUserIds()) {
                jdbcTemplate.update("INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)", taskItem.id(), userId);
            }
        }
        return number;
    }

    public int addProject(String projectName) {
        Map<String, Object> parameters = Map.of("project_name", projectName);
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("projects")
                .usingGeneratedKeyColumns("project_id");
        return insert.executeAndReturnKey(parameters).intValue();
    }

    public Map<String, Object> findUserByEmail(String email) {
        String sql = "SELECT user_id AS user_id,name AS name, email AS email, password AS password FROM users WHERE email = ?";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, email);
        return users.isEmpty() ? null : users.get(0);
    }

    // ユーザー保存ロジックを追加
    public void createUser(String name, String email, String encodedPassword) {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, name, email, encodedPassword);
    }

    /**
     * パスワード更新メソッド
     */
    public void updatePassword(String email, String encodedPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        jdbcTemplate.update(sql, encodedPassword, email);
    }

    private List<TaskItem> mapToTaskItems(List<Map<String, Object>> result) {
        return result.stream()
                .map((Map<String, Object> row) -> {
                    String taskId = row.get("ID").toString();
                    List<Integer> userIds = jdbcTemplate.queryForList(
                            "SELECT user_id FROM task_assignments WHERE task_id = ?",
                            Integer.class,
                            taskId
                    );

                    return new TaskItem(
                            taskId,
                            row.get("task").toString(),
                            userIds,
                            row.get("project_id") != null ? ((Number) row.get("project_id")).intValue() : null,
                            row.get("project_name") != null ? row.get("project_name").toString() : "未割当",
                            row.get("description") != null ? row.get("description").toString() : "",
                            row.get("deadline").toString(),
                            ((Number) row.get("done")).intValue(),
                            row.get("start_date")!=null?row.get("start_date").toString():""
                    );
                })
                .toList();
    }

    public Integer addDepartment(String deptName) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("departments")
                .usingGeneratedKeyColumns("dept_id");
        Map<String, Object> params = new HashMap<>();
        params.put("dept_name", deptName);
        Number key = insert.executeAndReturnKey(params);
        return key.intValue();
    }
    public Integer findDeptIdByName(String deptName) {
        String sql = "SELECT dept_id FROM departments WHERE dept_name = ?";
        List<Integer> ids = jdbcTemplate.queryForList(sql, Integer.class, deptName);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public Integer addSection(String sectionName, Integer deptId) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("sections")
                .usingGeneratedKeyColumns("section_id");
        Map<String, Object> params = new HashMap<>();
        params.put("section_name", sectionName);
        params.put("dept_id", deptId);
        Number key = insert.executeAndReturnKey(params);
        return key.intValue();
    }
    public Integer findSectionIdByName(String sectionName, Integer deptId) {
        String sql = "SELECT section_id FROM sections WHERE section_name = ? AND dept_id = ?";
        List<Integer> ids = jdbcTemplate.queryForList(sql, Integer.class, sectionName, deptId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public void createUser(String name, String email, String encodedPassword, Integer sectionId) {
        String sql = "INSERT INTO users (name, email, password, section_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, name, email, encodedPassword, sectionId);
    }
    public List<String> findAllDeptNames() {
        String sql = "SELECT dept_name FROM departments";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public List<String> findAllDeptKanas() {
        String sql = "SELECT dept_kana FROM departments";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public Integer addDepartment(String deptName, String deptKana) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("departments")
                .usingGeneratedKeyColumns("dept_id");
        Map<String, Object> params = new HashMap<>();
        params.put("dept_name", deptName);
        params.put("dept_kana", deptKana);
        return insert.executeAndReturnKey(params).intValue();
    }
}