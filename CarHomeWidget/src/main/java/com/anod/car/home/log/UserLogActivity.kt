package com.anod.car.home.log

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.prefs.model.AppTheme
import kotlinx.android.synthetic.main.activity_user_log.*


/**
 * @author Alex Gavrishev
 * @date 04/01/2018
 */
class UserLogActivity: CarWidgetActivity() {

    override val theme: AppTheme
        get() = AppTheme(AppTheme.dark)

    class UserLogViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun apply(message: Message) {
            val tv = itemView as TextView
            tv.text = "${message.timestamp} ${message.message}"
            if (message.level > Log.WARN) {
                tv.setTextColor(itemView.context.resources.getColor(android.R.color.holo_red_dark))
            } else {
                tv.setTextColor(itemView.context.resources.getColor(android.R.color.white))
            }
        }
    }

    class UserLogAdapter(private val userLogger: UserLogger, val context: Context): RecyclerView.Adapter<UserLogViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserLogViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.list_item_log, parent, false)
            return UserLogViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userLogger.messages.size
        }

        override fun onBindViewHolder(holder: UserLogViewHolder, position: Int) {
            holder.apply(userLogger.messages[position])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_log)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = UserLogAdapter(UserLogger(), this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_log, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_share) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TITLE, "CarWidget Log")
            sendIntent.putExtra(Intent.EXTRA_TEXT, UserLogger().content)
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}