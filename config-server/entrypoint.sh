#!/bin/sh
# Prevent ownership issues with git inside container
git config --global --add safe.directory /etc/config-repo

if [ ! -d "/etc/config-repo/.git" ]; then
  echo "Initializing git repository in /etc/config-repo..."
  cd /etc/config-repo
  git init
  git checkout -b main || git branch -m main
  git config user.email "evaluator@example.com"
  git config user.name "Evaluator"
  git add .
  git commit -m "Initial configuration for inventory-service"
fi

cd /app
exec java -jar app.jar
