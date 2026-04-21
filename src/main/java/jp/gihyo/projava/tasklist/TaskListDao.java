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
            SELECT id, task, task_user AS taskUser,description, deadline, done FROM tasklist
            """;
        List<Map<String,Object>> result = jdbcTemplate.queryForList(query);
        return mapToTaskItems(result);
    }

    public List<String> findAllUsers(){
        String query = "SELECT name FROM Users";
        return jdbcTemplate.queryForList(query, String.class);
    }

    public int delete(String id) {
        int number = jdbcTemplate.update("DELETE FROM tasklist WHERE id = ?", id);
        return number;
    }

    public int update(TaskItem taskItem) {
        int number = jdbcTemplate.update(
                "UPDATE tasklist SET task = ?, task_user = ?, description=?, deadline = ?, done = ? WHERE id = ?",
                taskItem.task(),
                taskItem.taskUser(),
                taskItem.description(),
                taskItem.deadline(),
                taskItem.done(),
                taskItem.id());
        return number;
    }

    // --- ここから追加したメソッド（ちゃんとクラスの { } の中に入っています） ---

    // 1つのステータスで検索する場合
    public List<TaskItem> findByStatus(int status) {
        String query = "SELECT * FROM tasklist WHERE done = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, status);
        return mapToTaskItems(result);
    }

    // 複数のステータス（1と2など）で検索する場合
    public List<TaskItem> findByStatusList(List<Integer> statusList) {
        // ① 引数が空っぽだった時のために、安全策として空のリストを返すようにしておく
        if (statusList == null || statusList.isEmpty()) {
            return List.of();
        }

        // ② 引数の数だけ「?」を準備する（例: [1, 2] なら "?,?" になる）
        String placeholders = String.join(",", statusList.stream().map(s -> "?").toList());

        // ③ SQL文を組み立てる。ここで「(1, 2)」を「(?, ?)」に置き換えた！
        String query = "SELECT * FROM tasklist WHERE done IN (" + placeholders + ")";

        // ④ 実行する時に、初めて「箱の中身（1, 2）」を流し込む
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query, statusList.toArray());

        // ⑤ 画面表示用の形式に変換して返す
        return mapToTaskItems(result);
    }

    // 共通の変換処理（findAllなどから呼び出される）
    private List<TaskItem> mapToTaskItems(List<Map<String, Object>> result) {
        return result.stream()
                .map((Map<String, Object> row) -> new TaskItem(
                        row.get("id").toString(),
                        row.get("task").toString(),
                        row.get("taskUser")!=null?row.get("taskUser").toString():"",
                        row.get("description") != null ? row.get("description").toString() : "",
                        row.get("deadline").toString(),
                        ((Number)row.get("done")).intValue()
                ))
                .toList();
    }
}