package com.hypersocket.migration.exception;

/**
 * Extending to Throwable as a hack, as logic where this is thrown we already catch Exception.
 * Need to escape it, hence going a level up in hierarchy.
 */
public class MigrationProcessRealmAlreadyExistsThrowable extends Throwable{
}
