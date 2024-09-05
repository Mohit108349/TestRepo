package com.familypedia.view.dashboard.character.adapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ItemCharacterBinding
import com.familypedia.network.CharacterData
import com.familypedia.utils.*
import com.familypedia.utils.Constants.CHARACTER_DATA
import com.familypedia.utils.Constants.CHARACTER_ID
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_ADD_NEW_POST
import com.familypedia.utils.Constants.FROM_ADD_POST
import com.familypedia.utils.Constants.FROM_CHARACTER_YOUR_CHARACTERS
import com.familypedia.utils.Constants.FROM_SEARCH_CHARACTERS
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.PERMITTED_USERS
import com.familypedia.utils.Constants.PROFILE_PIC
import com.familypedia.utils.Constants.USER_ID
import com.familypedia.utils.Constants.USER_NAME
import com.familypedia.view.dashboard.character.CharacterByUserActivity
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity
import com.familypedia.view.dashboard.character.post.AddNewPostActivity

class CharactersAdapter(
    val context: Activity,
    private val charactersList: List<CharacterData>,
    private val listener: CharactersListener,
    private val from: String,
    private val resultLauncher: ActivityResultLauncher<Intent>?,
    private val blockedState: Boolean? = null
) : RecyclerView.Adapter<CharactersAdapter.CharacterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val characterData = charactersList[position]
        holder.bind(characterData)
    }

    override fun getItemCount(): Int {
        listener.onListSizeChanged(charactersList.size)
        return charactersList.size
    }

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(characterData: CharacterData) {
            binding.tvCharacterName.setTextOnTextView(characterData.name, "")
            binding.ivCharacterImage.loadImagesWithGlide(IMAGE_URL + characterData.profile_pic, true)

            if (from == FROM_CHARACTER_YOUR_CHARACTERS) {
                binding.ivEditCharacter.showView()
            } else {
                binding.ivEditCharacter.hideView()
            }

            if (from == FROM_SEARCH_CHARACTERS) {
                binding.tvOwner.showView()
                binding.tvOwnerName.showView()
                binding.tvOwnerName.setTextOnTextView(characterData.createdBy?.name ?: "", "")
            }

            binding.itemCharacter.setSafeOnClickListener {
                println("THIS IS  :: $blockedState")
                if (blockedState == true) {
                    listener.onBlock(true)
                } else {
                    when (from) {
                        FROM_SEARCH_CHARACTERS -> {
                            val bundle = Bundle().apply {
                                putString(USER_NAME, characterData.name)
                                putString(USER_ID, characterData._id)
                                putString(PROFILE_PIC, characterData.profile_pic)
                                putSerializable(PERMITTED_USERS, characterData)
                            }
                            AboutCharacterActivity.open(context, bundle)
                        }
                        FROM_ADD_NEW_POST -> {
                            val bundle = Bundle().apply {
                                putString(FROM, FROM_ADD_POST)
                                putString(CHARACTER_ID, characterData._id)
                                putSerializable(CHARACTER_DATA, characterData)
                            }
                            AddNewPostActivity.open(context, bundle)
                        }
                        else -> {
                            val bundle = Bundle().apply {
                                putString(USER_NAME, characterData.name)
                                putString(USER_ID, characterData._id)
                                putString("created_by", characterData.userId)
                                putString(PROFILE_PIC, characterData.profile_pic)
                                putSerializable(PERMITTED_USERS, characterData)
                            }
                            if (resultLauncher != null) {
                                val intent = Intent(context, AboutCharacterActivity::class.java).apply {
                                    putExtras(bundle)
                                }
                                context.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                                resultLauncher.launch(intent)
                            } else {
                                AboutCharacterActivity.open(context, bundle)
                            }
                        }
                    }
                }
            }

            binding.ivEditCharacter.setSafeOnClickListener {
                listener.onEditCharacterClicked(characterData)
            }
        }
    }
}

interface CharactersListener {
    fun onListSizeChanged(size: Int)
    fun onEditCharacterClicked(characterData: CharacterData)
    fun onBlock(isBlocked: Boolean) {}
}
