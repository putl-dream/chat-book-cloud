package fun.amireux.chat.book.auth.security.oauth;

import fun.amireux.chat.book.auth.service.command.OAuthLoginCommand;

import java.util.Map;

public interface OAuthUserResolver {

    String support();

    OAuthLoginCommand resolve(Map<String, Object> attributes);
}
