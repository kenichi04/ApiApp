package jp.techacademy.kenichi04.apiapp

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_api.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class ApiFragment: Fragment() {

    private val apiAdapter by lazy { ApiAdapter(requireContext()) }
    // 描画をMainスレッドで行い、API通信の処理は別スレッドで行う必要がある
    private val handler = Handler(Looper.getMainLooper())

    // Fragment->ActivityにFavoriteの変更を通知する
    private var fragmentCallback: FragmentCallback? = null

    // 最初のページを0とする
    private var page = 0
    // Apiでデータ読み込み中のフラグ。スクロールでの連続読み込みを防ぐため、制御のため（true時は追加読み込みしない）
    private var isLoading = false

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
        // fragment_api.xmlが反映されたViewを作成してreturn
        return inflater.inflate(R.layout.fragment_api, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここから初期化処理を行う
        // ApiAdapterのお気に入り追加、削除用のメソッドの追加を行う
        apiAdapter.apply {
            // Adapterの処理をそのままActivityに通知する
            onClickAddFavorite = {
                fragmentCallback?.onAddFavorite(it)
            }
            onClickDeleteFavorite = {
                fragmentCallback?.onDeleteFavorite(it.id)
            }

            // Itemをクリックしたとき
            onClickItem = {
                fragmentCallback?.onClickItem(it)
            }
        }

        // RecyclerViewの初期化
        recyclerView.apply {
            adapter = apiAdapter
            layoutManager = LinearLayoutManager(requireContext())  // 一列ずつ表示

            // Scrollを検知するListener実装。RecyclerViewの下端に近づいた時に次のページを読み込んで、下に付け足す
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                // dx: x軸方向の変化量(横)、dy: y軸方向の変化量(縦). RecyclerViewは縦方向なので、dyだけ考慮
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy == 0) {  // 縦方向の変化量（スクロール量）0の時は動いていないため何もしない
                        return
                    }
                    val totalCount = apiAdapter.itemCount  // RecyclerViewの現在の表示アイテム数
                    // RecyclerViewの現在見えている最後のViewHolderのposition
                    val lastVisibleItem = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    // totalCountとlastVisibleItemから全体のアイテム数のうちどこまでが見えているかがわかる(例:totalCountが20、lastVisibleItemが15の時は、現在のスクロール位置から下に5件見えていないアイテムがある)
                    // 一番下にスクロールした時に次の20件を表示する等の実装が可能になる。
                    // ユーザビリティを考えると、一番下にスクロールしてから追加した場合、一度スクロールが止まるので、ユーザーは気付きにくい
                    // ここでは、一番下から5番目を表示した時に追加読み込みする様に実装する
                    if (!isLoading && lastVisibleItem >= totalCount - 6) {
                        // 読み込み中ではなく、かつ、現在のスクロール位置から下に5件見えていないアイテムがある
                        updateData(true)
                    }
                }
            })
        }
        swipeRefreshLayout.setOnRefreshListener {
            updateData()
        }
        updateData()
    }

    // お気に入りが削除された時の処理（Activityからコールされる）
    fun updateView() {
        recyclerView.adapter?.notifyDataSetChanged()  // RecyclerViewのAdapterに対して再描画のリクエスト
    }

    private fun updateData(isAdd: Boolean = false) {
        if (isLoading) {
            return
        } else {
            isLoading = true
        }
        if (isAdd) {
            page ++
        } else {
            page = 0
        }
        // page:0 は１件目から、page:1 は21件目から取得、page:2 は...
        val start = page * COUNT + 1

        val url = StringBuilder()
            .append(getString(R.string.base_uri))   // https://webservice.recruit.co.jp/hotpepper/gourmet/v1/
            .append("?key=").append(getString(R.string.api_key))  // Apiを使うためのApiKey
            .append("&start=").append(start)  // 何件目からデータを取得するか
            .append("&count=").append(COUNT)  // 1回で20件取得する
            .append("&keyword=").append(getString(R.string.api_keyword))  // 検索ワード
            .append("&format=json")  // ここで利用しているAPIは戻り値をxmlかjsonで選択できる
            .toString()
        // Http通信を行う本体
        val client = OkHttpClient.Builder()
             // ログに通信の詳細を出す事ができる
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        val request = Request.Builder()
            .url(url)
            .build()
        // ここで実際にHttp通信を行う/ enqueueの引数にコールバック設定
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {  // Error時の処理
                e.printStackTrace()
                handler.post {
                    // 失敗時には空のリストで更新する
                    updateRecyclerView(listOf(), isAdd)
                }
                isLoading = false  // 読み込み中フラグを折る
            }

            override fun onResponse(call: Call, response: Response) {  // 成功時の処理
                var list = listOf<Shop>()
                response.body?.string()?.also {
                    // JsonデータからApiResponseへの変換
                    val apiResponse = Gson().fromJson(it, ApiResponse::class.java)
                    list = apiResponse.results.shop
                }
                handler.post {
                    updateRecyclerView(list, isAdd)
                }
                isLoading = false  // 読み込み中フラグを折る
            }
        })

    }

    private fun updateRecyclerView(list: List<Shop>, isAdd: Boolean) {
        if (isAdd) {
            apiAdapter.add(list)
        } else {
            apiAdapter.refresh(list)
        }

        swipeRefreshLayout.isRefreshing = false  // SwopeRefreshLayoutのくるくるを消す
    }

    companion object {
        // 1回のAPIで取得する件数
        private const val COUNT = 20
    }
}