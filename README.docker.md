# Docker environments

Three isolated stacks for the `webservices.rest` module. Each builds the
module from source (multi-stage `Dockerfile`) and runs it on the
`openmrs/openmrs-core` image against its **own** MariaDB — separate database
names, users, passwords, volumes and ports, so test never touches prod (or dev)
data.

| Environment | Compose file               | Env file    | Web URL                | DB host port | DB name        |
|-------------|----------------------------|-------------|------------------------|--------------|----------------|
| Dev (default) | `docker-compose.yml`     | `.env.dev`  | http://localhost:8080/openmrs | 3307 | `openmrs_dev`  |
| Test        | `docker-compose.test.yml`  | `.env.test` | http://localhost:8081/openmrs | 3308 | `openmrs_test` |
| Prod        | `docker-compose.prod.yml`  | `.env.prod` | http://localhost:80/openmrs   | (not published) | `openmrs_prod` |

Isolation comes from distinct compose project names + distinct named volumes
(`db-data`, `test-db-data`, `prod-db-data`, …), so the three databases can run
side by side without collisions.

## Usage

Dev (uses the default compose file and `.env.dev`):

```bash
docker compose up -d --build
```

Test:

```bash
docker compose -f docker-compose.test.yml --env-file .env.test up -d --build
# wipe data for a clean run:
docker compose -f docker-compose.test.yml --env-file .env.test down -v
```

Prod (set strong secrets in `.env.prod` first):

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

Stop a stack (keep data): `... down`  •  Stop + delete data: `... down -v`.

## Notes

- Default admin login: `admin` / value of `OMRS_ADMIN_USER_PASSWORD`.
- `OPENMRS_CORE_TAG` selects the core base image; pick a tag matching
  `openmrs.version` in `pom.xml` (currently 2.8.7) when a matching tag exists,
  else `nightly`.
- Prod sets `OMRS_DB_AUTO_UPDATE=false` and `OMRS_CREATE_TABLES=false` so a
  real schema is never silently migrated or recreated.
- `.env.dev` / `.env.test` / `.env.prod` are git-ignored. `.env.example`
  documents every variable.
- First boot runs Liquibase to build the schema — the app can take a few
  minutes before the web UI responds.
