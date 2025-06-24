#!/bin/bash

echo "Running tests..."
mvn clean test

echo "Generating Allure report..."
allure generate target/allure-results --clean -o target/allure-report

echo "Opening Allure report..."
allure open target/allure-report