import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.repository.ProfileRepository
import com.fyp.spotnbook.repository.UserType
import com.fyp.spotnbook.views.profile.ViewProfileActivity
import com.google.android.material.imageview.ShapeableImageView

class SearchUserResultAdapter(private val context: Context, private var dataList: MutableList<String>) :
    RecyclerView.Adapter<SearchUserResultAdapter.ViewHolder>() {
    private var profileRepository = ProfileRepository()

    // ViewHolder class to hold the views for each item in the RecyclerView
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: ShapeableImageView = itemView.findViewById(R.id.profileImage)
        var authorizedIcon: ImageView = itemView.findViewById(R.id.authorizedIcon)
        var user_username: TextView = itemView.findViewById(R.id.user_username)
        var user_uid: TextView = itemView.findViewById(R.id.user_uid)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_users, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uid = dataList[position]
        displayData(uid, holder)
    }

    private fun displayData(uid: String, holder: ViewHolder){
        profileRepository.isUserOrMerchant(uid) { userType ->
            when (userType) {
                UserType.USER -> {
                    profileRepository.getUserData(uid) { result ->
                        if (result.isSuccess) {
                            val userData = result.userData!!

                            Glide.with(context)
                                .load(userData.imageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.anonymous)
                                .error(R.drawable.anonymous)
                                .into(holder.profileImage)
                            holder.authorizedIcon.visibility = View.GONE
                            holder.user_username.text = userData.username
                            holder.user_uid.setText("UID: ${userData.userID}")

                            holder.itemView.setOnClickListener {
                                val intent = Intent(context, ViewProfileActivity::class.java)
                                intent.putExtra("uid", userData.userID)
                                context.startActivity(intent)
                            }
                        }
                    }
                }
                UserType.MERCHANT -> {
                    profileRepository.getMerchantData(uid) { result ->
                        if (result.isSuccess) {
                            val merchantData = result.merchantData!!

                            Glide.with(context)
                                .load(merchantData.profileImageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.anonymous)
                                .error(R.drawable.anonymous)
                                .into(holder.profileImage)
                            holder.authorizedIcon.visibility = if (merchantData.authorizedMerchant) View.VISIBLE else View.GONE
                            holder.user_username.text = merchantData.merchantName
                            holder.user_uid.text = "UID: ${merchantData.merchantID}"

                            holder.itemView.setOnClickListener {
                                val intent = Intent(context, ViewProfileActivity::class.java)
                                intent.putExtra("uid", merchantData.merchantID)
                                context.startActivity(intent)
                            }
                        }
                    }
                }
                UserType.NEITHER -> {
                    // Neither logic
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<String>) {
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged()
    }
}