# 📚 Books‑API Automated Test Suite

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Installation](#installation)
4. [Runtime Configuration](#runtime-configuration)
5. [Running Tests Locally](#running-tests-locally)
6. [Viewing Test Reports](#viewing-test-reports)
7. [Running in GitHub Actions](#running-in-github-actions)
8. [Test Coverage & Known Bugs](#test-coverage--known-bugs)
9. [License](#license)

---

## Overview

End‑to‑end automated tests for a sample **Books REST API**.

| Layer / Concern | Technology                                     |
|-----------------|------------------------------------------------|
| Test runner     | Cucumber 7 + TestNG 7.9                        |
| HTTP client     | Rest‑Assured 5.5                               |
| Assertions      | AssertJ 3.27                                   |
| Reports         | Allure 2.29                                    |
| Logging         | SLF4J / Logback                                |
| Retry control   | `QuorumRetryAnalyzer` (≤ 5 attempts, pass ≥ 3) |
| Contract check  | JSON‑Schema (Jackson)                          |
| Service layer   | `api.service.BookService` (hides raw HTTP)     |

---

## Prerequisites

| Tool             | Min Version | Purpose                 |
|------------------|-------------|-------------------------|
| **JDK**          | 17          | compile & run tests     |
| **Apache Maven** | 3.9         | build / dependencies    |
| **Git**          | latest      | clone repository        |
| **Allure CLI**   | 2.29.x      | view HTML reports (opt) |

---

## Installation

```bash
git clone https://github.com/ZeroTull/books-api.git
cd books-api-tests
# Dependencies download automatically on first Maven run
```

---

## Runtime Configuration

The suite checks **system properties → environment variables → `config.properties`**.

| Property       | Overrides (← highest)                    | Example                  |
|----------------|------------------------------------------|--------------------------|
| `api.baseUrl`  | `-Dapi.baseUrl` • `API_BASEURL` • file   | `https://api.my-host/v1` |
| `api.username` | `-Dapi.username` • `API_USERNAME` • file | `user04`                 |
| `api.password` | `-Dapi.password` • `API_PASSWORD` • file | `s3cr3t`                 |
| `api.timeout`  | `-Dapi.timeout`  • `API_TIMEOUT`  • file | `15` (seconds)           |

```bash
mvn test -Dapi.baseUrl=http://localhost:8080/api/v1 -Dapi.username=dev -Dapi.password=devpwd
```

---

## Running Tests Locally

| Task                 | Command                                                                      |
|----------------------|------------------------------------------------------------------------------|
| **Run all tests**    | `mvn clean test`                                                             |
| Run by **tag**       | `mvn test -Dcucumber.filter.tags="@happy"`                                   |
| Run a **feature**    | `mvn test -Dcucumber.features=src/test/resources/features/put-books.feature` |
| Extra **debug** logs | `mvn test -Dlog.level=DEBUG`                                                 |

### Buckets

| Bucket        | Command                                    |
|---------------|--------------------------------------------|
| Smoke         | `mvn test -Dcucumber.filter.tags="@smoke"` |
| Happy paths   | `mvn test -Dcucumber.filter.tags="@happy"` |
| Negative only | `mvn test -Dcucumber.filter.tags="@neg"`   |
| Known defects | `mvn test -Dcucumber.filter.tags="@bug"`   |

> `retry.count=N` — max flake retries (default 5)

---

## Viewing Test Reports

| View            | How                                                                |
|-----------------|--------------------------------------------------------------------|
| **Console**     | Rest‑Assured & Logback print request / response + Cucumber summary |
| **Allure HTML** | `mvn allure:report` → `mvn allure:serve` (auto‑opens browser)      |

---

## Running in GitHub Actions

See `.github/workflows/api-tests.yaml` (excerpt):

```yaml
env:
  API_BASEURL: ${{ secrets.API_BASEURL }}
  API_USERNAME: ${{ secrets.API_USERNAME }}
  API_PASSWORD: ${{ secrets.API_PASSWORD }}

steps:
  - uses: actions/checkout@v3
  - uses: actions/setup-java@v3
    with:
      distribution: temurin
      java-version: 17
  - name: Run tests & build Allure report
    run: |
      mvn -B clean test -Dapi.baseUrl=$API_BASEURL -Dapi.username=$API_USERNAME -Dapi.password=$API_PASSWORD
      mvn -B allure:report
  - uses: actions/upload-artifact@v4
    with:
      name: allure-report
      path: target/site/allure-maven-plugin
```

Add three repository **secrets** so credentials never appear in code.

---

## Test Coverage & Known Bugs

* CRUD happy paths, negatives & edge‑cases, contract checks, retries.
* Known open defects documented in [`defects.md`](defects.md) (tagged`@bug`) – run but are **expected to fail** until
  the API is fixed.

---

## License

MIT – feel free to reuse this project as a template.
