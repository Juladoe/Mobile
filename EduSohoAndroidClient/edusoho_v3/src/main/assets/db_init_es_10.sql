create table if not exists school_nofity (
    id integer primary key AutoIncrement,
    title varchar(1024),
    createdTime integer,
    content text,
    type varchar(64),
    msgNo varchar(64)
);
