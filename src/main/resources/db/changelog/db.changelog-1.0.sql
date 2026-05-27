create table public.redis_offset(
    id varchar primary key,
    offset_value bigint
);

insert into public.redis_offset values('redisCahce1', 0);
