package com.atguigu.java.service.impl;

import com.atguigu.java.entity.User;
import com.atguigu.java.mapper.UserMapper;
import com.atguigu.java.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * UserServiceImpl
 *
 * @author 李嘉宇
 * @date 2025/10/28 19:09
 * @version 1.0
 * 演示mybaits-plus对service层进行单表的CRUD方法的增强
 * 扩展方式：
 *    步骤1 service接口extends IService<User>
 *    步骤2 service实现类extends ServiceImpl<UserMapper,User>
 *    步骤3 service实现类实现Imlpement UserService
 *
 *    总结业务层扩展的好处与优势
 *    1.提供单表crud相关的业方法
 *    2.对于mapper层，他的方法更接近业务能更好用，比如sacvOrUpdate(T t) saceBatch()
 *    3.业务扩展逻辑，业务层可以快速获取mapper对象
 *service扩展提供的业务方法提供了哪些
 *
 *    // 插入一条记录（选择字段，策略插入）
 *    boolean save(T entity);
 *    // 插入（批量）
 *    boolean saveBatch(Collection<T> entityList);
 *    // 插入（批量）
 *    boolean saveBatch(Collection<T> entityList, int batchSize);
 *    * @description
 *
 *    // TableId 注解属性值存在则更新记录，否插入一条记录
 *    boolean saveOrUpdate(T entity);
 *    // 批量修改插入
 *    boolean saveOrUpdateBatch(Collection<T> entityList);
 *    / / 批量修改插入
 *    boolean saveOrUpdateBatch(Collection<T> entityList, int batchSize);
 *
 *    // 根据 queryWrapper 设置的条件，删除记录
 *    boolean remove(Wrapper<T> queryWrapper);
 *    // 根据 ID 删除
 *    boolean removeById(Serializable id);
 *    // 根据 columnMap 条件，删除记录
 *    boolean removeByMap(Map<String, Object> columnMap);
 *    // 删除（根据ID 批量删除）
 *    boolean removeByIds(Collection<? extends Serializable> idList);
 *
 *    // 根据 ID 查询
 *     T getById(Serializable id);
 *     // 根据 Wrapper，查询一条记录。结果集，如果是多个会抛出异常，随机取一条加上限制条件 wrapper.last("LIMIT 1")
 *     T getOne(Wrapper<T> queryWrapper);
 *     // 根据 Wrapper，查询一条记录
 *     T getOne(Wrapper<T> queryWrapper, boolean throwEx);
 *    // 根据 Wrapper，查询一条记录
 *    Map<String, Object> getMap(Wrapper<T> queryWrapper);
 *    // 根据 Wrapper，查询一条记录
 *    <V> V getObj(Wrapper<T> queryWrapper, Function<? super Object, V> mapper);
 *
 *    // 查询所有
 *      List<T> list();
 *     // 查询列表
 *     List<T> list(Wrapper<T> queryWrapper);
 *      // 查询（根据ID 批量查询）
 *     Collection<T> listByIds(Collection<? extends Serializable> idList);
 *
 *     // 查询总记录数
 *     int count();
 *     // 根据 Wrapper 条件，查询总记录数
 *    int count(Wrapper<T> queryWrapper);
 *
 *    //自3.4.3.2开始,返回值修改为long
 *    // 查询总记录数
 *    long count();
 *    // 根据 Wrapper 条件，查询总记录数
 *    long count(Wrapper<T> queryWrapper);
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

}
