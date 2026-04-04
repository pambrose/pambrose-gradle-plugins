VERSION=$(shell grep '^version =' build.gradle.kts | head -1 | sed 's/.*"\(.*\)"/\1/')

default: versioncheck

stop:
	./gradlew --stop

clean:
	./gradlew clean

build: clean
	./gradlew build -xtest

tests:
	./gradlew test

tree:
	./gradlew -q dependencies

depends:
	./gradlew dependencies

versioncheck:
	./gradlew dependencyUpdates --no-configuration-cache

refresh:
	./gradlew --refresh-dependencies

publish-local:
	./gradlew publishToMavenLocal

publish-local-snapshot:
	./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenLocal

GPG_ENV = \
	ORG_GRADLE_PROJECT_signingInMemoryKey="$$(gpg --armor --export-secret-keys $$GPG_SIGNING_KEY_ID)" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)

publish-snapshot:
	$(GPG_ENV) ./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenCentral

publish-maven-central:
	$(GPG_ENV) ./gradlew publishAndReleaseToMavenCentral

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.4.1 --distribution-type=bin
