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

class FavoriteAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // お気に入り登録したShopを格納
    private val items = mutableListOf<FavoriteShop>()

    // お気に入り画面から削除するときのコールバック（お気に入り画面では追加はない、削除のみできる）
    // （ApiFragmentへ通知するメソッド）
    var onClickDeleteFavorite: ((FavoriteShop) -> Unit)? = null

    // Itemを押したときのメソッド
    var onClickItem: ((String) -> Unit)? = null

    // お気に入り画面用のViewHolderオブジェクトの生成
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            // お気に入り登録0件の場合
            VIEW_TYPE_EMPTY -> EmptyViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_favorite_empty, parent, false))
            // 上記以外（1件以上のお気に入り登録がある場合）
            else -> FavoriteItemViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_favorite, parent, false))
        }
    }

    // 更新用のメソッド
    fun refresh(list: List<FavoriteShop>) {
        items.apply {
            clear()  // itemsを空にする
            addAll(list)  // itemsにlistを全て追加
        }
        notifyDataSetChanged()  // recyclerViewを再描画
    }

    // お気に入りが1件もない場合、「お気に入りはありません」レイアウトを出すため
    override fun getItemCount(): Int {
        return if (items.isEmpty()) 1 else items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM
    }

    // viewHolderのバインド
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FavoriteItemViewHolder) {
            updateFavoriteItemViewHolder(holder, position)
        }
    }

    // ViewHolder内のUI部品に値などをセット
    private fun updateFavoriteItemViewHolder(holder: FavoriteItemViewHolder, position: Int) {
        val data = items[position]
        holder.apply {
            rootView.apply {
                // 背景色
                setBackgroundColor(ContextCompat.getColor(context, if (position % 2 == 0) android.R.color.white else android.R.color.darker_gray))

                setOnClickListener {
                    onClickItem?.invoke(data.url)
                }

            }
            nameTextView.text = data.name
            Picasso.get().load(data.imageUrl).into(imageView)  // Picassoライブラリを使用しimageViewに画像セット
            favoriteImageView.setOnClickListener {
                onClickDeleteFavorite?.invoke(data)
                notifyItemChanged(position)
            }
        }
    }

    // お気に入りが登録されているときのリスト
    class FavoriteItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val rootView: ConstraintLayout = view.findViewById(R.id.rootView)
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val favoriteImageView: ImageView = view.findViewById(R.id.favoriteImageView)
    }

    // お気に入り登録がまだ行われていないとき
    class EmptyViewHolder(view: View): RecyclerView.ViewHolder(view)

    companion object {
        // Viewの種類を表現する定数、こちらはお気に入りのお店
        private const val VIEW_TYPE_ITEM = 0
        // Viewの種類を表現する定数、こちらはお気に入りが1件もないとき
        private const val VIEW_TYPE_EMPTY = 1
    }

}