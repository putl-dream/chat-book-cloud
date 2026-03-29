package fun.amireux.chat.book.auth.service.login.impl;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class GoogleLoginStrategy extends AbstractOAuthLoginStrategy {

    public GoogleLoginStrategy(UserService userService) {
        super(userService);
    }

    @Override
    public LoginMethod support() {
        return LoginMethod.GOOGLE;
    }
}
