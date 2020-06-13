package com.example.quizzicat.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.QuestionsDataRetrievalFacade
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.*
import com.example.quizzicat.R
import com.example.quizzicat.Utils.ModelArrayCallback
import com.example.quizzicat.Utils.ModelCallback
import com.google.firebase.firestore.FirebaseFirestore

class ActiveQuestionsAdapter(
    private val mainContext: Context?,
    private val firebaseFirestore: FirebaseFirestore,
    private val list: List<ActiveQuestion>): RecyclerView.Adapter<ActiveQuestionsAdapter.ActiveQuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveQuestionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ActiveQuestionViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun setAnswerData(answerText: TextView, activeAnswer: ActiveQuestionAnswer) {
        answerText.text = activeAnswer.answer_text
        if (activeAnswer.correct)
            answerText.setBackgroundResource(R.drawable.shape_rect_green)
    }

    override fun onBindViewHolder(holder: ActiveQuestionViewHolder, position: Int) {
        val activeQuestion = list[position]
        holder.bind(firebaseFirestore, mainContext!!, activeQuestion)

        holder.question_topic_icon!!.setOnClickListener {
            QuestionsDataRetrievalFacade(firebaseFirestore, mainContext)
                .getAnswersForQuestion(object: ModelArrayCallback {
                    override fun onCallback(value: List<ModelEntity>) {
                        val answers = value as ArrayList<ActiveQuestionAnswer>
                        val inflated = LayoutInflater.from(mainContext)
                        val questionAnswersView = inflated.inflate(R.layout.view_pending_question_answers, null)
                        val questionText = questionAnswersView.findViewById<TextView>(R.id.display_question_answer_text)
                        questionText!!.text = list[position].question_text
                        val firstAnswerText = questionAnswersView.findViewById<TextView>(R.id.display_first_answer_text)
                        setAnswerData(firstAnswerText, answers[0])
                        val secondAnswerText = questionAnswersView.findViewById<TextView>(R.id.display_second_answer_text)
                        setAnswerData(secondAnswerText, answers[1])
                        val thirdAnswerText = questionAnswersView.findViewById<TextView>(R.id.display_third_answer_text)
                        setAnswerData(thirdAnswerText, answers[2])
                        val fourthAnswerText = questionAnswersView.findViewById<TextView>(R.id.display_fourth_answer_text)
                        setAnswerData(fourthAnswerText, answers[3])

                        AlertDialog.Builder(mainContext)
                            .setView(questionAnswersView)
                            .setPositiveButton("Exit", null)
                            .show()
                    }
                }, activeQuestion.qid)
        }
    }

    class ActiveQuestionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_question_card, parent, false)) {

        var question_topic_icon: ImageView? = null
        private var question_text: TextView? = null
        private var question_difficulty: TextView? = null

        init {
            question_topic_icon = itemView.findViewById(R.id.view_question_topic_icon)
            question_text = itemView.findViewById(R.id.view_question_text)
            question_difficulty = itemView.findViewById(R.id.view_question_difficulty)
        }

        fun bind(firebaseFirestore: FirebaseFirestore, mainContext: Context, question: ActiveQuestion) {
            TopicsDataRetrievalFacade(firebaseFirestore, mainContext).getTopicDetails(object : ModelCallback {
                override fun onCallback(value: ModelEntity) {
                    question_text!!.text = question.question_text
                    var questionDifficultyString = ""
                    when (question.difficulty) {
                        1.toLong() -> questionDifficultyString = "Easy"
                        2.toLong() -> questionDifficultyString = "Medium"
                        3.toLong() -> questionDifficultyString = "Hard"
                    }
                    question_difficulty!!.text = questionDifficultyString
                    val topic = value as Topic
                    ImageLoadingFacade(mainContext).loadImage(topic.icon_url, question_topic_icon!!)
                }
            }, question.tid)
        }
    }
}