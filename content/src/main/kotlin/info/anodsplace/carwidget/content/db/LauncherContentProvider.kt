package info.anodsplace.carwidget.content.db

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import info.anodsplace.carwidget.content.BuildConfig

open class LauncherContentProvider : ContentProvider() {

    private var openHelper: SQLiteOpenHelper? = null

    override fun onCreate(): Boolean {
        openHelper = DatabaseHelper(context!!)
        return true
    }

    override fun getType(uri: Uri): String {
        val args = SqlArguments(uri, null, null)
        return if (TextUtils.isEmpty(args.where)) {
            "vnd.android.cursor.dir/" + args.table
        } else {
            "vnd.android.cursor.item/" + args.table
        }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        val args = SqlArguments(uri, selection, selectionArgs)
        val qb = SQLiteQueryBuilder()
        qb.tables = args.table

        val db = openHelper!!.writableDatabase
        val result = qb.query(db, projection, args.where, args.args, null, null, sortOrder)
        result.setNotificationUri(context!!.contentResolver, uri)

        return result
    }

    override fun insert(uri: Uri, initialValues: ContentValues?): Uri? {
        val args = SqlArguments(uri)

        val db = openHelper!!.writableDatabase
        val rowId = db.insert(args.table, null, initialValues)
        return if (rowId <= 0) {
            null
        } else ContentUris.withAppendedId(uri, rowId)

    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val args = SqlArguments(uri)

        val db = openHelper!!.writableDatabase
        db.beginTransaction()
        try {
            val numValues = values.size
            for (i in 0 until numValues) {
                if (db.insert(args.table, null, values[i]) < 0) {
                    return 0
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return values.size
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val args = SqlArguments(uri, selection, selectionArgs)

        val db = openHelper!!.writableDatabase

        return db.delete(args.table, args.where, args.args)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val args = SqlArguments(uri, selection, selectionArgs)

        val db = openHelper!!.writableDatabase

        return db.update(args.table, values, args.where, args.args)
    }

    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            if (LOGD) {
                Log.d(TAG, "creating new launcher database")
            }

            db.execSQL("CREATE TABLE favorites (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "intent TEXT," +
                    "itemType INTEGER," +
                    "iconType INTEGER," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB," +
                    "isCustomIcon INTEGER" +
                    ");")
        }


        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (LOGD) {
                Log.d(TAG, "onUpgrade triggered")
            }
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL("ALTER TABLE favorites ADD COLUMN isCustomIcon INTEGER DEFAULT 0")
            }
        }

    }

    internal class SqlArguments {
        val table: String
        val where: String?
        val args: Array<String>?

        constructor(url: Uri, where: String?, args: Array<String>?) {
            if (url.pathSegments.size == 1) {
                this.table = url.pathSegments[0]
                this.where = where
                this.args = args
            } else if (url.pathSegments.size != 2) {
                throw IllegalArgumentException("Invalid URI: $url")
            } else if (!TextUtils.isEmpty(where)) {
                throw UnsupportedOperationException("WHERE clause not supported: $url")
            } else {
                this.table = url.pathSegments[0]
                this.where = "_id=" + ContentUris.parseId(url)
                this.args = null
            }
        }

        constructor(url: Uri) {
            if (url.pathSegments.size == 1) {
                table = url.pathSegments[0]
                where = null
                args = null
            } else {
                throw IllegalArgumentException("Invalid URI: $url")
            }
        }
    }

    companion object {
        private const val TAG = "LauncherProvider"
        private const val LOGD = false
        private const val DATABASE_NAME = "carhomewidget.db"
        private const val DATABASE_VERSION = 2

        val AUTHORITY_FREE = if (BuildConfig.DEBUG)
            "com.anod.car.home.free.debug.shortcutsprovider"
        else
            "com.anod.car.home.free.shortcutsprovider"

        val AUTHORITY_PRO = if (BuildConfig.DEBUG)
            "com.anod.car.home.pro.debug.shortcutsprovider"
        else
            "com.anod.car.home.pro.shortcutsprovider"

        internal const val TABLE_FAVORITES = "favorites"

        /**
         * Build a query string that will match any row where the column matches
         * anything in the values list.
         */
        internal fun buildOrWhereString(column: String, values: IntArray): String {
            val selectWhere = StringBuilder()
            for (i in values.indices.reversed()) {
                selectWhere.append(column).append('=').append(values[i])
                if (i > 0) {
                    selectWhere.append(" OR ")
                }
            }
            return selectWhere.toString()
        }
    }
}