@echo off
del dev.db*
cargo sqlx db create
cargo sqlx mig run


