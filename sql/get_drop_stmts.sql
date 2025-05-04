select 'drop table "' || tablename || '" cascade;' from pg_tables where schemaname = 'public';

