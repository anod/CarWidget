package com.anod.car.home.prefs.lookandfeel

import android.app.Application
import com.anod.car.home.R
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.prefs.LookAndFeelActivity
import com.anod.car.home.prefs.drag.ShortcutShadowBuilder
import info.anodsplace.framework.AppLog

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.os.BackgroundTask

class SkinPreviewViewModel(application: Application): AndroidViewModel(application) {
    val view = MutableLiveData<View>()
    private val applicationContext = ApplicationContext(getApplication())
    var builder: WidgetViewBuilder? = null
    var overrideSkin: String? = null

    fun load() {
        val param = Pair(this.builder!!, this.overrideSkin)
        BackgroundTask(object : BackgroundTask.Worker<Pair<WidgetViewBuilder, String?>, View>(applicationContext, param) {
            override fun run(param: Pair<WidgetViewBuilder, String?>, context: ApplicationContext): View {
                param.first.init()
                param.first.overrideSkin = param.second
                val rv = param.first.build()
                try {
                    return rv.apply(context.actual, null)
                } catch (e: InflateException) {
                    AppLog.e("Cannot generate preview for ${param.second}", e)
                    return TextView(applicationContext.actual).apply {
                        text = "Cannot generate preview"
                    }
                }
            }

            override fun finished(result: View) {
                this@SkinPreviewViewModel.view.value = result
            }
        }).execute()
    }

}

class SkinPreviewFragment : Fragment(), View.OnLongClickListener {

    private var position: Int = 0

    private var lookAndFeelActivity: LookAndFeelActivity? = null

    internal var viewGroup: ViewGroup? = null

    private var shortcutsCount: Int = 0

    private val viewModel: SkinPreviewViewModel by lazy { ViewModelProviders.of(this).get(SkinPreviewViewModel::class.java) }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lookAndFeelActivity!!.onPreviewStart(position)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        position = arguments!!.getInt(ARG_POSITION)
        lookAndFeelActivity = context as LookAndFeelActivity?
    }

    override fun onDetach() {
        super.onDetach()
        lookAndFeelActivity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.skin_item, container, false)
        viewGroup = view.findViewById(R.id.container_preview)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.overrideSkin = lookAndFeelActivity!!.getSkinItem(position).value
        viewModel.builder = lookAndFeelActivity!!.createBuilder()
        viewModel.view.observe(this, Observer { inflatedView ->
            lookAndFeelActivity!!.onPreviewCreated(position)

            if (inflatedView.parent != null) {
                return@Observer
            }

            if (viewGroup!!.childCount > 0) {
                viewGroup!!.removeAllViews()
            }

            val model = WidgetShortcutsModel.init(activity!!, lookAndFeelActivity!!.appWidgetId)
            shortcutsCount = model.count

            setupDragNDrop(inflatedView, model)
            viewGroup!!.addView(inflatedView)
        })
    }

    fun refresh() {
        viewModel.load()
    }

    override fun onDestroyView() {
        if (viewGroup != null && viewGroup!!.childCount > 0) {
            for (pos in 0 until shortcutsCount) {
                val btnResId = WidgetViewBuilder.btnIds[pos]
                val dragButton = viewGroup!!.findViewById<View>(btnResId) as? ImageView
                if (dragButton == null) {
                    AppLog.e("Count: $shortcutsCount, pos: $pos")
                    continue
                }
                dragButton.setOnLongClickListener(null)
                dragButton.setOnDragListener(null)
            }
        }

        super.onDestroyView()
    }

    private fun setupDragNDrop(inflatedView: View, model: WidgetShortcutsModel) {

        for (pos in 0 until shortcutsCount) {
            val btnResId = WidgetViewBuilder.btnIds[pos]
            val btn = inflatedView.findViewById<View>(btnResId) as? ImageView
            if (btn == null) {
                AppLog.e("Count: $shortcutsCount, pos: $pos")
                continue
            }
            val shortcut = model.get(pos)

            initDragButton(pos, btn, shortcut != null)
        }
    }

    override fun onLongClick(dragButton: View): Boolean {
        val dragData = "" + dragButton.tag
        val data = ClipData.newPlainText(dragData, dragData)
        lookAndFeelActivity!!.onBeforeDragStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dragButton.startDragAndDrop(data, ShortcutShadowBuilder(dragButton), null, 0)
        } else {
            dragButton.startDrag(data, ShortcutShadowBuilder(dragButton), null, 0)
        }
        return true
    }


    private fun initDragButton(cellId: Int, dragButton: ImageView,
                               hasShortcut: Boolean) {
        dragButton.tag = cellId.toString()
        if (hasShortcut) {
            dragButton.setOnLongClickListener(this)
        }
        dragButton.setOnDragListener(lookAndFeelActivity!!.dragListener)
    }

    companion object {
        private const val ARG_POSITION = "position"
        fun newInstance(position: Int) = SkinPreviewFragment().apply {
            arguments = bundleOf(ARG_POSITION to position)
        }
    }

}
