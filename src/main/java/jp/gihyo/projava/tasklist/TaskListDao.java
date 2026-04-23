/*
「プロになるJava」サンプル
https://gihyo.jp/book/2022/978-4-297-12685-8

Takaaki Sugiyama 2022 copyright reserved.
License: CC0 1.0 Universal
*/

package jp.gihyo.projava.tasklist;

import jp.gihyo.projava.tasklist.HomeController.TaskItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
                "INSERT INTO tasklist (id, task, project_id, description, deadline, done) VALUES (?, ?, ?, ?, ?, ?)",
                taskItem.id(), taskItem.task(), taskItem.projectId(),
                taskItem.description(), taskItem.deadline(), taskItem.done()
        );

        // 2. 中間テーブルへの担当者登録
        if (taskItem.taskUserIds() != null) {
            for (Integer userId : taskItem.taskUserIds()) {
                jdbcTemplate.update(
                        "INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)",
                        taskItem.id(), userId
                );
            }
        }
    }

    // --- フィルタリング用メソッド ---
    public List<TaskItem> findByCondition(String status, String projectId, String deptId, String sectionId, String keyword) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT t.*, p.project_name ");
        sql.append("FROM tasklist t ");
        sql.append("LEFT JOIN projects p ON t.project_id = p.project_id ");
        sql.append("LEFT JOIN task_assignments ta ON t.id = ta.task_id ");
        sql.append("LEFT JOIN users u ON ta.user_id = u.user_id ");
        sql.append("LEFT JOIN sections s ON u.section_id = s.section_id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (!"all".equals(status)) {
            sql.append(" AND t.done = ?");
            params.add("working".equals(status) ? 1 : Integer.parseInt(status));
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
            sql.append(" AND (LOWER(t.task) LIKE LOWER(?) OR LOWER(u.name) LIKE LOWER(?))");
            String wildcardKeyword = "%" + keyword + "%";
            params.add(wildcardKeyword); // task 用
            params.add(wildcardKeyword); // u.name 用
        }

        sql.append(" ORDER BY t.deadline ASC");

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return mapToTaskItems(result);
    }

    // --- マスターデータ取得用 ---
    public List<Map<String, Object>> findAllUsers() {
        String query = "SELECT user_id, name FROM users";
        return jdbcTemplate.queryForList(query);
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

    public List<Map<String, Object>> findSectionsByDeptId(int deptId) {
        return jdbcTemplate.queryForList("SELECT * FROM sections WHERE dept_id = ?", deptId);
    }

    // --- 更新・削除 ---
    public int delete(String id) {
        return jdbcTemplate.update("DELETE FROM tasklist WHERE id = ?", id);
    }

    @Transactional
    public int update(TaskItem taskItem) {
        int number = jdbcTemplate.update(
                "UPDATE tasklist SET task = ?, project_id = ?, description = ?, deadline = ?, done = ? WHERE id = ?",
                taskItem.task(),
                taskItem.projectId(),
                taskItem.description(),
                taskItem.deadline(),
                taskItem.done(),
                taskItem.id());

        jdbcTemplate.update("DELETE FROM task_assignments WHERE task_id = ?", taskItem.id());
        if (taskItem.taskUserIds() != null) {
            for (Integer userId : taskItem.taskUserIds()) {
                jdbcTemplate.update(
                        "INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)",
                        taskItem.id(), userId
                );
            }
        }
        return number;
    }

    public int addProject(String projectName) {
        Map<String, Object> parameters = Map.of("project_name", projectName);
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("projects")
                .usingGeneratedKeyColumns("project_id");
        Number key = insert.executeAndReturnKey(parameters);
        return key.intValue();
    }

    // --- マッピング用 ---
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
                            ((Number) row.get("done")).intValue()
                    );
                })
                .toList();
    }
}