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
        SELECT t.*, p.project_name 
        FROM tasklist t
        LEFT JOIN projects p ON t.project_id = p.project_id
        ORDER BY t.deadline ASC
        """;
        List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
        return mapToTaskItems(result);
    }

    public List<Map<String, Object>> findAllUsers() {
        String query = "SELECT user_id,name FROM users";
        return jdbcTemplate.queryForList(query);
    }

        public List<Map<String, Object>> findAllProjects() {
            return jdbcTemplate.queryForList("SELECT project_id as id, project_name as name FROM projects");
        }


    public int delete(String id) {
        int number = jdbcTemplate.update("DELETE FROM tasklist WHERE id = ?", id);
        return number;
    }

    public int update(TaskItem taskItem) {
        int number = jdbcTemplate.update(
                "UPDATE tasklist SET task = ?, task_user_id = ?, project_id = ?, description = ?, deadline = ?, done = ? WHERE id = ?",
                taskItem.task(),
                taskItem.taskUserId(),
                taskItem.projectId(), // 追加：プロジェクトIDの更新
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


    public List<TaskItem> findByProjectId(int projectId) {
        String query = """
    SELECT t.*, p.project_name 
    FROM tasklist t
    LEFT JOIN projects p ON t.project_id = p.project_id
    WHERE t.project_id = ?
    ORDER BY t.deadline ASC
    """;
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, projectId);
        return mapToTaskItems(result);
    }

    // 新しいプロジェクトをDBに登録し、自動で割り振られたIDを返すメソッド
    public int addProject(String projectName) {
        // 1. 挿入したいデータを「カラム名」と「値」のペアとして準備します
        Map<String, Object> parameters = Map.of("project_name", projectName);

        // 2. SimpleJdbcInsertを使って、projectsテーブルへのデータ挿入を準備します
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("projects") // 挿入先のテーブル名
                .usingGeneratedKeyColumns("project_id"); // 自動採番されるIDのカラム名

        // 3. 実行して、生成されたIDを取得し、int型に変換して返します
        Number key = insert.executeAndReturnKey(parameters);
        return key.intValue();
    }

    private List<TaskItem> mapToTaskItems(List<Map<String, Object>> result) {
        return result.stream()
                .map((Map<String, Object> row) -> new TaskItem(
                        row.get("id").toString(),
                        row.get("task").toString(),
                        row.get("task_user_id") != null ? ((Number)row.get("task_user_id")).intValue() : 0,
                        row.get("project_id") != null ? ((Number)row.get("project_id")).intValue() : 0, // 追加
                        row.get("project_name") != null ? row.get("project_name").toString() : "未割当", // 追加
                        row.get("description") != null ? row.get("description").toString() : "",
                        row.get("deadline").toString(),
                        ((Number)row.get("done")).intValue()
                ))
                .toList();
    }
}

