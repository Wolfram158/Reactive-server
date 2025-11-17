create table if not exists counter (
    id bigserial primary key,
    name varchar(64) not null unique,
    value bigint not null default 0
);

create index if not exists idx_counter_name on counter(name);