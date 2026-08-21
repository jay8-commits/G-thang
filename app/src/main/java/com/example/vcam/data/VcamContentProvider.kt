package com.example.vcam.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class VcamContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        context?.let { VcamConfigManager.init(it) }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf("config_json"))
        val ctx = context
        val config = if (ctx != null) {
            VcamConfigManager.loadConfig(ctx)
        } else {
            VcamConfigManager.getConfig()
        }
        cursor.addRow(arrayOf(config.toJson()))
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.item/vnd.com.example.vcam.config"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val json = values?.getAsString("config_json")
        if (!json.isNullOrBlank()) {
            val config = com.example.vcam.model.VcamConfig.fromJson(json)
            context?.let { VcamConfigManager.saveConfig(it, config) }
            return 1
        }
        return 0
    }
}
