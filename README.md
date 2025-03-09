# This is an outdated file.

---

# 1. File Setup

## 1.1. Project structure
You are currently in the backend, but there is also a frontend project. In your file system, create a folder for the entire project. Place both projects in that folder, and name them "frontend" and "backend" respectively. For the EnvPopulator, the folder names do need to contain backend / frontend (case-insensitive), more on that in #2, but for the compose file they need to have these names. If you are simply copying the contents from another folder, make sure to include dotfiles (especially `.prettierrc.yml`), otherwise building the frontend image will fail when running `docker compose up` in Step 3.

There is a `compose.yaml` file in `backend/compose/production` and one in `backend/compose/development`, more on the latter in 2.2. Move the `compose.yaml` you wish to use to the parent project folder, along with the environment file required for building and running. Once created in #2, it is located in `backend/compose/production/docker/.env`. If you wish to use it, do the same for the `EnvPopulator.jar`

**Regarding Compose Files**
There is a readme in `backend/compose/` that explains all docker files and their differences. In general though, you are looking for the production docker compose.

## 1.2. File Structure
The file structure should look something like this:
```
Dashboard/
├── backend/
│   ├── src/
│   │   └── ...
│   └── ...
├── frontend/
│   ├── src/
│   │   └── ...
│   └── ...
├── compose.yaml
├── .env
└── [EnvPopulator.jar]
```

## 1.9
### Google Client-Id and Client-Secret
Follow this guide https://support.google.com/googleapi/answer/6158849?hl=en to create a new OAuth 2.0 Client and select web as the applicationtype after that you should set the following values (replace http://localhost with https://domain if you want to use one):
- authorized javascript sources: http://localhost
- authorized redirect urls:  http://localhost/realms/kit-dashboard/broker/google/endpoint and http://localhost/auth/callback

### Route-API key
- you can create a developer account here https://developer.tomtom.com/ and get a key for the routing api
### KVV-API key
- you can apply for the trias key here: https://www.kvv.de/fahrplan/fahrplaene/open-data.html


# 2. Environment Variables

There are many environment variables that need to be set, in 3-4 different files. Files, variables, and descriptions are listed in tables below, for each file there is an example file that contains the default structure for every file.

## 2.1. The EnvPopulator

There is an EnvPopulator program written in Java 17 that opens a Java Swing UI window, where you can set all relevant variables, and it will create all environment files for you.

At the top, you will notice the filepaths for the frontend and backend. If it says "Not found", make sure you are executing the program in the parent folder, and that your frontend and backend folder contain their respective names.

The usage of this program is entirely optional and not required for setup, though it is convenient. You may still want to take a look at the descriptions of the environment variables.

## 2.2. Testing with consistent private keys in Keycloak

The Dashboard offers a development environment with a docker compose file located in `backend/compose/development/`. The key change is that there is a different Keycloak build that allows you to set your own certificate.\
This brings the advantage that the keys can stay persistent and are not regenerated, which allows to generate 10-year keys for Test Accounts that only work when the test server is on, and don't need to be regenerated frequently.

### 2.2.1. Generating Keycloak Signature Key and Certificate using OpenSSL

You can generate a key and certificate using the below commands. This allows keeping keys the same across pcs and container creations. The key purpose is that tokens can be generated for testing, and re-used, even when the container is deleted and re-created, or moved to a different pc, simply by keeping the key the same.

Generating a key and 10 year self-signed certificate:

**Unix-based systems:***
```bash
openssl req -x509 -newkey rsa:2048 -keyout /dev/stdout -out /dev/stdout -days 3650 -nodes -subj "/CN=keycloak"
```

**Windows:**
```bash
openssl req -x509 -newkey rsa:2048 -keyout con -out con -days 3650 -nodes -subj "/CN=keycloak"
```

### 2.2.2. Generating Keycloak Signature Key and Certificate using Docker

If you do not have openssl available, you need not worry. The following command creates a temporary container running alpine linux, installs openssl, generates a key and certificate, and self-removes after.

```bash
docker run --rm alpine sh -c "apk add --no-cache openssl && openssl req -x509 -newkey rsa:2048 -keyout /dev/stdout -out /dev/stdout -days 3650 -nodes -subj '/CN=keycloak'"
```

### 2.2.3. Notes regarding key generation

Once generated, they stay the same, regardless of how often you delete and recreate the keycloak container. This keeps long tokens for testaccounts for E2E tests valid across computers.

### 2.2.4. <span style="background-color: #3F0000; color: #FFC2C2"> >> Important! <<</span>

**When manually entering these into the .env file, it is crucial to a) keep the line breaks as \n and b) escape them, so essentially `\\n` for each line break**

## 2.3. The Environment Files and Variables

### 2.3.1. Keycloak Environment file(s):

- `backend/compose/production/keycloak/.env`
- Example File: `backend/compose/production/keycloak/.env.example`

| Variable                | Description                                                                                                                        |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| AUTH_GOOGLE_ID          | The ID of your Google OAuth Client                                                                                                 |
| AUTH_GOOGLE_SECRET      | The Secret to your Google ID                                                                                                       |
| AUTH_KIT_ID             | The KIT Client ID                                                                                                                  |
| AUTH_KIT_SECRET         | The KIT Client Secret                                                                                                              |
| AUTH_KIT_ISSUER_LINK    | The Link of the issuer: https://oidc.scc.kit.edu/auth/realms/kit                                                                   |
| SPRING_CLIENT_ID        | The Keycloak Client ID of the Spring client.\ **Important: Should match the Client ID in the secret.properties file in Spring**    |
| SPRING_CLIENT_SECRET    | The Keycloak Client Secret of the Spring client\ **Important: Should match the Client ID in the secret.properties file in Spring** |
| KEYCLOAK_ADMIN          | The username for the admin user for the Keycloak UI on port 8081                                                                   |
| KEYCLOAK_ADMIN_PASSWORD | The password for the admin user for the Keycloak UI on port 8081                                                                   |
| REDIRECT_URI_1          | Allowed redirect URI for Keycloak, the first one in the example is for post log-in. The path should not be changed.                |
| REDIRECT_URI_2          | Allowed redirect URI for Keycloak, the second one in the example is for post log-out. The path should not be changed.              |
| WEB_ORIGIN              | Allowed web origin for Keycloak requests. Should match either your Host URL.                                                       |

### 2.3.2. Keycloak Development file for testing

There are to variants for the keycloak files and environment. The one located in `.../develoment/` contains to additional variables. This is to ensure that keycloak uses the same key and certificate to sign tokens across computers and docker container creations.

- `backend/compose/development/keycloak/.env`
- Example File: `backend/compose/development/keycloak/.env.example`

**Additional Variables**
| Variable                | Description                                                                                                                                                                                                                                                                                                 |
|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| KEYCLOAK_PRIVATE_KEY    | The private key for Keycloak, used for e.g. signing jwt tokens. Should be of format -----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY----- and contain the line breaks, **escaped**. So, written should be `\\n`, such that when it is parsed, it turns into \n internally, which is what is needed. |
| KEYCLOAK_CERTIFICATE    | The certificate generated with the key for Keycloak. Should start with -----BEGIN CERTIFICATE-----\\n...\\n-----END CERTIFICATE----- and contain the line breaks, **escaped**. So, written should be `\\n`, such that when it is parsed, it turns into \n internally, which is what is needed.              |

### 2.3.3. Spring Environment file:

- `backend/compose/production/spring/secret.properties`
- Example File: `backend/compose/development/spring/secret.properties-example`

| Variable                                 | Description                                                                                                                         |
|------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| spring.datasource.username               | Username for the postgres database login                                                                                            |
| spring.datasource.password               | Password for the postgres database login                                                                                            |
| dashboard.secrets.keycloak-client-id     | The Keycloak Client ID of the Spring client.\ **Important: Should match the Client ID in the keycloak .evn file in the backend**    |
| dashboard.secrets.keycloak-client-secret | The Keycloak Client Secret of the Spring client\ **Important: Should match the Client ID in the keycloak .evn file in the backend** |
| dashboard.secrets.kvv-secret             | The secret for the KVV api, used for fetching data for the public transport widget                                                  |
| dashboard.secrets.kvv-u-rL               | The URL for KVV data fetching                                                                                                       |
| dashboard.secrets.route-api-key          | The api key for the routing api                                                                                                     |


### 2.3.4. Nextjs / Docker Environment file:

- `backend/compose/production/docker/.env` --- Move To ---> `.env`
- Example File: `backend/compose/production/docker/.env.example`

| Variable                        | Description                                                                                                                                                                                                                                                                         |
|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SPRING_SERVER_URL               | The base url to the spring server. Only called from the server, so host.docker.internal (or docker network uri) should be fine.                                                                                                                                                     |
| NEXT_PUBLIC_GOOGLE_MAPS_API_KEY | The Google Maps JS API Key. This will be visible to users (Map is client side), so you need to restrict it in the Google console. This is only for the location picker, which makes 0-1 loads per edit page. This key needs to have the Javascript API enabled with Google to work. |

### 2.4. Custom Host

By default, everything is configured to work on `http://localhost` only. This especially means that all link builders from the backend use this host to build redirect urls. If you are using a custom host, you need to enter it in the above-mentioned environment files instead of `http://localhost`.\
In addition to that, the variable properties, which are used for link building, are located in `backend/compose/production/spring/variable.properties`, and need to be changed accordingly as well.
**Other Links:** There are links that do not reference localhost, but rather urls which correspond to docker containers. These links are for docker-network-internal redirects and must also be changed when you're switching to a domain with ***HTTPS*** because the host must match the certificates' host for those requests as well.

The above steps are automatically done by the environment populator, if you are using it. The below changes to the Caddyfile for custom hosts or HTTPS **are not**.

### 2.5. Setting up Caddy and HTTPS
By default, Caddy is configured such that the dashboard is available over `http`. This configuration is located in `backend/compose/production/caddy/Caddyfile`.

If you want to serve the dashboard over https, you must provide an external domain as host (instead of localhost everywhere). In this example we will assume the domain is: www.dashboard.de
In the configuration (`backend/compose/production/spring/variables.properties`) and `.env` files you will have to replace http://localhost with https://www.dashboard.de.
Furthermore, you should change the first line of the Caddyfile (`backend/compose/production/caddy/Caddyfile`) from `:80 {` to `www.dashboard.de {` and the `localhost` in line 16 to `www.dashboard.de` to adjust Keycloak Web Origins.

**Note:** If you are using the development compose, you need to make the same adjustments to the Caddyfile in `backend/compose/development/caddy/Caddyfile`

# 3. Launching the Server(s)

Once you have everything set up, you need only go into the parent folder from #1 in your terminal and run the following command to launch all services and servers:

```bash
docker compose up
```

It is important that no container names collide, otherwise you will need to rename or delete some containers, either in docker or in the docker compose

# 4. Giving a user administrator permissions


To setup and Admin user you need to execute the following commands on the host machine running the docker compose:

Open a bash shell inside the keycloak container:
```bash
sudo docker exec -it pse-keycloak /bin/bash
```

Use the keycloak admin CLI to login to the keycloak server:
```bash
/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --client admin-cli --user admin --password admin
```
Use the keycloak admin CLI to list the available users in the dashboard realm:
```bash
/opt/keycloak/bin/kcadm.sh get users -r kit-dashboard
```

Use the keycloak admin CLI to add the admin role to the user with the given name:
```bash
/opt/keycloak/bin/kcadm.sh add-roles -r kit-dashboard --uusername myUser --rolename admin
```
