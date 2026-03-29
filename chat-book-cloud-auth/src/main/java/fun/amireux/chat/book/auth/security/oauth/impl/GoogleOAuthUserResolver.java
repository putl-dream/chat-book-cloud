package fun.amireux.chat.book.auth.security.oauth.impl;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.security.oauth.OAuthResolveException;
import fun.amireux.chat.book.auth.security.oauth.OAuthUserResolver;
import fun.amireux.chat.book.auth.service.command.OAuthLoginCommand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleOAuthUserResolver implements OAuthUserResolver {

    @Override
    public String support() {
        return "google";
    }

    @Override
    public OAuthLoginCommand resolve(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (StringUtils.isBlank(email)) {
            throw new OAuthResolveException("no_email", "Google account email is required");
        }

        return new OAuthLoginCommand(
                LoginMethod.GOOGLE,
                email,
                (String) attributes.get("name"),
                (String) attributes.get("picture")
        );
    }
}
