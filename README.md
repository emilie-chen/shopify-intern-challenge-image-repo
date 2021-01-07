For the database, create a table to store images:
```sql
CREATE TABLE images (
    id varchar(1023),
    name varchar(1023),
    owner varchar(1023),
    accessible_to LONGTEXT,
    image LONGBLOB
);
```

Create another table to store users:
```sql
CREATE TABLE users (
    id varchar(1023),
    username varchar(1023)
);
```
