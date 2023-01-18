package com.example.reporterapp.callback

import android.view.View

interface RecyclerViewListener  {
    fun recyclerViewListClicked(v: View, position: Int, localId: Int)
}
