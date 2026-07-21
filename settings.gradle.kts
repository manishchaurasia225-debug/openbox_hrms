plugins {
	// Enables Gradle to auto-detect installed JDKs and auto-provision a matching
	// toolchain (Java 21) when none is found locally.
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "hrms"
