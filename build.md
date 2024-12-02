# 手動Build手順

``` sh
./gradlew bootBuildImage --imageName=ablankz/nova-team-service:1.0.2
docker push ablankz/nova-team-service:1.0.2
```