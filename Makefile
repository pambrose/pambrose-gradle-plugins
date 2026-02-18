default: versioncheck

stop:
	./gradlew --stop

clean:
	./gradlew clean

build: clean
	./gradlew jar buildFatJar

tree:
	./gradlew -q dependencies

depends:
	./gradlew dependencies

versioncheck:
	./gradlew dependencyUpdates

refresh:
	./gradlew --refresh-dependencies

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.2.0 --distribution-type=bin
