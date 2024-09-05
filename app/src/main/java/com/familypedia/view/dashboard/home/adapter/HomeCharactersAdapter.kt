package com.familypedia.view.dashboard.home.adapter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ItemHomeCharactersBinding
import com.familypedia.databinding.LayoutViewMoreBinding
import com.familypedia.network.CharacterData
import com.familypedia.utils.Constants
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.SEE_MORE
import com.familypedia.utils.loadImagesWithGlide
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.utils.setTextOnTextView
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity
import com.familypedia.view.dashboard.profile.favouriteCharacter.FavouriteCharactersActivity

class HomeCharactersAdapter(
    private val context: Context,
    private val dataList: List<CharacterData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_CHARACTER = 1
        private const val VIEW_SEE_MORE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_CHARACTER -> {
                val binding = ItemHomeCharactersBinding.inflate(LayoutInflater.from(context), parent, false)
                CharacterViewHolder(binding)
            }
            VIEW_SEE_MORE -> {
                val binding = LayoutViewMoreBinding.inflate(LayoutInflater.from(context), parent, false)
                SeeMoreViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CharacterViewHolder -> holder.bind(dataList[position])
            is SeeMoreViewHolder -> holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position]._id == SEE_MORE) {
            VIEW_SEE_MORE
        } else {
            VIEW_CHARACTER
        }
    }

    inner class CharacterViewHolder(private val binding: ItemHomeCharactersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CharacterData) {
            binding.ivCharacterProfileImage.loadImagesWithGlide(
                IMAGE_URL + data.profile_pic,
                false
            )
            binding.tvCharacterName.setTextOnTextView(data.name, "")
            binding.itemHomeCharacter.setSafeOnClickListener {
                val bundle = Bundle().apply {
                    putString(Constants.USER_NAME, data.name)
                    putString(Constants.USER_ID, data._id)
                    putString(Constants.PROFILE_PIC, data.profile_pic)
                    putSerializable(Constants.PERMITTED_USERS, data)
                }
                AboutCharacterActivity.open(context as Activity, bundle)
            }
        }
    }

    inner class SeeMoreViewHolder(private val binding: LayoutViewMoreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.itemSeemore.setSafeOnClickListener {
                FavouriteCharactersActivity.open(context as Activity)
            }
        }
    }
}
