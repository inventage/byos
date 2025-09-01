# Bring Your Own Schema (BYOS)

[Build Pipeline](https://github.com/inventage/byos/actions/workflows/build.yml)

**Bring Your Own Schema (BYOS)** is an implementation based on the concepts presented in [my thesis](https://db.cs.uni-tuebingen.de/theses/2023/lukas-sailer/lukas-sailer-2023.pdf), focusing on transpiling GraphQL queries to SQL.
This repository provides a practical example of how to bring your own GraphQL schema and use it to generate efficient SQL queries.

## Features

- One SQL query for every root level query.
- GraphQL schema and SQL schema are not generated and do not contain BYOS specific code.
- jOOQ is used to ensure correctness of the SQL configuration.
- Supports filter arguments, pagination and sorting.

### limit

The number of results can be restricted by the `limit` argument.

```graphql
query { filmByIds(limit: 3) { edges { node { film_id title } } } }
```

### where

The result can be restricted to a condition by the `where` argument.

```graphql
query { filmByIds(where: {film_id: {_in: [1,2]}}) { edges { node { film_id title } } } }
```

### order

The order of the result can be defined by the `orderBy` argument.

```graphql
query { allFilms(orderBy: [{title: asc}]) { edges { node { film_id title } } } }
```

## Project structure

- `src/main/kotlin/byos` contains the BYOS implementation.
- `src/main/kotlin/example` contains the exemplary Spring Boot application.
- `src/main/resources/graphql` contains the GraphQL schema.
- `src/test/kotlin/example` contains the tests for the example application.

## If you want to try it out...

... you will have to have a local psql version of the [sakila](https://github.com/jOOQ/sakila/tree/main/postgres-sakila-db) database with this small addition:

```SQL
ALTER TABLE category
    ADD COLUMN parent_category_id INTEGER;
```

You could also set up your own database by:

- changing the connection of jOOQ (and the language if you do not want to use Postgres).
- using a GraphQL schema matching the SQL schema.
- defining relationships and how to resolve them.

To run the example run the `main` method in `ByosApplication.kt`.
After that you can query the GraphQL endpoint at `http://localhost:8080` or open the GraphiQL interface at `http://localhost:8080/graphiql`.

---

For more details about the approach and implementation, refer to the [associated thesis](https://db.cs.uni-tuebingen.de/theses/2023/lukas-sailer/lukas-sailer-2023.pdf).

Happy querying and transpiling!
