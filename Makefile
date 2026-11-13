.PHONY: default help stop clean build tests tree refresh kdocs versions versions-all \
        publish-local publish-local-snapshot publish-snapshot publish-maven-central upgrade-wrapper zizmor ci \
        _check-gpg-env _check-maven-central-env _require-version _require-gradle-version

VERSION := $(shell sed -n 's/^version=\(.*\)/\1/p' gradle.properties)
GRADLE_VERSION := $(shell sed -n 's/^gradle-wrapper = "\(.*\)"/\1/p' gradle/libs.versions.toml)

GPG_ENV = \
	ORG_GRADLE_PROJECT_signingInMemoryKey="$$(gpg --armor --export-secret-keys $$GPG_SIGNING_KEY_ID)" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyId="$$GPG_SIGNING_KEY_ID" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)

# Maven Central's credentials, a user token's name and password from central.sonatype.com, from the macOS keychain, as
# the signing key's passphrase is: the maven-central-username and maven-central-password entries (account
# maven-central), shared by every repository that publishes this way. They reach only the publishing commands, as the
# vanniktech plugin's mavenCentralUsername and mavenCentralPassword, never ~/.gradle/gradle.properties, which every
# Gradle build on the machine is given. Add them once with
#   security add-generic-password -a maven-central -s maven-central-username -U -w
#   security add-generic-password -a maven-central -s maven-central-password -U -w
# each of which asks for the value, so it stays out of the shell's history.
MAVEN_CENTRAL_ENV = \
	ORG_GRADLE_PROJECT_mavenCentralUsername="$$(security find-generic-password -a "maven-central" -s "maven-central-username" -w)" \
	ORG_GRADLE_PROJECT_mavenCentralPassword="$$(security find-generic-password -a "maven-central" -s "maven-central-password" -w)"

default: help

help: ## Show this menu of available targets
	@awk 'BEGIN {FS = ":.*?## "; printf "Available targets:\n"} \
		/^[a-zA-Z][a-zA-Z0-9_-]*:.*?## / {printf "  \033[36m%-24s\033[0m %s\n", $$1, $$2}' \
		$(MAKEFILE_LIST)

stop: ## Stop the Gradle daemon
	./gradlew --stop

clean: ## Remove build outputs
	./gradlew clean

# Forces a full rebuild and skips tests; run `make tests` separately.
build: clean ## Clean and build (skips tests)
	./gradlew build -x test

tests: ## Run the test suite
	./gradlew test

tree: ## Print the dependency tree
	./gradlew -q dependencies

refresh: ## Refresh dependencies from remote repositories
	./gradlew --refresh-dependencies

kdocs: ## Generate KDoc HTML documentation
	./gradlew dokkaGeneratePublicationHtml

# build.gradle.kts gives dependencyUpdates a formatter that lists only what is behind; versions-all names the plugin's
# own plain report, which also lists everything up to date.
versions: ## List the dependencies with newer versions, and any that could not be checked
	./gradlew dependencyUpdates --no-configuration-cache

versions-all: ## The plugin's full dependency report, up-to-date dependencies included
	./gradlew dependencyUpdates --no-configuration-cache --output-formatter=plain

publish-local: _require-version ## Publish to the local Maven repository
	./gradlew publishToMavenLocal

publish-local-snapshot: _require-version ## Publish a -SNAPSHOT to the local Maven repository
	./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenLocal

publish-snapshot: _require-version _check-gpg-env _check-maven-central-env ## Publish a -SNAPSHOT to Maven Central
	$(GPG_ENV) $(MAVEN_CENTRAL_ENV) ./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenCentral

publish-maven-central: _require-version _check-gpg-env _check-maven-central-env ## Publish and release to Maven Central
	$(GPG_ENV) $(MAVEN_CENTRAL_ENV) ./gradlew publishAndReleaseToMavenCentral

# Gradle's documented upgrade procedure: the first run rewrites
# gradle-wrapper.properties using the *old* wrapper jar; the second run
# regenerates the wrapper itself with the new version.
upgrade-wrapper: _require-gradle-version ## Upgrade Gradle wrapper to version in libs.versions.toml
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin

# Uses online audits when GH_TOKEN is set; otherwise runs offline.
zizmor: ## Audit GitHub Actions workflows and Dependabot config with zizmor
	zizmor .

# Mirrors the GitHub Actions workflows (zizmor.yml, tests.yml, and dokka.yml's build job), starting from a
# clean build directory as a fresh checkout does. zizmor runs its online audits, as in CI, when GH_TOKEN is
# set or gh is logged in; an empty GH_TOKEN makes zizmor fail, so it is only exported when non-empty.
ci: ## Run the GitHub Actions checks locally (zizmor, tests, Dokka build)
	@if [ -z "$$GH_TOKEN" ] && token=$$(gh auth token 2>/dev/null); then export GH_TOKEN="$$token"; fi; \
		$(MAKE) --no-print-directory zizmor
	./gradlew clean test dokkaGeneratePublicationHtml

_check-gpg-env:
	@if [ -z "$$GPG_SIGNING_KEY_ID" ]; then \
		echo "ERROR: GPG_SIGNING_KEY_ID is not set" >&2; exit 1; \
	fi
	@if ! gpg --list-secret-keys "$$GPG_SIGNING_KEY_ID" >/dev/null 2>&1; then \
		echo "ERROR: no GPG secret key found for GPG_SIGNING_KEY_ID=$$GPG_SIGNING_KEY_ID" >&2; exit 1; \
	fi
	@if ! security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w >/dev/null 2>&1; then \
		echo "ERROR: keychain entry 'gradle-signing-password' (account 'gpg-signing') not found" >&2; exit 1; \
	fi

# Fails, naming the keychain entry and never its value, when either of Maven Central's credentials is missing or empty
_check-maven-central-env:
	@for entry in maven-central-username maven-central-password; do \
		if [ -z "$$(security find-generic-password -a 'maven-central' -s "$$entry" -w 2>/dev/null)" ]; then \
			echo "ERROR: keychain entry '$$entry' (account 'maven-central') is missing or empty; add it with: security add-generic-password -a maven-central -s $$entry -U -w" >&2; \
			exit 1; \
		fi; \
	done

_require-version:
	@[ -n "$(VERSION)" ] || { echo "ERROR: Could not determine project version from gradle.properties" >&2; exit 1; }

_require-gradle-version:
	@[ -n "$(GRADLE_VERSION)" ] || { echo "ERROR: Could not determine gradle version from gradle/libs.versions.toml" >&2; exit 1; }
