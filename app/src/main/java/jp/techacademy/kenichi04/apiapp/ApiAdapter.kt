package jp.techacademy.kenichi04.apiapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ApiAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 取得したjsonデータを解析し、Shop型オブジェクトとして生成したものを格納するリスト
    private val items = mutableListOf<Shop>()

    // 一覧画面から登録する時のコールバック（FavoriteFragmentへ通知するメソッド）
    var onClickAddFavorite: ((Shop) -> Unit)? = null
    // 一覧画面から削除する時のコールバック（ApiFragmentへ通知するメソッド）
    var onClickDeleteFavorite: ((Shop) -> Unit)? = null

    // Itemを押した時のメソッド
    var onClickItem: ((String) -> Unit)? = null

    // 表示リスト更新時に呼び出すメソッド
//    fun refresh(list: List<Shop>) {
//        items.apply {
//            clear()   // itemsを空にする
//            addAll(list)   // itemsにlistを全て追加する
//        }
//        notifyDataSetChanged()   // recycleViewを再描画させる
//    }

    fun refresh(list: List<Shop>) {
        update(list, false)
    }

    fun add(list: List<Shop>) {
        update(list, true)
    }

    // 表示リスト更新時に呼び出すメソッド
    fun update(list: List<Shop>, isAdd: Boolean) {
        items.apply {
            if (!isAdd) {   // 追加の時はlistを空にしない
                clear()
            }
            addAll(list)  // itemsにlistを全て追加
        }
        notifyDataSetChanged()   // recycleViewを再描画
    }

    // RecycleViewで表示させる1件分のデータを作成
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewHolderを継承したApiItemViewHolderオブジェクトを生成し返す
        // LayoutInflarter.from(context)でlayoutInflaterオブジェクト生成？
        return ApiItemViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_favorite, parent, false))
    }

    // ViewHolderを継承したApiItemViewHolderクラスの定義（RecyclerViewで表示させる1つ1つのセルのView）
    class ApiItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // レイアウトファイルからidがrootViewのConstraintLayoutオブジェクトを取得し代入
        val rootView: ConstraintLayout = view.findViewById(R.id.rootView)
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val favoriteImageView: ImageView = view.findViewById(R.id.favoriteImageView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // 第一引数にonCreateViewHolderで作られたViewHolder、第二引数に何番目の表示か渡される
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ApiItemViewHolder) {
            // 生成されたViewHolderがApiItemViewHolderの場合
            updateApiItemViewHolder(holder, position)
        } //else {
           // 別のViewHolderをバインドさせることが可能となる
        //}
    }

    private fun updateApiItemViewHolder(holder: ApiItemViewHolder, position: Int) {
        // 生成されたViewHolderの位置を指定し、オブジェクトを代入
        val data = items[position]

        // お気に入り状態を取得
        val isFavorite = FavoriteShop.findBy(data.id) != null  // idでfavoriteShop取得できたら（!=null）true

        holder.apply {
            rootView.apply {
                // 偶数番目と奇数番目で背景色を変更させる
                setBackgroundColor(ContextCompat.getColor(context,
                    if (position % 2 == 0) android.R.color.white else android.R.color.darker_gray))

                setOnClickListener {
                    onClickItem?.invoke(
                        if (data.couponUrls.sp.isNotEmpty()) data.couponUrls.sp else data.couponUrls.pc
                    )
                }
            }
            // nameTextViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
            nameTextView.text = data.name
            //Picassoライブラリを使い、imageViewにdata.logoImageのurlの画像を読み込ませる
            Picasso.get().load(data.logoImage).into(imageView)
            // 白抜きの星マーク画像を指定
            favoriteImageView.apply {
                setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)
                setOnClickListener {
                    if (isFavorite) {
                        onClickDeleteFavorite?.invoke(data)
                    } else {
                        onClickAddFavorite?.invoke(data)
                    }
                    notifyItemChanged(position)
                }
            }


        }
    }

}