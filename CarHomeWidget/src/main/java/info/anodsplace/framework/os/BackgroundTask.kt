package info.anodsplace.framework.os

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import info.anodsplace.framework.app.ApplicationContext

/**
 * @author Alex Gavrishev
 * *
 * @date 14/04/2017.
 */
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
