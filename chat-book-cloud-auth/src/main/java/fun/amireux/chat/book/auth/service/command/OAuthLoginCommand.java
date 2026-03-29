package fun.amireux.chat.book.auth.service.command;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;

public record OAuthLoginCommand(
        LoginMethod loginMethod,
        String email,
        String username,
        String photo
) implements LoginCommand {
}
