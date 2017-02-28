create table if not exists audio_cache (
    id integer primary key AutoIncrement,
    localPath varchar(255),
    onlinePath varchar(255)
);