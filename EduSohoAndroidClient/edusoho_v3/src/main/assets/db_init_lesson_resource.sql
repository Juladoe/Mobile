create table if not exists lesson_resource (
    id integer primary key AutoIncrement,
    finish integer,
    materialId integer,
    host varchar(255),
    lessonId integer,
    userId integer,
    total integer,
    download integer
);
