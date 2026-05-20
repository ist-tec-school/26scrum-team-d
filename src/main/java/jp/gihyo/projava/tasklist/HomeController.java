/*
「プロになるJava」サンプル
https://gihyo.jp/book/2022/978-4-297-12685-8

Takaaki Sugiyama 2022 copyright reserved.
License: CC0 1.0 Universal
*/
package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@Controller
public class HomeController {
    record TaskItem(
            String id,
            @NotBlank @Size(max= 255) String task,
            List<Integer> taskUserIds,
            Integer projectId,
            String projectName,
            @Size(max=200)String description,
            @NotBlank String deadline,
            Integer done,
            @NotBlank String start_date
    ) {}

    private List<TaskItem> taskItems = new ArrayList<>();
    private final TaskListDao dao;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Autowired
    HomeController(TaskListDao dao, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/list")
    String listItems(Model model,
                     @AuthenticationPrincipal UserDetails userDetails, // ★ログイン情報を取得
                     @RequestParam(value = "scope", defaultValue = "mine") String scope, // ★デフォルトはmine
                     @RequestParam(value = "status", defaultValue = "working_group") String status,
                     @RequestParam(value = "projectId", defaultValue = "all") String projectId,
                     @RequestParam(value = "deptId", defaultValue = "all") String deptId,
                     @RequestParam(value = "sectionId", defaultValue = "all") String sectionId,
                     @RequestParam(value = "keyword", defaultValue = "") String keyword) {

        // ガントチャートのタスクからホーム画面への遷移
        if (status == null || status.isEmpty()) {
            status = "all";
            scope = "all";
        }

        // ★ログイン中のメールアドレスから、DB上のユーザー情報を取得してIDを取り出す
        Map<String, Object> user = dao.findUserByEmail(userDetails.getUsername());
        Integer currentUserId = (Integer) user.get("user_id");
        String loginUserName = (String) user.get("name");
        String loginUserEmail = (String) user.get("email");
        String deptName = (String) user.get("dept_name");
        String sectionName = (String) user.get("section_name");
        String loginUserDept = deptName + " " + sectionName;

        List<TaskItem> taskItems = dao.findByCondition(status, projectId, deptId, sectionId, keyword, scope, currentUserId);

        // 2. 画面（Thymeleaf）に渡すデータをセット
        model.addAttribute("loginUserName", loginUserName);
        model.addAttribute("loginUserEmail", loginUserEmail);
        model.addAttribute("loginUserDept", loginUserDept);
        model.addAttribute("taskList", taskItems);
        model.addAttribute("userList", dao.findAllUsers());
        model.addAttribute("projectList", dao.findAllProjects());

        // 部署と課のリストもプルダウンに表示するために必要です（DAOにメソッドがある前提）
        model.addAttribute("deptList", dao.findAllDepartments());
        model.addAttribute("sectionList", dao.findAllSections());

        // 現在選ばれている値を保持（HTML側の th:selected や hidden で使用）
        model.addAttribute("selectedScope", scope);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProject", projectId);
        model.addAttribute("selectedDept", deptId);
        model.addAttribute("selectedSection", sectionId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("taskItem", new TaskItem("", "", List.of(), null, "", "", "", 0,""));
        // listItems メソッド内
        String today = java.time.LocalDate.now().toString();
        String twoDaysLater = java.time.LocalDate.now().plusDays(2).toString();

        model.addAttribute("today", today);
        model.addAttribute("twoDaysLater", twoDaysLater);
        return "home";
    }

    @PostMapping("/add")
    String addItem(@Validated @ModelAttribute("taskItem") TaskItem item, // 1. 引数をRecordに変更してバリデーション
                   BindingResult result,
                   Model model,
                   @RequestParam(value="projectId", required=false) Integer projectId,
                   @RequestParam(value="newProjectName", required=false) String newProjectName,
                   @RequestParam(value="status", defaultValue="all") String status,
                   @RequestParam(value="keyword", defaultValue="") String keyword) {

        // 2. エラー判定を追加
        boolean isPastDate = false;
        if (item.deadline() != null && !item.deadline().isEmpty()) {
            java.time.LocalDate deadlineDate = java.time.LocalDate.parse(item.deadline());
            if (deadlineDate.isBefore(java.time.LocalDate.now())) {
                isPastDate = true;
            }
        }

        boolean isProjectNameTooLong = (newProjectName != null && newProjectName.length()>50);

        // 開始予定日と期限日の前後関係チェック（保存を阻止する最終ガード）
        boolean isInvalidDateOrder = false;
        if (item.start_date() != null && !item.start_date().isEmpty() &&
                item.deadline() != null && !item.deadline().isEmpty()) {
            java.time.LocalDate start = java.time.LocalDate.parse(item.start_date());
            java.time.LocalDate deadline = java.time.LocalDate.parse(item.deadline());
            if (deadline.isBefore(start)) {
                isInvalidDateOrder = true;
            }
        }

        if (result.hasErrors()|| isPastDate || isProjectNameTooLong || isInvalidDateOrder) {
            List<TaskItem> taskItems = dao.findByCondition(status, "all", "all", "all", keyword, "all", null);
            model.addAttribute("taskList", taskItems);
            model.addAttribute("userList", dao.findAllUsers());
            model.addAttribute("projectList", dao.findAllProjects());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("keyword", keyword);

            String msg = "入力内容に不備があります";
            if(isPastDate){
                msg = "過去の日付は入力できません";
            } else if (isProjectNameTooLong) {
                msg = "プロジェクト名が長すぎます（50文字以内）";
            } else if (result.hasFieldErrors("task")) {
                msg = "タスク名を入力してください（長すぎる場合もエラーになります）";
            } else if (result.hasFieldErrors("description")) {
                msg = "説明文は200文字以内で入力してください";
            }

            model.addAttribute("errorMessage", msg);
            return "home";
        }

        // 3. 正常系のプロジェクト登録ロジック
        Integer targetProjectId = null;
        if (Integer.valueOf(0).equals(projectId) && newProjectName != null && !newProjectName.isEmpty()) {
            targetProjectId = dao.addProject(newProjectName);
        } else if (projectId != null && !Integer.valueOf(0).equals(projectId)) {
            targetProjectId = projectId;
        }

        String id = UUID.randomUUID().toString().substring(0, 8);
        TaskItem newItem = new TaskItem(
                id,                 // 生成したID
                item.task(),
                item.taskUserIds(),
                targetProjectId,    // 判定したプロジェクトID
                "",
                item.description(),
                item.deadline(),
                item.done(),
                item.start_date()
        );

        dao.add(newItem);
        return "redirect:/list#task-list-top";
    }

    @GetMapping("/delete")
    String deleteItem(@RequestParam("id") String id,
                      @RequestParam(value = "scope", defaultValue = "mine") String scope,
                      @RequestParam(value = "status", defaultValue = "working_group") String status,
                      @RequestParam(value = "projectId", defaultValue = "all") String projectId,
                      @RequestParam(value = "deptId", defaultValue = "all") String deptId,
                      @RequestParam(value = "sectionId", defaultValue = "all") String sectionId,
                      @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        dao.delete(id);

        // パラメータを維持してリダイレクト
        String encodedKeyword = java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
        return String.format("redirect:/list?scope=%s&status=%s&projectId=%s&deptId=%s&sectionId=%s&keyword=%s#task-list-top",
                scope, status, projectId, deptId, sectionId, encodedKeyword);
    }

    @PostMapping("/update")
    String updateItem(@Validated @ModelAttribute("taskItem") TaskItem item,
                      BindingResult result,
                      Model model,
                      @RequestParam(value="scope", defaultValue="mine") String scope,
                      @RequestParam(value="projectId", required=false) Integer projectId,
                      @RequestParam(value="filterProjectId", defaultValue="all") String filterProjectId,
                      @RequestParam(value="newProjectName", required=false) String newProjectName,
                      @RequestParam(value="status", defaultValue="all") String status,
                      @RequestParam(value="deptId", defaultValue="all") String deptId,
                      @RequestParam(value="sectionId", defaultValue="all") String sectionId,
                      @RequestParam(value="keyword", defaultValue="") String keyword){

        // 4. バリデーションエラーの判定
        boolean isPastDate = false;
        if (item.deadline() != null && !item.deadline().isEmpty()) {
            if (java.time.LocalDate.parse(item.deadline()).isBefore(java.time.LocalDate.now())){
                isPastDate = true;
            }
        }

        boolean isProjectNameTooLong = (newProjectName != null && newProjectName.length()>50);

    // 開始予定日と期限日の前後関係チェック
        boolean isInvalidDateOrder = false;
        if (item.start_date() != null && !item.start_date().isEmpty() &&
                item.deadline() != null && !item.deadline().isEmpty()) {
            java.time.LocalDate start = java.time.LocalDate.parse(item.start_date());
            java.time.LocalDate deadline = java.time.LocalDate.parse(item.deadline());
            if (deadline.isBefore(start)) {
                isInvalidDateOrder = true;
            }
        }
        if (result.hasErrors()|| isPastDate || isProjectNameTooLong || isInvalidDateOrder) {
            // リストの再取得（画面表示を維持するため）
            List<TaskItem> taskItems = dao.findByCondition(status, "all", "all", "all", keyword, "all", null);
            model.addAttribute("taskList", taskItems);
            model.addAttribute("userList", dao.findAllUsers());
            model.addAttribute("projectList", dao.findAllProjects());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("keyword", keyword);
            if (isInvalidDateOrder) {
                model.addAttribute("isUpdateError", true);
                return "home";
            }
            // 4. ★追加：エラー内容を具体的に分岐
            String msg = "入力内容に不備があります。";
            if (isPastDate) {
                msg = "過去の日付は指定できません。";
            } else if (isProjectNameTooLong) {
                msg = "プロジェクト名が長すぎます（50文字以内）";
            } else if (result.hasFieldErrors("task")) {
                msg = "タスク名が長すぎるか、未入力です";
            } else if (result.hasFieldErrors("description")) {
                msg = "説明文は200文字以内で入力してください";
            }
            model.addAttribute("errorMessage", msg);
            // 5. ダイアログ制御用のフラグとメッセージ
            model.addAttribute("isUpdateError", true);
            model.addAttribute("updateErrorMessage", "更新に失敗しました。" + msg);
            return "home"; // redirectせずhomeを返す
        }

        Integer targetProjectId = null;
        if (Integer.valueOf(0).equals(projectId) && newProjectName != null && !newProjectName.isEmpty()) {
            targetProjectId = dao.addProject(newProjectName);
        } else if (projectId != null && !Integer.valueOf(0).equals(projectId)) {
            targetProjectId = projectId;
        }

     // 更新用データの作成
        TaskItem updateData = new TaskItem(
                item.id(),
                item.task(),
                item.taskUserIds(),
                targetProjectId, // projectId
                "",              // projectName (更新時は空文字またはDAOで取得)
                item.description(),
                item.deadline(),
                item.done(),
                item.start_date()
        );


        dao.update(updateData);
        // 1. まず日本語のキーワードを安全な形式に変換する
        String encodedKeyword = java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
        return String.format("redirect:/list?scope=%s&status=%s&projectId=%s&deptId=%s&sectionId=%s&keyword=%s#task-list-top",
                scope, status, filterProjectId, deptId, sectionId, encodedKeyword);
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

