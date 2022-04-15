package com.lamont.assignment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.lamont.assignment.R
import com.lamont.assignment.model.QuizMenu
import com.lamont.assignment.ui.QuizFragment

class QuizAdapter(val context: QuizFragment, private var quizQues: List<QuizMenu>) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    private lateinit var itemListener: OnItemClickListener

    interface OnItemClickListener{
        fun onQuizClicked(position: Int)
        fun onLeaderClicked(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        itemListener = listener
    }

    class QuizViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){
            //initialize views
            val tvQuizTitle = itemView.findViewById<TextView>(R.id.tvQuizTitle)!!
            val joinBtn = itemView.findViewById<Button>(R.id.joinBtn)!!
            val tvQuizDuration = itemView.findViewById<TextView>(R.id.tvQuizDuration)!!
            val quizImage = itemView.findViewById<ImageView>(R.id.ivQuizImage)!!
            private val quizLeaderboardBtn = itemView.findViewById<Button>(R.id.quizLeaderboardBtn)!!

        init {
            //initialize button for selected quiz
            joinBtn.setOnClickListener{
                listener.onQuizClicked(bindingAdapterPosition)
            }
            quizLeaderboardBtn.setOnClickListener {
                listener.onLeaderClicked(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.quiz_item, parent, false)

        return QuizViewHolder(adapterLayout, itemListener)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        //binding contents to views
        val quiz : QuizMenu = quizQues[position]
        holder.tvQuizTitle.text = quiz.title
        holder.tvQuizDuration.text = "Duration : " + quiz.duration.toString() + " Minutes"
        Glide.with(context)
            .load(quiz.image)
            .into(holder.quizImage)
        holder.joinBtn.text = "Join !"
    }

    override fun getItemCount(): Int {
        return quizQues.size
    }

}