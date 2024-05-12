 package com.example.memorygame

import android.animation.ArgbEvaluator
import android.content.Context
import android.icu.text.CaseMap.Title
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryCard
import com.example.memorygame.models.MemoryGame
import com.example.memorygame.utils.DEFAULT_ICONS
import com.google.android.material.snackbar.Snackbar

 class MainActivity : AppCompatActivity() {
    //code added by me
     companion object{
         private const val TAG ="MainActivity"
     }


     private lateinit var clRoot : ConstraintLayout
     private lateinit var rvBoard: RecyclerView
     private lateinit var tvNumMoves: TextView
     private lateinit var tvNumPairs: TextView

     private lateinit var memoryGame: MemoryGame
     private lateinit var adapter:MemoryBoardAdapter
     private var boardSize: BoardSize = BoardSize.EASY

     //code already added
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //code added by me
         clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

         setUpBoard()

        //code already added
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

     override fun onCreateOptionsMenu(menu: Menu?): Boolean {
         menuInflater.inflate(R.menu.menu_main,menu)
         return true
     }

     override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when(item.itemId){
             R.id.mi_refresh -> {
                 if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                     showAlertDialog("Quit Your Current Game",null,View.OnClickListener{
                         setUpBoard()
                     })
                 }else{
                     setUpBoard()
                 }
             }
         }
         return super.onOptionsItemSelected(item)
     }

     private fun showAlertDialog(title: String,view: View?, positiveClickListener: View.OnClickListener) {
         AlertDialog.Builder(this)
             .setTitle(title)
             .setView(view)
             .setNegativeButton("CANCEL",null)
             .setPositiveButton("OK"){_,_->
                 positiveClickListener.onClick(null)
             }.show()
     }

     private fun setUpBoard() {
         tvNumPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
         memoryGame = MemoryGame(boardSize)

         adapter=MemoryBoardAdapter(this,boardSize,memoryGame.cards,object : MemoryBoardAdapter.CardClickListener{
             override fun onCardClicked(position: Int) {
                 updateGameWithFlip(position)
             }
         })
         rvBoard.adapter = adapter
         rvBoard.setHasFixedSize(true)
         rvBoard.layoutManager = GridLayoutManager(this,boardSize.getWidth())
     }

     private fun updateGameWithFlip(position: Int) {
         // Error Checking
         if (memoryGame.haveWonGame()){
             //Alert the user of an invalid move
             //snackbar is used for alerting the user
             Snackbar.make(clRoot,"You Already Won",Snackbar.LENGTH_LONG).show()
             return
         }
         if (memoryGame.isCardFaceUp(position)){
             //Alert the user of an invalid move
             Snackbar.make(clRoot,"Invalid Move!",Snackbar.LENGTH_SHORT).show()
             return
         }
         //actually flip over the card
         if(memoryGame.flipCard(position)){
             Log.i(TAG,"FOUND A MATCH! NUM PAIRS FOUND: ${memoryGame.numPairsFound}")
             val color = ArgbEvaluator().evaluate(
                 memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                 ContextCompat.getColor(this,R.color.color_progress_none),
                 ContextCompat.getColor(this,R.color.color_progress_full),
                 ) as Int
             tvNumPairs.setTextColor(color)
             tvNumPairs.text="PAIRS: ${memoryGame.numPairsFound}/${boardSize.getNumPairs()}"
             if (memoryGame.haveWonGame()){
                 Snackbar.make(clRoot,"YOU WON!! CONGRATS",Snackbar.LENGTH_LONG).show()
             }
         }
         tvNumMoves.text = "MOVES : ${memoryGame.getNumMoves()}"
         adapter.notifyDataSetChanged()
     }
}