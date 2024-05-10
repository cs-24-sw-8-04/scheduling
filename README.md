# Scheduling

[![codecov](https://codecov.io/gh/cs-24-sw-8-04/scheduling/graph/badge.svg?token=ODKWSLYAO5)](https://codecov.io/gh/cs-24-sw-8-04/scheduling)

This repository contains the complete scheduling system and a simulator to test the system with many users and devices.
The system consists of a Android frontend, written in Kotlin with Jetpack Compose as the GUI toolkit, and a CRUD REST API backend written in Rust with the Axum framework.

The purpose of the system is to schedule devices to run at times where they can make the most use of available renewable energy.
The user registers their devices in the app and creates tasks for them.
A task is a timespan of where the device can run and a duration of how long the device runs for.
The task is then scheduled by the backend by creating an event that specifies the specific time the device should run.
To do this an algorithm called the global scheduler is used to ensure optimal scheduling by taking into account tasks from all users.


# Frontend

The frontend allows the user to login, manage smart devices, and create tasks.
A task is a time range that a device must be scheduled to run.
The backend then schedules the task by using the global scheduler.
The user is notified when their task has been scheduled, informing the user of the time the device will turn on.


## Building

Open the `frontend/` project in Android Studio.
Here the app can be built.


# Backend

The backend is a CRUD REST API written in Rust with the Axum framework.
It keeps track of accounts, authentication tokens, devices, tasks, and events in a sqlite database.
The database schema is defined in the `migrations/` folder.
The backend has the following endpoints:

These endpoints return an authentication token on success.
An authentication token is associated to an account.
- `accounts/register` register an account
- `accounts/login` login to an account

An authentication token is needed to call the following endpoints.
The endpoints only operate on the account's data, and cannot see or operate on other accounts' data.
- `devices/all` get all devices
- `devices/create` create a device
- `devices/delete` delete a device
- `tasks/all` get all tasks
- `tasks/create` create a task
- `tasks/delete` delete a task
- `events/all` get all events
- `events/get` get the event associated with a task

Creating or deleting tasks signals to the backend that the scheduling algorithm needs to run.
It waits for 5 minutes to collect more task creations/deletions and to not run the algorithm too often as it is expensive.
The algorithm then runs and creates/updates events for all tasks in the system.

Many of these endpoints communicate with JSON.
This is done in rust by defining structs that specify the schema of the JSON input/output.
These structs are put into a seperate project (`protocol`) so they can be reused in the simulator.


## Building

We use sqlx to communicate with the sqlite database.
This provides compile time checked sql statements and type bindings to and from the database.
It does however, require additional setup for the program to build.

First install the sqlx-cli tool:
```bash
cargo install sqlx-cli --no-default-features --features sqlite
```

Then create a .env file in the backend directory that contains the database file location:
```
DATABASE_URL=sqlite://dev.db
```

The database file can then be created and its schema setup with the following commands:
```bash
cargo sqlx db create
cargo sqlx migrate run
```

The backend can now be built:
```bash
cargo build
```

