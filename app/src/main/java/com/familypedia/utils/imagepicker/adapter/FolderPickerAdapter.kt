import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.EfImagepickerItemFolderBinding
import com.familypedia.utils.imagepicker.features.imageloader.ImageLoader
import com.familypedia.utils.imagepicker.features.imageloader.ImageType
import com.familypedia.utils.imagepicker.listeners.OnFolderClickListener
import com.familypedia.utils.imagepicker.model.Folder

class FolderPickerAdapter(
    private val context: Context,
    private val imageLoader: ImageLoader,
    private val folderClickListener: OnFolderClickListener
) : RecyclerView.Adapter<FolderPickerAdapter.FolderViewHolder>() {

    private val folders: MutableList<Folder> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = EfImagepickerItemFolderBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders.getOrNull(position) ?: return

        imageLoader.loadImage(folder.images.first(), holder.binding.image, ImageType.FOLDER)

        holder.binding.apply {
            tvName.text = folder.folderName
            tvNumber.text = folder.images.size.toString()
            root.setOnClickListener { folderClickListener(folder) }
        }
    }

    fun setData(folders: List<Folder>?) {
        folders?.let {
            this.folders.clear()
            this.folders.addAll(folders)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount() = folders.size

    class FolderViewHolder(val binding: EfImagepickerItemFolderBinding) : RecyclerView.ViewHolder(binding.root)
}
