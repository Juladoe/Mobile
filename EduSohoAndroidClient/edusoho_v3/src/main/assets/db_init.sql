create table if not exists data_cache (
    type varchar(64),
    key varchar(255),
    value text
);

create table if not exists school(
    id integer,
    domain varchar(100),
    title varchar(100),
    version varchar(10),
    mobileApiVersion  varchar(10),
    mobileApiUrl varchar(110)
);