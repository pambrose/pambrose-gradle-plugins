.PHONY: default help stop clean build tests tree refresh kdocs versions versions-all \
        publish-local publish-local-snapshot publish-snapshot publish-maven-central release release-check \
        _confirm-release release-tag upgrade-wrapper zizmor ci \
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

# `make release` publishes only from a clean, up-to-date checkout of this branch whose GitHub CI has passed.
RELEASE_BRANCH ?= master
GITHUB_URL := https://github.com/pambrose/pambrose-gradle-plugins
# The artifact every publish releases to Maven Central, as com.pambrose:<artifact>, along with a plugin marker for each
# com.pambrose.* plugin ID. release-check refuses a VERSION whose POM is already there.
MAVEN_ARTIFACT := pambrose-gradle-plugins
MAVEN_CENTRAL_URL := https://repo1.maven.org/maven2/com/pambrose
# The files steps 2 and 3 of AGENTS.md's Releasing section bump to the new version; release-check fails if one doesn't
# mention VERSION, or still has the previous release's version where those steps bump it. Keep this list in step with
# those steps.
RELEASE_VERSION_FILES := README.md llms.txt
# The CI jobs that must have passed for the release commit, as workflow-file/job-name, separated by semicolons since
# job names can hold spaces. make release runs a clean build and the tests itself, but not the Dokka build or the
# zizmor audit, so a commit CI failed them on stops before anything is published. The workflow file tells apart jobs
# with the same name. Renaming one of these jobs, or adding a check a release should wait for, means updating this list.
RELEASE_REQUIRED_JOBS := tests.yml/Run tests;dokka.yml/build;zizmor.yml/Audit GitHub Actions

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

# Publishes nothing: it fails unless this checkout is fit to publish VERSION. The local checks run first, then the
# network ones (GitHub's CI checks for HEAD, Maven Central). A bumped file also fails if it still has the previous
# release's version (the CHANGELOG section below VERSION's) where step 2 or 3 bumps it: a plugins block's
# id("com.pambrose.*") version, the pambrose-plugins catalog entry, or the Maven coordinate. Historical mentions, such
# as "(was 1.1.9)", don't match. And every CI check passing isn't enough, since with [skip ci] or a disabled workflow a
# lone third-party check can be the whole list: each RELEASE_REQUIRED_JOBS job must also have run for HEAD and passed.
# Maven Central must answer 404 for VERSION's POM; any other answer, a failed connection included, stops the check.
release-check: _require-version _check-gpg-env _check-maven-central-env ## Fail unless this checkout is fit to publish VERSION
	@case "$(VERSION)" in *SNAPSHOT*|*-rc*|*-beta*|*-alpha*) \
		echo "ERROR: $(VERSION) is a pre-release; make release publishes final versions" >&2; exit 1;; esac
	@test -z "$$(git status --porcelain)" \
		|| { echo "ERROR: the working tree is dirty; commit or stash first" >&2; exit 1; }
	@branch=$$(git rev-parse --abbrev-ref HEAD); [ "$$branch" = "$(RELEASE_BRANCH)" ] \
		|| { echo "ERROR: releases are cut from $(RELEASE_BRANCH), not $$branch" >&2; exit 1; }
	@git fetch -q origin $(RELEASE_BRANCH) \
		&& [ "$$(git rev-parse HEAD)" = "$$(git rev-parse origin/$(RELEASE_BRANCH))" ] \
		|| { echo "ERROR: HEAD is not origin/$(RELEASE_BRANCH); pull or push first" >&2; exit 1; }
	@grep -qE '^## \[$(VERSION)\] - [0-9]{4}-[0-9]{2}-[0-9]{2}$$' CHANGELOG.md \
		|| { echo "ERROR: CHANGELOG.md has no dated '## [$(VERSION)] - YYYY-MM-DD' section (AGENTS.md, Releasing step 4)" >&2; exit 1; }
	@grep -qE '^## v$(VERSION) — [0-9]{4}-[0-9]{2}-[0-9]{2}$$' RELEASE_NOTES.md \
		|| { echo "ERROR: RELEASE_NOTES.md has no dated '## v$(VERSION) — YYYY-MM-DD' section (AGENTS.md, Releasing step 5)" >&2; exit 1; }
	@missing=$$(for f in $(RELEASE_VERSION_FILES); do grep -qF '$(VERSION)' "$$f" || echo "  $$f"; done); \
	[ -z "$$missing" ] || { printf 'ERROR: not bumped to $(VERSION) (AGENTS.md, Releasing steps 2-3):\n%s\n' "$$missing" >&2; exit 1; }
	@prev=$$(awk -v h='## [$(VERSION)]' 'f && /^## \[/ { sub(/^## \[/, ""); sub(/\].*/, ""); print; exit } index($$0, h) == 1 { f = 1 }' CHANGELOG.md); \
	[ -n "$$prev" ] || { echo "ERROR: CHANGELOG.md has no release section below [$(VERSION)]" >&2; exit 1; }; \
	grep -qxF "[$(VERSION)]: $(GITHUB_URL)/compare/$$prev...$(VERSION)" CHANGELOG.md \
		|| { echo "ERROR: CHANGELOG.md has no '[$(VERSION)]: $(GITHUB_URL)/compare/$$prev...$(VERSION)' link (AGENTS.md, Releasing step 4)" >&2; exit 1; }; \
	p=$$(printf '%s' "$$prev" | sed 's/\./\\./g'); \
	stale=$$(grep -nE "com\.pambrose\.[a-z]+\"\) version \"$$p\"|pambrose-plugins = \"$$p\"|$(MAVEN_ARTIFACT):$$p([^0-9.]|\$$)" \
		$(RELEASE_VERSION_FILES) | sed 's/^/  /'); \
	[ -z "$$stale" ] || { printf 'ERROR: still at the previous version, %s (AGENTS.md, Releasing steps 2-3):\n%s\n' "$$prev" "$$stale" >&2; exit 1; }
	@tagged=$$(git rev-parse -q --verify "refs/tags/$(VERSION)^{commit}"); \
	[ -z "$$tagged" ] || [ "$$tagged" = "$$(git rev-parse HEAD)" ] \
		|| { echo "ERROR: tag $(VERSION) points at another commit" >&2; exit 1; }
	@runs=$$(gh api "repos/{owner}/{repo}/commits/$$(git rev-parse HEAD)/check-runs" --paginate \
		--jq '.check_runs[] | "\(.status) \(.conclusion) \(.name)"') \
		|| { echo "ERROR: could not read HEAD's CI checks from GitHub; is gh logged in?" >&2; exit 1; }; \
	[ -n "$$runs" ] || { echo "ERROR: no CI checks have run for HEAD" >&2; exit 1; }; \
	failed=$$(echo "$$runs" | grep -Ev '^completed (success|skipped|neutral) ' | sed 's/^/  /'); \
	[ -z "$$failed" ] || { printf 'ERROR: CI has not passed for HEAD:\n%s\n' "$$failed" >&2; exit 1; }
	@wfruns=$$(gh api "repos/{owner}/{repo}/actions/runs?head_sha=$$(git rev-parse HEAD)&per_page=100" \
		--jq '.workflow_runs[] | "\(.id) \(.path | sub(".*/"; ""))"') \
		|| { echo "ERROR: could not read HEAD's workflow runs from GitHub; is gh logged in?" >&2; exit 1; }; \
	jobs=$$(echo "$$wfruns" | while read -r id wf; do \
			[ -n "$$id" ] || continue; \
			gh api "repos/{owner}/{repo}/actions/runs/$$id/jobs?per_page=100" \
				--jq ".jobs[] | \"$$wf/\(.name) \(.conclusion)\"" || exit 1; \
		done) \
		|| { echo "ERROR: could not read the jobs of HEAD's workflow runs from GitHub" >&2; exit 1; }; \
	missing=$$(echo '$(RELEASE_REQUIRED_JOBS)' | tr ';' '\n' | sed 's/^ *//' | while read -r job; do \
		echo "$$jobs" | grep -qFx "$$job success" || echo "  $$job"; done); \
	[ -z "$$missing" ] || { printf 'ERROR: required CI jobs have not passed for HEAD:\n%s\n' "$$missing" >&2; exit 1; }
	@pom="$(MAVEN_CENTRAL_URL)/$(MAVEN_ARTIFACT)/$(VERSION)/$(MAVEN_ARTIFACT)-$(VERSION).pom"; \
	code=$$(curl -sI -o /dev/null -w '%{http_code}' "$$pom"); \
	case "$$code" in \
		404) ;; \
		200) echo "ERROR: com.pambrose:$(MAVEN_ARTIFACT):$(VERSION) is already on Maven Central; bump the version" >&2; exit 1;; \
		*) echo "ERROR: could not check Maven Central for com.pambrose:$(MAVEN_ARTIFACT):$(VERSION) (HTTP $$code)" >&2; exit 1;; \
	esac
	@echo "Ready to release $(VERSION) from $$(git rev-parse --short HEAD)"

_confirm-release:
	@echo "About to publish $(VERSION). A Maven Central release can't be removed."
	@echo "  Maven Central: com.pambrose:$(MAVEN_ARTIFACT):$(VERSION) and the com.pambrose.* plugin markers"
	@printf 'Continue? [y/N] ' && read ans && [ "$$ans" = "y" ] || { echo "Nothing published."; exit 1; }

# Each step runs only if the one before it passed, so nothing is published until the checkout passes release-check, a
# clean build, and the test suite. Once Maven Central has the release, release-tag tags HEAD and pushes the tag, which
# the GitHub release then attaches to; creating that release is printed as the next step. The docs need no step of their
# own: dokka.yml deployed them when the release commit reached master. After a partial release, finish with the
# remaining targets: release-check refuses a version already published.
release: ## Gate, build, test, and publish VERSION to Maven Central (asks first), then tag it
	$(MAKE) release-check
	$(MAKE) build
	$(MAKE) tests
	@$(MAKE) --no-print-directory _confirm-release
	$(MAKE) publish-maven-central
	$(MAKE) release-tag
	@prev=$$(git describe --tags --abbrev=0 HEAD^ 2>/dev/null); \
	echo; \
	echo "Published $(VERSION) to Maven Central and pushed its tag."; \
	echo "Next: create the GitHub release (tag $(VERSION), title v$(VERSION)) with a summary that ends with"; \
	echo "  **Full Changelog**: $(GITHUB_URL)/compare/$$prev...$(VERSION)"; \
	echo "  gh release create $(VERSION) --verify-tag --title v$(VERSION) --notes-file <notes>"

# Tags HEAD as VERSION, as the earlier releases' lightweight tags are, and pushes the tag. make release runs it once
# Maven Central has the release; the GitHub release then attaches to it. A tag already on HEAD, from a release that
# stopped partway, is pushed again; one on another commit fails, as release-check does.
release-tag: _require-version ## Tag HEAD as VERSION and push the tag (make release runs it after publishing)
	@tagged=$$(git rev-parse -q --verify "refs/tags/$(VERSION)^{commit}"); \
	if [ -z "$$tagged" ]; then \
		git tag "$(VERSION)" || exit 1; \
	elif [ "$$tagged" != "$$(git rev-parse HEAD)" ]; then \
		echo "ERROR: tag $(VERSION) points at another commit" >&2; exit 1; \
	fi; \
	git push --quiet origin "refs/tags/$(VERSION)" \
		&& echo "Tagged $$(git rev-parse --short HEAD) as $(VERSION) and pushed the tag"

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
