package jp.techacademy.kenichi04.apiapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_web_view.*
import java.net.URL

class WebViewActivity : AppCompatActivity() {

    var shopId = ""
    var shopName = ""
    var shopAddress = ""
    var shopImageUrl = ""
    var shopUrl = ""
    var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        shopId = intent.getStringExtra(KEY_ID).toString()
        shopName = intent.getStringExtra(KEY_NAME).toString()
        shopAddress = intent.getStringExtra(KEY_ADDRESS).toString()
        shopImageUrl = intent.getStringExtra(KEY_IMAGE_URL).toString()
        shopUrl = intent.getStringExtra(KEY_URL).toString()
        isFavorite = FavoriteShop.findBy(shopId) != null

        webView.loadUrl(shopUrl)
    }

    // ActionBar用のレイアウトファイル読み込み
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_favorite, menu)
        if (isFavorite && menu != null) {
            val item: MenuItem = menu.findItem(R.id.menu_favorite)
            item.setIcon(R.drawable.ic_star)
        }
        return true
    }

    // ActionBarにセットしたアイテム押下時の処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("ApiApp", "ItemClick")
        Log.d("ApiApp", "id: $shopId")
        Log.d("ApiApp", "name: $shopName")
        Log.d("ApiApp", "imageUlr: $shopImageUrl")
        Log.d("ApiApp", "url :$shopUrl")

        if (item.itemId == R.id.menu_favorite) {
            val favoriteShop = FavoriteShop.findBy(shopId)
            if (favoriteShop != null) {
                // お気に入り削除処理
                showConfirmDeleteFavoriteDialog(shopId, item)

            } else {
                // お気に入り追加処理
                FavoriteShop.insert(FavoriteShop().apply {
                    id = shopId
                    name = shopName
                    address = shopAddress
                    imageUrl = shopImageUrl
                    url = shopUrl
                })
                item.setIcon(R.drawable.ic_star)
            }

        }
        return true
    }

    private fun showConfirmDeleteFavoriteDialog(id: String, item: MenuItem) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                FavoriteShop.delete(id)
                item.setIcon(R.drawable.ic_star_border)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

//    companion object {
//        private const val KEY_URL = "key_url"
//        fun start(activity: Activity, url: String) {
//            activity.startActivity(
//                Intent(activity, WebViewActivity::class.java).putExtra(KEY_URL, url)
//            )
//        }
//    }
    companion object {
        private const val KEY_ID = "key_id"
        private const val KEY_NAME = "key_name"
        private const val KEY_ADDRESS = "key_address"
        private const val KEY_IMAGE_URL = "imageUrl"
        private const val KEY_URL = "key_url"

        fun start(activity: Activity, id: String, name: String, address: String, imageUrl: String, url: String) {
            activity.startActivity(
                Intent(activity, WebViewActivity::class.java)
                    .putExtra(KEY_ID, id)
                    .putExtra(KEY_NAME, name)
                    .putExtra(KEY_ADDRESS, address)
                    .putExtra(KEY_IMAGE_URL, imageUrl)
                    .putExtra(KEY_URL, url)
            )
        }
    }
}