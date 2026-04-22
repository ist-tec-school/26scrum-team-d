/*
「プロになるJava」サンプル
https://gihyo.jp/book/2022/978-4-297-12685-8

Takaaki Sugiyama 2022 copyright reserved.
License: CC0 1.0 Universal
*/

package jp.gihyo.projava.tasklist;
import jp.gihyo.projava.tasklist.HomeController.TaskItem;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskListDao {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    TaskListDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void add(TaskItem taskItem) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(taskItem);
        SimpleJdbcInsert insert =
                new SimpleJdbcInsert(jdbcTemplate)
                        .withTableName("tasklist");
        insert.execute(param);
    }
    public List<TaskItem> findAll() {
        String query = """
            SELECT id, task, task_user_id,description, deadline, done FROM tasklist
            ORDER BY deadline ASC
            """;
        List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
        return mapToTaskItems(result);
    }

    public List<Map<String, Object>> findAllUsers(){
        String query = "SELECT user_id,name FROM users";
        return jdbcTemplate.queryForList(query);
    }

    public int delete(String id) {
        int number = jdbcTemplate.update("DELETE FROM tasklist WHERE id = ?", id);
        return number;
    }

    public int update(TaskItem taskItem) {
        int number = jdbcTemplate.update(
                "UPDATE tasklist SET task = ?, task_user_id = ?, description=?, deadline = ?, done = ? WHERE id = ?",
                taskItem.task(),
                taskItem.taskUserId(),
                taskItem.description(),
                taskItem.deadline(),
                taskItem.done(),
                taskItem.id());
        return number;
    }

    // --- ここから追加したメソッド（ちゃんとクラスの { } の中に入っています） ---

    // 1つのステータスで検索する場合
    public List<TaskItem> findByStatus(int status) {
        String query = "SELECT * FROM tasklist WHERE done = ? ORDER BY deadline ASC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, status);
        return mapToTaskItems(result);
    }

    // 複数のステータス（1と2など）で検索する場合
    public List<TaskItem> findByStatusList(List<Integer> statusList) {
        if (statusList == null || statusList.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", statusList.stream().map(s -> "?").toList());

        String query = "SELECT * FROM tasklist WHERE done IN (" + placeholders + ") ORDER BY deadline ASC";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, statusList.toArray());

        return mapToTaskItems(result);
    }

    private List<TaskItem> mapToTaskItems(List<Map<String, Object>> result) {
        return result.stream()
                .map((Map<String, Object> row) -> new TaskItem(
                        row.get("id").toString(),
                        row.get("task").toString(),
                        row.get("task_user_id") != null ? ((Number)row.get("task_user_id")).intValue() : 0,
                        row.get("description") != null ? row.get("description").toString() : "",
                        row.get("deadline").toString(),
                        ((Number)row.get("done")).intValue()
                ))
                .toList();
    }
}