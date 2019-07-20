package com.anod.car.home.prefs.lookandfeel

import android.app.Application
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
import androidx.lifecycle.*
import com.anod.car.home.R
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.prefs.LookAndFeelActivity
import com.anod.car.home.prefs.drag.ShortcutShadowBuilder
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SkinPreviewViewModel(application: Application): AndroidViewModel(application) {
    val view = MutableLiveData<View>()
    lateinit var builder: WidgetViewBuilder
    var overrideSkin: String? = null

    fun load() {
        viewModelScope.launch {
            val preview = renderPreview(builder, overrideSkin)
            view.value = preview
        }
    }

    private suspend fun renderPreview(builder: WidgetViewBuilder, overrideSkin: String?): View = withContext(Dispatchers.Default) {
        builder.init()
        builder.overrideSkin = overrideSkin
        val rv = builder.build()
        try {
            return@withContext rv.apply(getApplication(), null)
        } catch (e: InflateException) {
            AppLog.e("Cannot generate preview for $overrideSkin", e)
            return@withContext TextView(getApplication()).apply {
                text = "Cannot generate preview"
            }
        }
    }
}

class SkinPreviewFragment : Fragment(), View.OnLongClickListener {

    private var position: Int = 0

    private var lookAndFeelActivity: LookAndFeelActivity? = null

    private var viewGroup: ViewGroup? = null

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

    override fun onAttach(context: Context) {
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
