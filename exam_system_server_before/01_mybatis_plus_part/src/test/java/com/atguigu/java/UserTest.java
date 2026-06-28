package com.atguigu.java;

import com.atguigu.java.entity.User;
import com.atguigu.java.mapper.UserMapper;
import com.atguigu.java.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * Mybatis-plus分页插件使用
     *   准备工作：
     *     1.导入对应依赖没注意mybatis-plus 3.5.9+版本
     *     <dependency>
     *       <groupId>com.baomidou</groupId>
     *       <artifactId>mybatis-plus-extension</artifactId>
     *       <version>你的版本号（如 3.5.9 或更高）</version>
     *     </dependency>
     *     2.将分页插件加入到ioc容器
     *          @Bean
     *          public MybatisPlusInterceptor mybatisPlusInterceptor() {
     *            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
     *            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
     *            return interceptor;
     *          }
     *       3.使用mybatis-plus提供的mapper或者service里的select方法
     *       // 无条件分页查询
     *       IPage<T> page(IPage<T> page);
     *       // 条件分页查询
     *       IPage<T> page(IPage<T> page, Wrapper<T> queryWrapper);
     *       // 无条件分页查询
     *       IPage<Map<String, Object>> pageMaps(IPage<T> page);
     *       // 条件分页查询
     *       IPage<Map<String, Object>> pageMaps(IPage<T> page, Wrapper<T> queryWrapper);
     *       4.我们自定义的mapper方法想使用Mybatisplus提供的分页插件应该怎么做
     *       1.如何自定义方法
     *       todo:正常定义mapper方法接口即可 List<User> queryUserByAge(int age);
     *       正常定义mapper.xml如果这个文件在resourse包下则无需声明配置（否则声明 mapper-location: classpath:/mappers/*.xml）
     *       2.自定义分页插件的使用
     *       任何mapper方法想要使用mybatis-plus提供的分页，你的方法必须结构一定是固定的
     *       返回值必须是IPage第一个参数必须是Page
     *       注意；自定义的方法要使用分页差价，自己在sql的时候千万别添加：结尾
     *
     */
    @Test
    public  void testPage(){
        //todo:自定义mapper方法
        Page<User> page = new Page<>(2,4);
        IPage<User> list = userMapper.queryUserByAge(page,10);
        List<User> records1 = list.getRecords();
        System.out.println(records1);

//        LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.gt(User::getId,1);
//        //Page是IPage接口的具体实现
//        Page<User> page=new Page<>(2,3);
//        Page<User> page1 = userService.page(page, lambdaQueryWrapper);
//
//        //当前页的数据集合
//        List<User> records = page1.getRecords();
//        System.out.println(records);
//        //满足当前条件的总数量
//        long total = page1.getTotal();
//        System.out.println("count="+total);

    }
    /**
     * 配置mybatisplus的逻辑删除值deleted
     * 非真实删除，修改数据状态，其实就是更新数据
     * mybatisplus配置逻辑删除
     *   局部：单独的实体类中
     *       1.找到逻辑删除字段对应的属性
     *       2.添加@TableLogic注解即可默认0是未删除
     *   全局：
     * 3.使用和测试
     * 正常使用即可
     * 自动删除变修改
     * 自动添加逻辑删除条件 where deleted=0
     * 4.超级注意事项
     *   一旦配置逻辑删除字段
     *   mybatis-plus 提供的方法都自动添加逻辑删除的处理
     *   一旦我们后期自己定义mapper.xmlxia写的sql语句，都要自己添加逻辑删除判断where deleted =0
     */
    @Test
    public void logiaDelete() {
        boolean remove = userService.removeById(6);
        System.out.println("remove "+remove);

        List<User> list = userService.list();
        System.out.println(list);
    }

    @Test
    public void testSelectList(){
        List<User> users = userMapper.selectList(null);
        System.out.println(users);
    }

    /**
     * 条件构造器只能用于mybatisplus提供的单表crud(Mapper,service)
     * 删除查询修改都可以使用条件构造器
     *    条件构造器的结构
     *       AbstractWrapper
     *          QueryWrapper-->条件（删除，查询，修改）
     *          UpdateWrapper-->更高级的条件（删除查询修改）还可以装修改列的和值
     *       拼接条件的语法
     *          1.创建wrapper对象，new QueryWrapper |UpdateWrapper
     *          2.wrapper.(String 列名,Object 列的值)  方法是固定的，方法的含义是比较符号 > gt
     *          3.多个条件自动使用and一直.就行了
     *        符号对应的方法
     *          eq name=laoba
     *          ne <>不等于
     *          gt > ge>=
     *          lt < le<=
     *          like 模糊查询%e%
     *          likeLeft %e  以e结尾
     *          likeRight e%  以e开头
     *          in in(1,2,3)
     *          isNull 列名 is Null
     *          or ()  或者
     *
     *          上述的`QueryWrapper`和`UpdateWrapper`均有一个`Lambda`版本，
     *          也就是`LambdaQueryWrapper`和`LambdaUpdateWrapper`，
     *          `Lambda`版本的优势在于，可以省去字段名的硬编码，具体案例如下：
     *
     *
     */
    @Test
    public  void testQuaryWrapper(){
        //需求一使用业务层提供的方法进行条件查询(list(wrapper))
        //查看name中包含e且年龄大于15的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //列名+数值
        queryWrapper.like("name","e").gt("age",15);
        List<User> list = userService.list(queryWrapper);
        System.out.println(list);


    }

    /*
    测试业务层的扩展CRUD
    一共七个方向  save saveOrUpdate remove update get list count
     */
    @Test
    public void tsetServiceCrud(){

        User user = new User();
        user.setName("蔡徐坤");
        user.setAge(20);
        user.setEmail("cxk9191.520@qq.com");
        userService.save(user);


        User user1 = userService.getById(6);
        user1.setName("鸽鸽");
        userService.saveOrUpdate(user1);

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.gt("age",15);
        long count = userService.count(wrapper);
        System.out.println("count"+count);
    }





    @Test
    public void testInsert() {
        User user = new User();
        user.setName("kobe");
        user.setAge(15);
        user.setEmail("man@kobe.com");
        userMapper.insert(user);
    }

    @Test
    public void selceById(){
        User user = userMapper.selectById(1);
        System.out.println(user);
    }
    @Test
    public void updateById(){
        User user = userMapper.selectById(1);
        user.setAge(110);
        userMapper.updateById(user);
        System.out.println(user);
    }

    @Test
    public void deleteById(){
        userMapper.deleteById(1);
       this.testSelectList();
    }


}
