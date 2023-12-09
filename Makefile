
build:
	mvn compile

unit-test:
	@echo "executando teestes unitários"
	@mvn test

integration-test:
	mvn test -P integration-test

system-test:
	make docker-start
	mvn test -P system-test
	make docker-stop

performance-test:
	mvn gatling:test -P performance-test

test: unit-test integration-test

package:
	mvn package

docker-build:
	docker build -t backend:dev -f ./Dockerfile .

docker-start:
	docker compose -f docker-compose.yaml up -d

docker-stop:
	docker compose -f docker-compose.yaml down