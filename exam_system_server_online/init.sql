-- 考试系统数据库初始化脚本
-- 数据库: exam_system_online

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(100) NOT NULL                COMMENT '用户名',
    password      VARCHAR(255) NOT NULL                COMMENT '密码',
    real_name     VARCHAR(100)                         COMMENT '真实姓名',
    role          VARCHAR(50)  DEFAULT 'STUDENT'       COMMENT '角色: ADMIN, TEACHER, STUDENT',
    status        VARCHAR(50)  DEFAULT 'ACTIVE'        COMMENT '状态: ACTIVE, INACTIVE',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted    TINYINT(3)   DEFAULT 0               COMMENT '逻辑删除(0-正常,1-删除)',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS banners (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title         VARCHAR(255)                         COMMENT '标题',
    description   TEXT                                 COMMENT '描述',
    image_url     VARCHAR(500)                         COMMENT '图片URL',
    link_url      VARCHAR(500)                         COMMENT '跳转链接',
    sort_order    INT          DEFAULT 0               COMMENT '排序',
    is_active     TINYINT(1)   DEFAULT 1               COMMENT '是否启用',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS categories (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name          VARCHAR(100) NOT NULL                COMMENT '分类名称',
    parent_id     BIGINT       DEFAULT 0               COMMENT '父分类ID',
    sort          INT          DEFAULT 0               COMMENT '排序',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS exams (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name            VARCHAR(255)                         COMMENT '考试名称',
    description     TEXT                                 COMMENT '描述',
    duration        INT                                  COMMENT '时长(分钟)',
    pass_score      INT                                  COMMENT '及格分数',
    total_score     INT                                  COMMENT '总分',
    question_count  INT          DEFAULT 0               COMMENT '题目数量',
    status          VARCHAR(50)  DEFAULT 'DRAFT'         COMMENT '状态: DRAFT, PUBLISHED, CLOSED',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS exam_records (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    exam_id          INT                                  COMMENT '考试ID',
    student_name     VARCHAR(100)                         COMMENT '考生姓名',
    score            INT          DEFAULT 0               COMMENT '得分',
    answers          TEXT                                 COMMENT '答题记录(JSON)',
    start_time       DATETIME                             COMMENT '开始时间',
    end_time         DATETIME                             COMMENT '结束时间',
    status           VARCHAR(50)  DEFAULT '进行中'        COMMENT '状态: 进行中, 已完成, 已批阅',
    window_switches  INT          DEFAULT 0               COMMENT '切屏次数',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted       TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS notices (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title         VARCHAR(255)                         COMMENT '标题',
    content       TEXT                                 COMMENT '内容',
    type          VARCHAR(50)  DEFAULT 'NOTICE'        COMMENT '类型',
    priority      INT          DEFAULT 0               COMMENT '优先级',
    is_active     TINYINT(1)   DEFAULT 1               COMMENT '是否启用',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS paper (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name            VARCHAR(255)                         COMMENT '试卷名称',
    description     TEXT                                 COMMENT '描述',
    status          VARCHAR(50)  DEFAULT 'DRAFT'         COMMENT '状态: DRAFT, PUBLISHED, STOPPED',
    total_score     DECIMAL(10,2) DEFAULT 0.00           COMMENT '总分',
    question_count  INT          DEFAULT 0               COMMENT '题目数量',
    duration        INT                                  COMMENT '时长(分钟)',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS paper_question (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    paper_id      INT                                   COMMENT '试卷ID',
    question_id   BIGINT                                COMMENT '题目ID',
    score         DECIMAL(10,2) DEFAULT 0.00            COMMENT '题目分数',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(3)    DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_paper_id (paper_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS questions (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title         VARCHAR(500)                         COMMENT '题目标题',
    type          VARCHAR(50)  DEFAULT 'CHOICE'        COMMENT '类型: CHOICE, JUDGE, TEXT',
    multi         TINYINT(1)   DEFAULT 0               COMMENT '是否多选',
    category_id   BIGINT                               COMMENT '分类ID',
    difficulty    VARCHAR(50)  DEFAULT 'MEDIUM'        COMMENT '难度: EASY, MEDIUM, HARD',
    score         INT          DEFAULT 0               COMMENT '默认分值',
    analysis      TEXT                                 COMMENT '题目解析',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_type (type),
    INDEX idx_category_id (category_id),
    INDEX idx_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS question_answers (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    question_id   BIGINT                               COMMENT '题目ID',
    answer        TEXT                                 COMMENT '标准答案',
    keywords      VARCHAR(500)                         COMMENT '评分关键词',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS question_choices (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    question_id   BIGINT                               COMMENT '题目ID',
    content       VARCHAR(500)                         COMMENT '选项内容',
    is_correct    TINYINT(1)   DEFAULT 0               COMMENT '是否正确答案',
    sort          INT          DEFAULT 0               COMMENT '排序',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS answer_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    exam_record_id  INT                                  COMMENT '考试记录ID',
    question_id     INT                                  COMMENT '题目ID',
    user_answer     TEXT                                 COMMENT '用户答案',
    score           INT          DEFAULT 0               COMMENT '得分',
    is_correct      TINYINT(1)   DEFAULT 0               COMMENT '是否正确',
    ai_correction   TEXT                                 COMMENT 'AI批改意见',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(3)   DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS videos (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '视频ID',
    title           VARCHAR(255)                         COMMENT '标题',
    description     TEXT                                 COMMENT '描述',
    category_id     BIGINT                               COMMENT '分类ID',
    file_url        VARCHAR(500)                         COMMENT '文件URL',
    cover_url       VARCHAR(500)                         COMMENT '封面URL',
    duration        INT          DEFAULT 0               COMMENT '时长(秒)',
    file_size       BIGINT       DEFAULT 0               COMMENT '文件大小',
    uploader_name   VARCHAR(100)                         COMMENT '上传者名称',
    uploader_type   TINYINT(1)   DEFAULT 0               COMMENT '上传者类型',
    user_id         BIGINT                               COMMENT '用户ID',
    admin_id        BIGINT                               COMMENT '管理员ID',
    status          TINYINT(1)   DEFAULT 0               COMMENT '状态: 0-待审核,1-已发布,2-已拒绝,3-已下架',
    audit_admin_id  BIGINT                               COMMENT '审核管理员ID',
    audit_time      DATETIME                             COMMENT '审核时间',
    audit_reason    TEXT                                 COMMENT '审核原因',
    view_count      BIGINT       DEFAULT 0               COMMENT '观看次数',
    like_count      BIGINT       DEFAULT 0               COMMENT '点赞次数',
    tags            VARCHAR(255)                         COMMENT '标签',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS video_categories (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name          VARCHAR(100) NOT NULL                COMMENT '名称',
    description   TEXT                                 COMMENT '描述',
    parent_id     BIGINT       DEFAULT 0               COMMENT '父级ID',
    sort_order    INT          DEFAULT 0               COMMENT '排序',
    status        TINYINT(1)   DEFAULT 1               COMMENT '状态: 1-启用,0-禁用',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS video_likes (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    video_id      BIGINT                               COMMENT '视频ID',
    user_ip       VARCHAR(50)                          COMMENT '用户IP',
    user_agent    VARCHAR(500)                         COMMENT '用户代理',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id),
    INDEX idx_video_id (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS video_views (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '观看ID',
    video_id        BIGINT                               COMMENT '视频ID',
    user_ip         VARCHAR(50)                          COMMENT '用户IP',
    user_agent      VARCHAR(500)                         COMMENT '用户代理',
    view_duration   INT          DEFAULT 0               COMMENT '观看时长(秒)',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '观看时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
