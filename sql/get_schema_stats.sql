with tbl as (SELECT table_schema,table_name FROM information_schema.tables   where table_name not like 'pg_%' and table_schema in ('public')) select table_schema, table_name, (xpath('/row/c/text()', query_to_xml(format('select count(*) as c from %I.%I', table_schema, table_name), false, true, '')))[1]::text::int as rows_n from tbl ORDER BY 3 DESC;


