VERSION=1.0.7

default: versioncheck

stop:
	./gradlew --stop

clean:
	./gradlew clean

build: clean
	./gradlew build -x test

tests:
	./gradlew test

tree:
	./gradlew -q dependencies

depends:
	./gradlew dependencies

trigger-build:
	curl -s "https://jitpack.io/com/github/pambrose/pambrose-gradle-plugins/${VERSION}/build.log"

view-build:
	curl -s "https://jitpack.io/api/builds/com.github.pambrose/pambrose-gradle-plugins/${VERSION}" | python3 -m json.tool

versioncheck:
	./gradlew dependencyUpdates --no-configuration-cache

refresh:
	./gradlew --refresh-dependencies

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.2.0 --distribution-type=bin
