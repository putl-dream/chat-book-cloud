package fun.amireux.chat.book.auth.service.command;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;

public record CaptchaLoginCommand(
        String email,
        String captcha
) implements LoginCommand {

    @Override
    public LoginMethod loginMethod() {
        return LoginMethod.VERIFICATION_CODE;
    }
}
