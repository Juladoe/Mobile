create table if not exists data_m3u8 (
    id integer primary key AutoIncrement,
    finish integer,
    host varchar(255),
    lessonId integer,
    userId integer,
    total_num integer,
    download_num integer,
    play_list text
);

create table if not exists data_m3u8_url (
    id integer primary key AutoIncrement,
    lessonId integer,
    finish integer,
    url varchar(2048)
);