package fun.amireux.chat.book.auth.security.oauth.impl;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.security.oauth.OAuthResolveException;
import fun.amireux.chat.book.auth.security.oauth.OAuthUserResolver;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GithubOAuthUserResolver implements OAuthUserResolver {

    @Override
    public String support() {
        return "github";
    }

    @Override
    public UserDTO resolve(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (StringUtils.isBlank(email)) {
            throw new OAuthResolveException("no_email", "Github account email is required");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setUsername((String) attributes.get("login"));

        Object avatarUrl = attributes.get("avatar_url");
        if (avatarUrl != null) {
            userDTO.setPhoto(avatarUrl.toString());
        }

        userDTO.setLoginMethod(LoginMethod.GITHUB);
        return userDTO;
    }
}
