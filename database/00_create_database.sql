-- Run once in MySQL Workbench using an administrator connection.
-- Phase 2 stores demonstration data only; do not connect to a live merchant database.
CREATE DATABASE IF NOT EXISTS mham_auto
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- Use a dedicated DB account, NOT root, for the Java application.
-- Create that account in Workbench with privileges limited to mham_auto.*.
-- Do not put account passwords in this file or the project ZIP.
