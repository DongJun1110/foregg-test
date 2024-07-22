#!/bin/bash

ROOT_PATH="/home/ubuntu/spring-github-action"
JAR="$ROOT_PATH/application.jar"

APP_LOG="$ROOT_PATH/application.log"
ERROR_LOG="$ROOT_PATH/error.log"
START_LOG="$ROOT_PATH/start.log"

NOW=$(date +%c)

echo "[$NOW] $JAR 복사" >> $START_LOG
cp $ROOT_PATH/build/libs/spring-github-action-1.0.0.jar $JAR

# 환경 변수 설정
export DATASOURCE=${{ secrets.datasource }}
export USERNAME=${{ secrets.username }}
export PASSWORD=${{ secrets.password }}
export JWT_SECRET=${{ secrets.jwt.secret }}
export JWT_REFRESH_SECRET=${{ secrets.jwt.secret.refresh }}
export REDIS_HOST=${{ secrets.REDIS_HOST }}
export REDIS_PORT=${{ secrets.REDIS_PORT }}
export REDIS_PASSWORD=${{ secrets.REDIS_PASSWORD }}

echo "[$NOW] > $JAR 실행" >> $START_LOG

# Spring Boot 애플리케이션 실행 시 환경 변수를 전달합니다.
nohup java -jar $JAR --spring.datasource.url=$DATASOURCE --spring.datasource.username=$USERNAME --spring.datasource.password=$PASSWORD --spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver --spring.jpa.hibernate.ddl-auto=update --spring.jpa.properties.hibernate.show_sql=true --spring.jpa.properties.hibernate.format_sql=true --spring.jpa.properties.hibernate.default_batch_fetch_size=1000 --spring.jwt.header=Authorization --spring.jwt.secret=$JWT_SECRET --spring.jwt.token-validity-in-seconds=3600 --spring.jwt.refresh-secret=$JWT_REFRESH_SECRET --spring.data.redis.port=$REDIS_PORT --spring.data.redis.host=$REDIS_HOST --spring.data.redis.password=$REDIS_PASSWORD --server.port=8080 > $APP_LOG 2> $ERROR_LOG &

SERVICE_PID=$(pgrep -f $JAR)
echo "[$NOW] > 서비스 PID: $SERVICE_PID" >> $START_LOG
