package fun.amireux.chat.book.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfoDO> {
}
