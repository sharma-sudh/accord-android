package com.sudh.accord.data.local

/**
 * Where a locally-held row stands relative to the server.
 *
 * SYNCED           - matches the server; safe to overwrite from a fresh fetch.
 * PENDING_CREATE   - exists only on this device, never reached the server yet.
 * PENDING_UPDATE   - exists on the server, but a local mutation (e.g. completing
 *                    a task) hasn't been pushed yet.
 * PENDING_DELETE   - marked for deletion; hidden from all reads, kept around only
 *                    so SyncWorker knows to tell the server before removing the row.
 */
enum class SyncState {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}