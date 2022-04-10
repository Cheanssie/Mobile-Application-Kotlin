package com.lamont.assignment.adapter

import android.content.Context
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

class QuizAdapter(val context: Context,var quizQues: List<QuizMenu>) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    private lateinit var itemListener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClicked(position: Int)
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

        init {
            //initialize button for selected quiz
            joinBtn.setOnClickListener{
                listener.onItemClicked(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.quiz_item, parent, false)

        return QuizViewHolder(adapterLayout, itemListener)
    }

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