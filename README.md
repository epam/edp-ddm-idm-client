# ddm-idm-client

### Overview

Project with clients for identity providers such as keycloak.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-idm-client</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```
2. Define property ```keycloak.url``` with value of Keycloak server host
3. Enable Feign clients `@EnableFeignClients` in case of usage `PublicIdmService`
4. IdmServiceFactory is available for  service creation with following configuration.

```java
@Configuration
@Import(IdmClientServiceConfig.class)
public class IdmConfig {

  @Autowired
  public IdmServiceFactory idmServiceFactory;

  @Bean
  @ConditionalOnProperty(prefix = "keycloak.officer", value = "realm")
  @ConfigurationProperties(prefix = "keycloak.officer")
  public KeycloakClientProperties officerRealmProperties() {
    return new KeycloakClientProperties();
  }

  @Bean("officer-keycloak-client-service")
  @ConditionalOnBean(name = "officerRealmProperties")
  public IdmService officerIdmService(KeycloakClientProperties officerRealmProperties) {
    return idmServiceFactory.createIdmService(officerRealmProperties.getRealm(),
            officerRealmProperties.getClientId(),
            officerRealmProperties.getClientSecret());
  }
}
```

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-idm-client is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).