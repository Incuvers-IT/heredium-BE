@echo off

echo Resetting remote branch . . .
call git fetch
call git reset --hard origin/jenkins
call git rebase origin/dev

echo Start building Heredium backend . . .
call graldew spotlessApply
call gradlew clean
call gradlew bootJar

call docker stop heredium-backend-test
call docker rm heredium-backend-test
call docker build -t heredium-backend-test .
call docker run -t -d --network host --name heredium-backend-test --restart=always heredium-backend-test
call docker image prune -a -f
