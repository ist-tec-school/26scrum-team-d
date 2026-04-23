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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping; // GETからPOSTに変えるため

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@Controller
public class HomeController {
    record TaskItem(
            String id,
            @NotBlank(message = "タスクを入力してください")
            String task,
            Integer taskUserId,
            Integer projectId,
            String projectName,
            String description,
            @NotBlank(message = "期限を入力してください")
            String deadline,
            Integer done
    ) {}

    private List<TaskItem> taskItems = new ArrayList<>();
    private final TaskListDao dao;

    @Autowired
    HomeController(TaskListDao dao) {
        this.dao = dao;
    }

    @GetMapping("/list")
    String listItems(Model model,
                     @RequestParam(value = "status", defaultValue = "all") String status,
                     @RequestParam(value = "projectId", defaultValue = "all") String projectId,
                     @RequestParam(value = "deptId", defaultValue = "all") String deptId,
                     @RequestParam(value = "sectionId", defaultValue = "all") String sectionId,
                     @RequestParam(value = "keyword", defaultValue = "") String keyword) {

        List<TaskItem> taskItems = dao.findFiltered(status, projectId, deptId, sectionId, keyword);

        model.addAttribute("taskList", taskItems);
        model.addAttribute("userList", dao.findAllUsers());
        model.addAttribute("projectList", dao.findAllProjects());
        model.addAttribute("deptList", dao.findAllDepartments());

        // --- ここを修正 ---
        // 部署が選択されている（all以外）場合は、その部署の課だけを取得する
        if (!"all".equals(deptId)) {
            model.addAttribute("sectionList", dao.findSectionsByDeptId(Integer.parseInt(deptId)));
        } else {
            model.addAttribute("sectionList", dao.findAllSections());
        }
        // -----------------

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProject", projectId);
        model.addAttribute("selectedDept", deptId);
        model.addAttribute("selectedSection", sectionId);
        model.addAttribute("keyword", keyword);

        return "home";
    }

    @PostMapping("/add")
    String addItem(@Validated TaskItem item, // バリデーション実行
                   BindingResult result,    // エラー結果を受け取る
                   Model model,
                   @RequestParam(value="status", defaultValue="all") String status,
                   @RequestParam(value="keyword", defaultValue="") String keyword) {

        // 1. バリデーションエラーがある場合
        if (result.hasErrors()) {
            // 現在のリストやユーザー、プロジェクト情報を再取得して画面を再表示
            List<TaskItem> taskItems = dao.findByCondition(status, keyword);
            model.addAttribute("taskList", taskItems);
            model.addAttribute("userList", dao.findAllUsers());
            model.addAttribute("projectList", dao.findAllProjects());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("keyword", keyword);

            // 「必須事項が未入力です」という共通メッセージを渡す
            model.addAttribute("errorMessage", "必須事項が未入力です");
            return "home"; // リダイレクトせず、そのままhome.htmlを表示
        }

        // 2. エラーがない場合はIDを発行して登録
        String id = UUID.randomUUID().toString().substring(0, 8);
        // IDをセットした新しいRecordを作成（Recordは不変なため）
        TaskItem newItem = new TaskItem(id, item.task(), item.taskUserId(), item.projectId(),
                "", item.description(), item.deadline(), 0);

        dao.add(newItem);
        return "redirect:/list";
    }

    @GetMapping("/delete")
    String deleteItem(@RequestParam("id") String id) {
        dao.delete(id);
        return "redirect:/list";
    }

    // HomeController.java 118行目付近
    @GetMapping("/update")
    String updateItem(@RequestParam("id") String id,
                      @RequestParam("task") String task,
                      @RequestParam(value = "taskUserId", required = false) Integer taskUserId,
                      @RequestParam(value = "projectId", required = false) String projectId,
                      @RequestParam(value = "newProjectName", required = false) String newProjectName, // ←これを受け取っている
                      @RequestParam("description") String description,
                      @RequestParam("deadline") String deadline,
                      @RequestParam("done") int done) {

        Integer targetProjectId = null;
        if ("new".equals(projectId) && newProjectName != null && !newProjectName.isEmpty()) {
            targetProjectId = dao.addProject(newProjectName);
        } else if (projectId != null && !projectId.isEmpty()) {
            targetProjectId = Integer.parseInt(projectId);
        }

        TaskItem taskItem = new TaskItem(id, task, taskUserId, targetProjectId, newProjectName, description, deadline, done);

        dao.update(taskItem);
        return "redirect:/list";
    }
}