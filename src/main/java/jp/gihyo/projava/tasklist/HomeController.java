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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@Controller
public class HomeController {
    record TaskItem(String id, String task, Integer taskUserId, String description, String deadline, int done) {
    }

    private List<TaskItem> taskItems = new ArrayList<>();
    private final TaskListDao dao;

    @Autowired
    HomeController(TaskListDao dao) {
        this.dao = dao;
    }

    @GetMapping("/list")

// int status ではなく String status で受け取る
    String listItems(Model model, @RequestParam(value = "status", defaultValue = "all") String status) {
        List<TaskItem> taskItems;

        // 文字列の比較で分岐させる
        if (status.equals("all")) {
            taskItems = dao.findAll();
        } else if (status.equals("working")) {
            // "working" の時は 状態「1」のものを探す
            taskItems = dao.findByStatus(1);
        } else {
            // それ以外（数値の文字列 "0" や "3" など）は数値に変換して検索
            taskItems = dao.findByStatus(Integer.parseInt(status));
        }

        model.addAttribute("taskList", taskItems);
        model.addAttribute("userList", dao.findAllUsers());
        // HTML側に現在の選択状態（文字列）を渡す
        model.addAttribute("selectedStatus", status);

        return "home";
    }

    @GetMapping("/add")
    String addItem(@RequestParam("task") String task,
                   @RequestParam(value="taskUserId",required = false) Integer taskUserId,
                   @RequestParam("description") String description,
                   @RequestParam("deadline") String deadline) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        TaskItem item = new TaskItem(id, task, taskUserId, description, deadline, 0);
        
        dao.add(item);
        return "redirect:/list";
    }

    @GetMapping("/delete")
    String deleteItem(@RequestParam("id") String id) {
        dao.delete(id);
        return "redirect:/list";
    }

    @GetMapping("/update")
    String updateItem(@RequestParam("id") String id,
                      @RequestParam("task") String task,
                      @RequestParam(value="taskUserId",required=false) Integer taskUserId,
                      @RequestParam("description") String description,
                      @RequestParam("deadline") String deadline,
                      @RequestParam("done") int done) {
        TaskItem taskItem = new TaskItem(id, task, taskUserId, description, deadline, done);
        dao.update(taskItem);
        return "redirect:/list";
    }
}