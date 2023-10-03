package com.lantu.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lantu.entity.User;
import com.lantu.mapper.UserMapper;
import com.lantu.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author laocai
 * @since 2023-09-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> login(User user) {
        //根据用户名和密码查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,user.getUsername());
        User loginUser = this.baseMapper.selectOne(wrapper);
        if(loginUser != null && passwordEncoder.matches(user.getPassword(),loginUser.getPassword()))
        {
            //暂时用UUID，终极方案是jwt
            String key = "user:" + UUID.randomUUID();

            //存入redis
            loginUser.setPassword(null);
            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);

            //返回数据
            Map<String,Object> data = new HashMap<>();
            data.put("token",key);
            return data;
        }
        return null;
    }

    @Override
    public Map<String, Object> getUserInfo(String token) {
        //根据token从redis中获取用户信息
        Object obj = redisTemplate.opsForValue().get(token);

        //反序列化，因为存入的时候存在序列化操作
        /***
         * 之前放入的时候序列化操作
         * Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
         *         redisTemplate.setValueSerializer(serializer);
         *         //setValueSerializer是针对值进行序列化处理
         *         //如果简单的处理成这样就可以，但是如果是包含了复杂的类型，比如集合、集合中又包含了对象，此时会出现问题无法反序列化
         *
         *         ObjectMapper om = new ObjectMapper();
         *         //ObjectMapper针对序列进行对象映射，对序列时序进行设置，方便反序列化
         *         om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
         *         om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
         *         om.setTimeZone(TimeZone.getDefault());
         *         om.configure(MapperFeature.USE_ANNOTATIONS, false);
         *         om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         *         om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
         *         om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance ,ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
         *         om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
         *         serializer.setObjectMapper(om);
         *
         */
        if(obj != null){
            //1.将对象转成JSON字符串 2.外面套JSON.parseObject反序列化成User对象
            User loginUser = JSON.parseObject(JSON.toJSONString(obj),User.class);
            //这里需要进行反序列化操作

            /***
             * x_user_role中被插入了数据
             *  id  user_id  role_id
             *  1      1        1
             *  2      1        2
             * 1号用户为1号角色，然后查看x_role中角色表对应信息
             *  role_id  role_name   role_desc
             *     1      admin      超级管理员
             *     2       hr        人事管理员
             *     3     normal      普通员工
             * 这里角色名称需要进行关联查询，目前能单表查的还是尽量单表查
             */
            Map<String, Object> data = new HashMap<>();
            data.put("name",loginUser.getUsername());
            data.put("avatar",loginUser.getAvatar());

            //角色
            List<String> roleList = this.baseMapper.getRoleNameByUserId(loginUser.getId());
            data.put("roles",roleList);

            return data;
        }
        return null;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(token);
    }
}
