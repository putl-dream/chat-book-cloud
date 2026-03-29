package fun.amireux.chat.book.auth.service.command;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;

public record PasswordLoginCommand(
        String username,
        String email,
        String password
) implements LoginCommand {

    @Override
    public LoginMethod loginMethod() {
        return LoginMethod.PASSWORD;
    }
}
