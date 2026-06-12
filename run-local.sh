#!/bin/bash
podman build -t dorm-portal-backend:local-test .
podman run --env-file .env -p 8080:8080 dorm-portal-backend:local-test
