# Circleci configuration

This is a circleci file configuration with version 2.0 with Android configuration and is created with a workflow to create parallel runs for test and deploy task.

#### Jobs

A run is comprised of one or more named jobs. Jobs are specified in the jobs map, the name of the job is the key in the map, and the value is a map describing the job.

#### Build

Each job consists of the job’s name as a key and a map as a value. A name should be unique within a current jobs.

#### Steps

A list of steps to be performed

#### More information

- [Circle CI. Language Guide: Android](https://circleci.com/docs/2.0/language-android)
- [Circle CI. 2.0 Docs](https://circleci.com/docs/2.0/)
