package jp.gihyo.projava.tasklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class TaskListApplication {

	// アプリケーション起動時に、JVM全体のデフォルトタイムゾーンを日本時間に固定する
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
	}

	public static void main(String[] args) {
		SpringApplication.run(TaskListApplication.class, args);
	}

}