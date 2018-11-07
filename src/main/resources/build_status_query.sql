SELECT
       "username",
       coalesce(received_table.total_received, 0) AS "received",
       coalesce(given_table.total_given, 0) AS "given",
       coalesce(given_table.total_given, 0) - coalesce(received_table.total_received,  0) AS "total"
FROM "user"
       FULL OUTER JOIN (SELECT "from", SUM(value) AS "total_received" FROM "transaction" AS total_received WHERE "to"=$1 GROUP BY "from") AS received_table ON received_table.from = "user".username
       FULL OUTER JOIN (SELECT "to", SUM(value) AS "total_given" FROM "transaction" WHERE "from"=$1 GROUP BY "to") AS given_table ON given_table.to = "user".username
WHERE "username" <> $1
