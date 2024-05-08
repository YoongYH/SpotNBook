package com.fyp.spotnbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

import androidx.viewpager2.widget.ViewPager2


/*
class ImageSliderAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false) // Use your item layout
        return ImageSliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageSliderViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        holder.bind(imageUrl)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ImageSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageUrl: String) {
            Glide.with(itemView)
                .load(imageUrl)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }
}
*/
// Adapter for populating images in ViewPager2
class ImageSliderAdapter(private val imageUrls: List<String>, private val viewPager2: ViewPager2) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ImageSliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageSliderViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        holder.bind(imageUrl)

    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ImageSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageUrl: String) {
            Glide.with(itemView)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.anonymous)
                .error(R.drawable.anonymous)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)


            // Set scale type to fit the image within the ImageView bounds
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }

/*    fun PagerSnapHelper.attachToViewPager2(viewPager2: ViewPager2) {
        // Convert ViewPager2 to RecyclerView
        val recyclerView = viewPager2.getChildAt(0) as RecyclerView
        attachToRecyclerView(recyclerView)
    }

    // Function to set up the dot indicators
    fun setUpIndicators() {
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(viewPager2)
        indicator.setSnapHelper(snapHelper)
        indicator.adapter = RecyclerAdapter().apply { setData(imageUrls) }
        indicator.orientation = LinearLayout.HORIZONTAL
        indicator.addItemDecoration(LinePagerIndicatorDecoration())
    }*/
}

