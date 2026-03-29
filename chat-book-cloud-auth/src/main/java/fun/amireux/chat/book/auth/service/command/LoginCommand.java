package fun.amireux.chat.book.auth.service.command;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;

public sealed interface LoginCommand permits PasswordLoginCommand, CaptchaLoginCommand, RegisterCommand, OAuthLoginCommand {

    LoginMethod loginMethod();
}
