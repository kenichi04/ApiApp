package jp.techacademy.kenichi04.apiapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_favorite.*

class FavoriteFragment: Fragment() {
    private val favoriteAdapter by lazy { FavoriteAdapter(requireContext()) }

    // FavoriteFragment -> MainActivityに削除を通知する
    private var fragmentCallback: FragmentCallback? = null

    // FragmentがActivityにアタッチされた時に呼び出される
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentCallback) {
            fragmentCallback = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_favorite.xmlが反映されたViewを生成してreturn
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここから初期化処理
        // FavoriteAdapterのお気に入り削除用のメソッドの追加を行う
        favoriteAdapter.apply {
            // Adapterの処理をそのままActivityに通知
            onClickDeleteFavorite = {
                fragmentCallback?.onDeleteFavorite(it.id)
            }
            // Itemをクリックした時
            onClickItem = {
//                fragmentCallback?.onClickItem(it)
                fragmentCallback?.onClickFavoriteShop(it)
            }
        }
        // RecyclerViewの初期化
        val itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        recyclerView.apply {
            adapter = favoriteAdapter
            layoutManager = LinearLayoutManager(requireContext())  // 一列ずつ表示
            addItemDecoration(itemDecoration)
        }
        swipeRefreshLayout.setOnRefreshListener {
            updateData()
        }
//        updateData()
    }

    // webViewActivityから戻った時にも更新
    override fun onStart() {
        super.onStart()
        updateData()
    }

//    override fun onResume() {
//        super.onResume()
//        updateData()
//    }

    fun updateData() {
        favoriteAdapter.refresh(FavoriteShop.findAll())
        swipeRefreshLayout.isRefreshing = false
    }

}