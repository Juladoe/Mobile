create table if not exists im_message (
    id integer PRIMARY KEY AUTOINCREMENT,
    uid varchar(36),
    convNo varchar(64),
    fromId varchar(100),
    fromName varchar(100),
    toId  varchar(100),
    toName varchar(100),
    msg  text,
    msgNo  varchar(64),
    time  integer,
    status integer
);

create table if not exists im_upload_extr (
    id integer PRIMARY KEY AUTOINCREMENT,
    message_uid  varchar(36),
    type varchar(64),
    source varchar(1024)
);

create table if not exists im_role (
    id integer PRIMARY KEY AUTOINCREMENT,
    rid  integer,
    type varchar(64),
    nickname varchar(100),
    avatar varchar(255)
);

create table if not exists im_conv (
    id integer primary key AutoIncrement,
    uid integer,
    convNo varchar(64),
    targetId integer,
    targetName varchar(100),
    avatar varchar(255),
    laterMsg  text,
    type varchar(64),
    unRead integer,
    createdTime  integer,
    updatedTime  integer
);

/**
    status
    normal 0
    no_disturb 1
**/
create table if not exists im_blacklist (
    id integer primary key AutoIncrement,
    convNo varchar(64),
    status integer
);

