import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.databinding.ItemFavouriteCharacterBinding
import com.familypedia.network.CharacterData
import com.familypedia.utils.Constants
import com.familypedia.utils.loadImagesWithGlide
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.utils.listeners.RemoveFavouriteCharacterListener
import com.familypedia.utils.setTextOnTextView
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity

class FavouriteCharactersAdapter(
    var context: Context,
    var dataList: List<CharacterData>,
    var listener: RemoveFavouriteCharacterListener
) : RecyclerView.Adapter<FavouriteCharactersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavouriteCharacterBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        listener.onListSizeChanged(dataList.size)
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    inner class ViewHolder(private val binding: ItemFavouriteCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CharacterData) {
            try {
                if (data.profile_pic != null && data.profile_pic.isNotEmpty()) {
                    binding.ivCharacterImage.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            } catch (e: Exception) {
                // Handle exception
            }
            binding.ivCharacterImage.loadImagesWithGlide(Constants.IMAGE_URL + data.profile_pic, false)
            binding.tvCharacterName.setTextOnTextView(data.name, "")
            binding.itemFavouriteCharacter.setSafeOnClickListener {
                val bundle = Bundle().apply {
                    putString(Constants.USER_NAME, data.name)
                    putString(Constants.USER_ID, data._id)
                    putString(Constants.PROFILE_PIC, data.profile_pic)
                    putSerializable(Constants.PERMITTED_USERS, data)
                }
                AboutCharacterActivity.open(context as Activity, bundle)
            }
            binding.ivFavourite.setSafeOnClickListener {
                listener.removeFavouriteCharacter(data)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
