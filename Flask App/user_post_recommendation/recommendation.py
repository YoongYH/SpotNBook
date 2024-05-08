from flask import Flask, Blueprint, request, jsonify

# Firebase Dependencies
import firebase_admin
from firebase_admin import credentials, firestore

# Initialize Firebase
cred = credentials.Certificate("spotnbook-b562b-firebase-adminsdk-aqrm1-e242bf6727.json")
firebase_admin.initialize_app(cred)

# Retrieve Firestore Instances
db = firestore.client()

def recommend_posts_for_user(user_id, n):
    # Retrieve user data from Firestore
    user_ref = db.collection("users").document(user_id)
    user_data = user_ref.get().to_dict()

    # Gather user preferences
    user_preferences = [user_data["gender"]["value"]] + user_data["following"]

    # Retrieve posts data from Firestore
    posts_ref = db.collection("posts").stream()
    posts_data = [doc.to_dict() for doc in posts_ref]

    # Initialize a dictionary to store post similarities
    post_similarities = {}

    # Calculate similarity between each post and user preferences
    for post_data in posts_data:
        # Check if the user has liked the post
        if user_id in post_data.get("likedBy", []):
            # If the user has liked the post, add it as a feature
            user_preferences.append(post_data["postId"])

        # Extract relevant features from the post
        post_features = [post_data["postCaption"], post_data["postBody"], post_data["taggedMerchant"]] + post_data["attachments"]

        # Calculate similarity using Jaccard similarity
        similarity_score = calculate_jaccard_similarity(user_preferences, post_features)
        post_similarities[post_data["postId"]] = similarity_score

    # Sort posts by similarity score in descending order
    sorted_post_ids = sorted(post_similarities, key=post_similarities.get, reverse=True)
    
    # Retrieve top-N recommended posts
    recommended_posts = []
    for post_id in sorted_post_ids[:n]:
        post_ref = db.collection("posts").document(post_id)
        post_data = post_ref.get().to_dict()
        recommended_posts.append(post_data)

    return recommended_posts

def calculate_jaccard_similarity(list1, list2):
    intersection = len(set(list1).intersection(list2))
    union = len(set(list1).union(list2))
    return intersection / union

recommendation_bp = Blueprint('recommendation_bp', __name__)
@recommendation_bp.route('/recommendation', methods=['POST'])
def recommendation(uid):
    result = recommend_posts_for_user(uid)
    return result

test_user_id = "sqHsmBbNqRgJhL5PQqxawh4Rlbx1"
num_recommendations = 3
recommended_posts = recommend_posts_for_user(test_user_id, num_recommendations)

print("Recommended Posts for User:")
for post in recommended_posts:
    print("Post ID:", post["postId"])
    print("Post Caption:", post["postCaption"])
    print()
    
test_user_id_2 = "325ngncOuNfOnZVSOykYuEfu6gz2"
num_recommendations = 3
recommended_posts_2 = recommend_posts_for_user(test_user_id_2, num_recommendations)

print("Recommended Posts for User:")
for post in recommended_posts_2:
    print("Post ID:", post["postId"])
    print("Post Caption:", post["postCaption"])
    print()