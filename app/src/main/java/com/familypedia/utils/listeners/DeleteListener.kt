package com.familypedia.utils.listeners

import com.familypedia.network.CharacterData


interface DeleteListener {
    fun onEditClick(character : CharacterData)
    fun onDeleteClick(character: CharacterData)
    fun onReportClick(character: CharacterData)
}