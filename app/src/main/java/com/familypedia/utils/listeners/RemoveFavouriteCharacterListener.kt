package com.familypedia.utils.listeners

import com.familypedia.network.CharacterData

interface RemoveFavouriteCharacterListener {
    fun removeFavouriteCharacter(character:CharacterData)
    fun onListSizeChanged(size:Int)
}