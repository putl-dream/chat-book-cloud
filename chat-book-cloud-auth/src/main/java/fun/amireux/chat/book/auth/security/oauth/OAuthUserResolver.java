package fun.amireux.chat.book.auth.security.oauth;

import fun.amireux.chat.book.auth.service.dto.UserDTO;

import java.util.Map;

public interface OAuthUserResolver {

    String support();

    UserDTO resolve(Map<String, Object> attributes);
}
