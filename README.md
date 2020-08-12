# Mail service

[![CircleCI](https://circleci.com/gh/AMPnet/mail-service/tree/master.svg?style=svg&circle-token=d5f3c62d9ef6f4d91facdb1d8d880e077444d15f)](https://circleci.com/gh/AMPnet/mail-service/tree/master)

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

### Mail Server

Spring mail server properties that must be set:

  * `spring.mail.host`
  * `spring.mail.port`
  * `spring.mail.username`
  * `spring.mail.password`

### Mail Properties

Internal mail service application properties:

  * `com.ampnet.mailservice.mail.enabled`
  * `com.ampnet.mailservice.mail.confirmation-base-link`
  * `com.ampnet.mailservice.mail.reset-password-base-link`
  * `com.ampnet.mailservice.mail.organization-invitations-link`
  * `com.ampnet.mailservice.mail.sender`
