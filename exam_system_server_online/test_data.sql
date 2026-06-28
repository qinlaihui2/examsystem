-- 测试数据脚本
USE exam_system_online;

-- ==================== 分类数据 ====================
INSERT INTO categories (id, name, parent_id, sort) VALUES
(1, 'Java基础', 0, 1),
(2, 'Spring框架', 0, 2),
(3, '数据库', 0, 3),
(4, '面向对象', 1, 1),
(5, '集合框架', 1, 2),
(6, 'Spring Boot', 2, 1),
(7, 'Spring Cloud', 2, 2),
(8, 'MySQL', 3, 1),
(9, 'Redis', 3, 2);

-- ==================== 题目数据 ====================
INSERT INTO questions (id, title, type, multi, category_id, difficulty, score, analysis) VALUES
(1, 'Java中，以下哪个关键字用于实现接口？', 'CHOICE', 0, 4, 'EASY', 5, 'implements关键字用于类实现接口，extends用于继承类。'),
(2, '下列哪些是Java的访问修饰符？（多选）', 'CHOICE', 1, 4, 'EASY', 5, 'Java有四种访问修饰符：public、protected、default、private。'),
(3, 'ArrayList和LinkedList中，哪个查询效率更高？', 'CHOICE', 0, 5, 'MEDIUM', 10, 'ArrayList底层是数组，支持O(1)随机访问；LinkedList底层是链表，查询需要遍历。'),
(4, 'Spring Boot的自动配置原理是什么？', 'TEXT', 0, 6, 'MEDIUM', 15, '通过@EnableAutoConfiguration注解，利用SpringFactoriesLoader加载META-INF/spring.factories中的自动配置类。'),
(5, 'MySQL中，InnoDB存储引擎的默认事务隔离级别是？', 'CHOICE', 0, 8, 'EASY', 5, 'InnoDB默认隔离级别是REPEATABLE READ（可重复读）。'),
(6, 'Redis是一种关系型数据库。', 'JUDGE', 0, 9, 'EASY', 5, 'Redis是非关系型的内存键值数据库。'),
(7, 'Spring中@Autowired和@Resource有什么区别？', 'TEXT', 0, 6, 'HARD', 20, '@Autowired是Spring提供的，默认按类型注入；@Resource是JSR-250标准，默认按名称注入。'),
(8, '以下哪些是MySQL中常见的索引类型？（多选）', 'CHOICE', 1, 8, 'MEDIUM', 10, 'MySQL常见索引类型包括B+Tree、Hash、Full-Text。Bitmap不是MySQL原生支持的。'),
(9, 'Java中的HashMap是线程安全的。', 'JUDGE', 0, 5, 'EASY', 5, 'HashMap不是线程安全的，ConcurrentHashMap是线程安全的。'),
(10, '什么是Spring的IoC容器？请简述其作用。', 'TEXT', 0, 6, 'MEDIUM', 15, 'IoC（控制反转）容器负责对象的创建、配置和生命周期管理，通过依赖注入实现对象之间的松耦合。');

-- ==================== 答案数据 ====================
INSERT INTO question_answers (question_id, answer, keywords) VALUES
(1, 'B', NULL),
(2, 'A,B,C,D', NULL),
(3, 'A', NULL),
(4, 'Spring Boot通过@EnableAutoConfiguration注解开启自动配置，利用@Conditional条件注解按需加载配置类', '自动配置,@EnableAutoConfiguration,条件注解'),
(5, 'C', NULL),
(6, '错误', NULL),
(7, '@Autowired按类型注入，@Resource按名称注入，@Autowired配合@Qualifier可按名称注入', '按类型,按名称,@Autowired,@Resource'),
(8, 'A,B,C', NULL),
(9, '错误', NULL),
(10, 'IoC容器是Spring的核心，负责管理Bean的生命周期和依赖关系，通过控制反转实现解耦', 'IoC,控制反转,Bean,依赖注入');

-- ==================== 选择题选项数据 ====================
INSERT INTO question_choices (question_id, content, is_correct, sort) VALUES
-- 第1题: Java实现接口关键字
(1, 'A. extends', 0, 1),
(1, 'B. implements', 1, 2),
(1, 'C. abstract', 0, 3),
(1, 'D. interface', 0, 4),
-- 第2题: Java访问修饰符（多选）
(2, 'A. public', 1, 1),
(2, 'B. protected', 1, 2),
(2, 'C. default', 1, 3),
(2, 'D. private', 1, 4),
(2, 'E. static', 0, 5),
-- 第3题: ArrayList vs LinkedList
(3, 'A. ArrayList', 1, 1),
(3, 'B. LinkedList', 0, 2),
(3, 'C. 两者效率相同', 0, 3),
(3, 'D. 取决于数据量', 0, 4),
-- 第5题: MySQL隔离级别
(5, 'A. READ UNCOMMITTED', 0, 1),
(5, 'B. READ COMMITTED', 0, 2),
(5, 'C. REPEATABLE READ', 1, 3),
(5, 'D. SERIALIZABLE', 0, 4),
-- 第6题: 判断题（无选项）
-- 第8题: MySQL索引类型（多选）
(8, 'A. B+Tree索引', 1, 1),
(8, 'B. Hash索引', 1, 2),
(8, 'C. Full-Text索引', 1, 3),
(8, 'D. Bitmap索引', 0, 4),
-- 第9题: 判断题（无选项）

-- ==================== 试卷数据 ====================
INSERT INTO paper (id, name, description, status, total_score, question_count, duration) VALUES
(1, 'Java基础测试卷', '涵盖Java基础语法、面向对象、集合框架等知识点', 'PUBLISHED', 100.00, 10, 60),
(2, 'Java入门小测', '面向初学者的Java基础测试', 'PUBLISHED', 50.00, 5, 30);

-- ==================== 试卷-题目关联 ====================
INSERT INTO paper_question (paper_id, question_id, score) VALUES
(1, 1, 5.00),
(1, 2, 5.00),
(1, 3, 10.00),
(1, 4, 15.00),
(1, 5, 5.00),
(1, 6, 5.00),
(1, 7, 20.00),
(1, 8, 10.00),
(1, 9, 5.00),
(1, 10, 20.00),
(2, 1, 10.00),
(2, 2, 10.00),
(2, 5, 10.00),
(2, 6, 10.00),
(2, 9, 10.00);

-- ==================== 公告数据 ====================
INSERT INTO notices (id, title, content, type, priority, is_active) VALUES
(1, '系统上线通知', '在线考试系统已正式上线，欢迎使用！', 'SYSTEM', 2, 1),
(2, '新功能：AI智能批改', '现已支持AI自动批改简答题，请老师们试用反馈。', 'FEATURE', 1, 1),
(3, '系统维护通知', '本周六凌晨2:00-4:00系统例行维护。', 'NOTICE', 0, 1);

-- ==================== 轮播图数据 ====================
INSERT INTO banners (title, image_url, link_url, sort_order, is_active) VALUES
('欢迎使用在线考试系统', '/banners/banner1.png', '/exam', 1, 1),
('AI智能批改上线', '/banners/banner2.png', '/about', 2, 1);
