package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {
    record TaskItem(String id, String task, String deadline, int done) {}

    private final TaskListDao dao;

    @Autowired
    HomeController(TaskListDao dao) {
        this.dao = dao;
    }

    @RequestMapping(value = "/hello")
    String hello(Model model) {
        model.addAttribute("time", LocalDateTime.now());
        return "hello";
    }

    @GetMapping("/list")
    String listItems(Model model, @RequestParam(name = "status", defaultValue = "all") String status) {
        List<TaskItem> taskItems;

        // 1. フィルタリングロジック
        if ("all".equals(status)) {
            taskItems = dao.findAll();
        } else if ("working".equals(status)) {
            taskItems = dao.findByStatusList(List.of(1, 2));
        } else {
            // ここで数値変換。万が一変な文字列が来ても落ちないように try-catch
            try {
                int statusInt = Integer.parseInt(status);
                taskItems = dao.findByStatus(statusInt);
            } catch (NumberFormatException e) {
                taskItems = dao.findAll();
            }
        }

        // 2. 画面に渡すデータをセット
        model.addAttribute("taskList", taskItems);
        // ★ここが超重要！HTMLの th:selected で使う変数を渡します
        model.addAttribute("selectedStatus", status);

        return "home";
    }

    @GetMapping("/add")
    String addItem(@RequestParam("task") String task, @RequestParam("deadline") String deadline) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        TaskItem item = new TaskItem(id, task, deadline, 0);
        dao.add(item);
        return "redirect:/list";
    }

    @GetMapping("/delete")
    String deleteItem(@RequestParam("id") String id) {
        dao.delete(id);
        return "redirect:/list";
    }

    @GetMapping("/update")
    String updateItem(@RequestParam("id") String id, @RequestParam("task") String task,
                      @RequestParam("deadline") String deadline, @RequestParam("done") int done) {
        TaskItem taskItem = new TaskItem(id, task, deadline, done);
        dao.update(taskItem);
        return "redirect:/list";
    }
}