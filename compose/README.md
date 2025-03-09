# This is an outdated file.

---

# Docker Compose Files and Folders

This section explains the folders, their purpose, and the different compose files. Each section explains a compose file along with its folder, what it's there for, and what to know.

---

## Production

This is the main compose file and configuration, and generally what you're looking for if you're not actively testing or developing the application.

### Services

The compose file starts the following services:
- Next
- Spring
- Postgres Database
- Keycloak
- Caddy

All services use their regular production configuration and the docker network.

### Docker Compose

The Docker compose file should be moved to the parent folder that contains `/frontend` and `/backend` as it needs to address files in both folders. Everything regarding setup is located in the `README` in the root of the backend.

---

## Development

### General

`/development/` contains data files for containers that require a different configuration than the production containers.

### Services

The compose file starts the following services:
- Next
- Spring
- Postgres Database
- Keycloak (Development)
- Caddy (Development)

### Development Keycloak

The Keycloak development container provides two additional environment variables: *Private Key* and *Certificate*. This allows to start keycloak with a consistent key to sign jwt tokens with, such that they are never invalid due to container recreation. This is useful for permanent tokens for testing purposes, but is not recommended in production.\
Another feature is (almost) never expiring tokens, further allowing to hardcode keys for test accounts and have them be valid across computers, environments, and time.

### Development Caddy

Caddy in development mode does not redirect using the internal docker network, but rather redirects to the common `localhost` (`host.docker.internal`). This allows to shut down the front- or backend and run a local development instance in an IDE while still allowing proper flow of requests.

### Docker Compose

The Docker compose file should be moved to the parent folder that contains `/frontend` and `/backend` as it needs to address files in both folders.

---

## Services only

There is a `services_only_compose.yaml` in `backend/compose/`. This is a docker compose file which was used during development and has legacy status. This simply starts services aside from frontend and backend, and just needs to be renamed to run.

### Services

The compose file starts the following services:
- Postgres Database
- Keycloak
- Caddy (Development)

Caddy development build is required because Spring and Next are not run in Docker

### Docker Compose

The Docker compose file can just be renamed to `compose.yaml` and run, it must not be moved. Of course, it is important that container names don't clash, but since it does not require access to the front- or backends' dockerfiles, it does not need to be in the top parent folder.
