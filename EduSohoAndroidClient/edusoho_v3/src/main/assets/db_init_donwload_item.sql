create table if not exists download_item (
    id integer primary key AutoIncrement,
    reference integer,
    targetId integer,
    type varchar(64),
    url varchar(2048)
);