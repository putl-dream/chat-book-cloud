package fun.amireux.chat.book.auth.service.login;

import fun.amireux.chat.book.auth.projectobject.UserInfoDO;

public record AuthenticatedUser(
        Integer userId,
        UserInfoDO userInfo
) {
}
