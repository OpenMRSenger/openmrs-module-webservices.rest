# syntax=docker/dockerfile:1

# ---- Stage 1: build the OpenMRS module (.omod) from source ----
ARG OPENMRS_CORE_TAG=nightly

FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /src

# Copy poms first to leverage Docker layer cache for dependency resolution
COPY pom.xml ./
COPY omod-common/pom.xml omod-common/
COPY omod/pom.xml omod/
COPY integration-tests/pom.xml integration-tests/
RUN mvn -B -q dependency:go-offline || true

# Copy sources and build the module (skip tests for image builds)
COPY . .
RUN mvn -B -DskipTests clean install

# ---- Stage 2: bake the built module onto the OpenMRS core image ----
FROM openmrs/openmrs-core:${OPENMRS_CORE_TAG}

# Drop the freshly built omod into the distribution modules dir.
# The core image loads every *.omod found here on startup.
COPY --from=build /src/omod/target/*.omod /openmrs/distribution/openmrs_modules/
