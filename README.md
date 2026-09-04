# Kopidlno Importer

Downloads address data for the municipality of Kopidlno from the [RUIAN registry](https://www.smartform.cz/download/kopidlno.xml.zip), parses the XML, and stores the results in a PostgreSQL database.

## What it does

1. Downloads a ZIP file from the RUIAN registry
2. Parses the XML using StAX (streaming, no full document loaded into memory)
3. Saves one `obec` and its `části obce` into a SQL database

## Tech stack

- Java 21, Spring Boot 3.3
- Spring Data JPA + PostgreSQL
- StAX (`XMLStreamReader`) for XML parsing
- Docker + Docker Compose

## Running

```bash
docker compose up
```

Starts PostgreSQL, creates the schema, runs the import, and exits. No other setup needed.

## Database schema

```sql
CREATE TABLE obec (
    kod   BIGINT       PRIMARY KEY,
    nazev VARCHAR(255) NOT NULL
);

CREATE TABLE cast_obce (
    kod      BIGINT       PRIMARY KEY,
    nazev    VARCHAR(255) NOT NULL,
    obec_kod BIGINT       NOT NULL,
    CONSTRAINT fk_cast_obce_obec FOREIGN KEY (obec_kod) REFERENCES obec (kod)
);
```

## Configuration

All settings can be overridden via environment variables:

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/kopidlno` | JDBC connection URL |
| `DB_USERNAME` | `kopidlno` | Database user |
| `DB_PASSWORD` | `kopidlno` | Database password |
| `SOURCE_URL` | RUIAN ZIP URL | Source data URL |
