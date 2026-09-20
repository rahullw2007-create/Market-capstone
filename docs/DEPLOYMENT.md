# Rahul Mart Deployment Notes

Rahul Mart is packaged as a Java 17 WAR application for Tomcat 9.

## Container deployment

The project includes:

- `Dockerfile` — Maven build stage followed by Tomcat 9 runtime.
- `.dockerignore` — excludes local build output and editor files.
- `docker/start-rahulmart.sh` — maps the cloud-provided `PORT` to Tomcat.

The WAR is installed as `ROOT.war`, so the deployed site is served from `/`.
The shared frontend API helper detects the current context automatically, so the same source works locally at `/zenith-bazaar/` and in a ROOT deployment.

## Database note

The project uses H2 file storage by default. This is suitable for demonstrations and local deployment. A cloud host may use ephemeral storage, so long-term database persistence should be configured separately when required.

## Local build

```text
mvn clean package
```

The resulting WAR remains `target/zenith-bazaar.war`.
