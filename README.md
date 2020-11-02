# Mail service

## Start

Application is running on port: `8127`. To change default port set configuration: `server.port`.

### Build

```sh
./gradlew build
```

### Run

```sh
./gradlew bootRun
```

### Test

```sh
./gradlew test
```

## Application Properties

### Mail Server Properties

Spring mail server properties that must be set:

  * `spring.mail.host`
  * `spring.mail.port`
  * `spring.mail.username`
  * `spring.mail.password`

### Mail Properties

Internal mail service application properties:

  * `com.ampnet.mailservice.mail.enabled`
  * `com.ampnet.mailservice.mail.sender`
  * `com.ampnet.mailservice.mail.base-url`
