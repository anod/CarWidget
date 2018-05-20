package info.anodsplace.framework.os

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import android.util.LruCache
import info.anodsplace.framework.app.ApplicationContext

/**
 * @author Alex Gavrishev
 * *
 * @date 14/04/2017.
 */

class CachedBackgroundTask<P, R>(private val key: String, private val worker: BackgroundTask.Worker<P, R>, private val storage: LruCache<String, Any?>) {

    fun execute() {
        val cached = storage.get(key) as? R
        if (cached != null) {
            worker.finished(cached)
            return
        }

        BackgroundTask(object : BackgroundTask.Worker<P, R>(this.worker.context, this.worker.param) {
            override fun run(param: P, context: ApplicationContext): R {
                return worker.run(param, context)
            }

            override fun finished(result: R) {
                storage.put(key, result)
                worker.finished(result)
            }
        }).execute()
    }
}

class BackgroundTask<P, R>(private val worker: Worker<P, R>) : AsyncTask<Void, Void, R>() {

    abstract class Worker<Param, Result> protected constructor(internal val context: ApplicationContext, internal val param: Param) {

        constructor(context: Context, param: Param) : this(ApplicationContext(context), param)
        constructor(application: Application, param: Param) : this(ApplicationContext(application), param)

        abstract fun run(param: Param, context: ApplicationContext): Result
        abstract fun finished(result: Result)
    }

    override fun doInBackground(vararg params: Void): R {
        return this.worker.run(this.worker.param, this.worker.context)
    }

    override fun onPostExecute(result: R) {
        this.worker.finished(result)
    }
}
