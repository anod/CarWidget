package com.anod.car.home.prefs.lookandfeel

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
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class SkinPreviewFragment : Fragment(), LoaderManager.LoaderCallbacks<View>, View.OnLongClickListener {

    private var position: Int = 0

    private var lookAndFeelActivity: LookAndFeelActivity? = null

    internal var viewGroup: ViewGroup? = null

    private var shortcutsCount: Int = 0

    override fun onResume() {
        super.onResume()
        loaderManager.initLoader(0, null, this).forceLoad()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lookAndFeelActivity!!.onPreviewStart(position)
        loaderManager.initLoader(0, null, this)
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

    fun refresh() {
        loaderManager.initLoader(0, null, this).forceLoad()
    }

    class ViewLoader(private val activity: LookAndFeelActivity, private val position: Int) : AsyncTaskLoader<View>(activity) {

        override fun loadInBackground(): View? {
            val builder = activity.createBuilder()
            builder.init()

            builder.overrideSkin = activity.getSkinItem(position).value
            val rv = builder.build()

            return rv.apply(activity.applicationContext, null)
        }

    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<View> {
        return ViewLoader(lookAndFeelActivity!!, position)
    }

    override fun onLoadFinished(loader: Loader<View>, inflatedView: View) {
        lookAndFeelActivity!!.onPreviewCreated(position)

        if (inflatedView.parent != null) {
            //            View parent = (View) inflatedView.getParent();
            //            viewGroup.addView(parent);
            return
        }

        if (viewGroup!!.childCount > 0) {
            viewGroup!!.removeAllViews()
        }

        val model = WidgetShortcutsModel.init(activity!!,
                lookAndFeelActivity!!.appWidgetId)
        shortcutsCount = model.count

        setupDragNDrop(inflatedView, model)

        viewGroup!!.addView(inflatedView)
        //        viewGroup.invalidate();
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
            val shortcut = model.getShortcut(pos)

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

    override fun onLoaderReset(loader: Loader<View>) {

    }

    companion object {

        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): SkinPreviewFragment {
            val f = SkinPreviewFragment()

            val args = Bundle()
            args.putInt(ARG_POSITION, position)

            f.arguments = args

            return f
        }
    }

}
