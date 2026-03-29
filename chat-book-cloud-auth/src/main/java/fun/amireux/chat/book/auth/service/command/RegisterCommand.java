package fun.amireux.chat.book.auth.service.command;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;

public record RegisterCommand(
        String email,
        String username,
        String password,
        String captcha
) implements LoginCommand {

    @Override
    public LoginMethod loginMethod() {
        return LoginMethod.REGISTER;
    }
}
