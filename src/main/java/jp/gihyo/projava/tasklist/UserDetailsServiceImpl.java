package jp.gihyo.projava.tasklist;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Map;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final TaskListDao dao;

    public UserDetailsServiceImpl(TaskListDao dao) {
        this.dao = dao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Map<String, Object> user = dao.findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + email);
        }

        return new User(
                (String) user.get("email"),
                (String) user.get("password"),
                Collections.emptyList()
        );
    }
}