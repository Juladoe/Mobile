create table if not exists course_discuss(
    discussId integer primary key AutoIncrement,
    id integer,
    courseId integer,
    fromId integer,
    nickname varchar(100),
    headImgUrl varchar(255),
    content varchar(255),
    belongId integer,
    type varchar,
    delivery integer,
    createdTime integer
);

create table if not exists course_thread(
    id integer ,
    courseId integer,
    lessonId integer,
    userId integer,
    nickname varchar(100),
    headImgUrl varchar(255),
    type varchar(20),
    isStick integer,
    isElite integer,
    postNum integer,
    title varchar(100),
    content varchar(255),
    createdTime varchar(30)
);

create table if not exists course_thread_post(
    id integer primary key AutoIncrement,
    postId integer,
    courseId integer,
    lessonId integer,
    threadId integer,
    userId integer,
    nickname varchar(100),
    headImgUrl varchar(255),
    isElite integer,
    content varchar(255),
    type varchar(100),
    delivery integer,
    createdTime varchar(30)
);