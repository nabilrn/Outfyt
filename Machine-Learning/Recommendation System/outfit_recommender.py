import os
import pandas as pd
import numpy as np
import joblib
import re
from sklearn.preprocessing import LabelEncoder, MinMaxScaler
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer
from collections import defaultdict

class LikableOutfitRecommender:
    def __init__(self, csv_path='styles_new.csv', sep=';', model_path='outfit_recommender_model.joblib'):
        self.skin_color_matches = {
            'Summer': [
                'Navy Blue', 'Pink', 'Lavender',
                'Purple', 'Blue', 'Teal',
                'White', 'Grey', 'Sea Green',
                'Turquoise Blue', 'Green'
            ],
            'Fall': [
                'Burgundy', 'Brown', 'Olive',
                'Maroon', 'Bronze', 'Copper',
                'Gold', 'Red', 'Orange'
            ],
            'Winter': [
                'Black', 'White', 'Grey',
                'Navy Blue', 'Red', 'Purple',
                'Burgundy', 'Silver', 'Teal'
            ],
            'Spring': [
                'Green', 'Lime Green', 'Yellow',
                'Pink', 'Light Blue', 'Turquoise',
                'Orange', 'Maroon'
            ]
        }

        self.subcategory_likes = defaultdict(lambda: defaultdict(lambda: defaultdict(int)))
        self.user_recommendations = defaultdict(lambda: defaultdict(list))
        self.user_likes = defaultdict(lambda: defaultdict(list))

        self.model_path = model_path

        # Check if a pre-trained model exists
        if os.path.exists(model_path):
            self.load_model()
        else:
            # Initialize from scratch and train
            self.data = None
            self.label_encoders = {}
            self.tfidf = TfidfVectorizer(stop_words='english')
            self.scaler = MinMaxScaler()

            # Initialize recommender with data
            df = pd.read_csv(csv_path, sep=sep)
            df.dropna(inplace=True)

            # Ensure correct data types
            df['productDisplayName'] = df['productDisplayName'].astype(str)
            df['id'] = df['id'].astype(str)
            df['gender'] = df['gender'].astype(str)
            df['baseColour'] = df['baseColour'].astype(str)
            df['usage'] = df['usage'].astype(str)
            df['season'] = df['season'].astype(str)

            # Fit the recommender with the loaded data
            self.fit(df)

            # Save the trained model
            self.save_model()

    def save_model(self):
        """
        Save the trained model components to a file
        """
        model_components = {
            'data': self.data,
            'label_encoders': self.label_encoders,
            'tfidf': self.tfidf,
            'scaler': self.scaler,
            'feature_matrix': self.feature_matrix,
            'similarity_matrix': self.similarity_matrix
        }

        joblib.dump(model_components, self.model_path)
        print(f"Model saved to {self.model_path}")

    def load_model(self):
        """
        Load pre-trained model components from a file
        """
        model_components = joblib.load(self.model_path)

        self.data = model_components['data']
        self.label_encoders = model_components['label_encoders']
        self.tfidf = model_components['tfidf']
        self.scaler = model_components['scaler']
        self.feature_matrix = model_components['feature_matrix']
        self.similarity_matrix = model_components['similarity_matrix']

        print(f"Model loaded from {self.model_path}")

    def fit(self, data, feature_weights=None):
        self.data = data.copy().reset_index(drop=True)

        categorical_features = ['gender', 'masterCategory', 'subCategory', 'articleType',
                               'baseColour', 'season', 'usage']

        if feature_weights is None:
            feature_weights = {f: 1.0 for f in categorical_features}

        encoded_features_list = []
        for feature in categorical_features:
            if feature in self.data.columns:
                self.label_encoders[feature] = LabelEncoder()
                encoded_feature = self.label_encoders[feature].fit_transform(self.data[feature])
                encoded_features_list.append(encoded_feature.reshape(-1, 1))

        encoded_features = np.hstack(encoded_features_list)
        encoded_features_scaled = self.scaler.fit_transform(encoded_features) * [feature_weights[f] for f in categorical_features]

        self.data['clean_name'] = self.data['productDisplayName'].apply(self.preprocess_text)
        text_features = self.tfidf.fit_transform(self.data['clean_name'])

        self.feature_matrix = np.hstack((encoded_features_scaled, text_features.toarray()))

        self.similarity_matrix = cosine_similarity(self.feature_matrix)

        return self

    def preprocess_text(self, text):
        text = str(text).lower()
        text = re.sub(r'[^a-zA-Z0-9\s]', '', text)
        return text

    def _get_input_key(self, skin_tone, gender, usage, season):
        return f"{skin_tone}_{gender}_{usage}_{season}"

    def add_like(self, user_id, product_name, subcategory, skin_tone, gender, usage, season, rating=1):
        input_key = self._get_input_key(skin_tone, gender, usage, season)

        product = self.data[self.data['productDisplayName'] == product_name]
        if len(product) == 0:
            print(f"Product not found: {product_name}")
            return

        item_id = product.iloc[0]['id']
        self.subcategory_likes[subcategory][input_key][item_id] += rating
        self.user_likes[user_id][subcategory].append(item_id)

    def get_recommendations_by_subcategory(self, user_id, skin_tone, gender, usage, season, subcategory, n=4):
        input_key = self._get_input_key(skin_tone, gender, usage, season)
        recommended_colors = self.skin_color_matches.get(skin_tone, [])

        mask = (
            (self.data['subCategory'] == subcategory) &
            (self.data['gender'] == gender) &
            (self.data['usage'] == usage) &
            (self.data['season'] == season) &
            (self.data['baseColour'].isin(recommended_colors))
        )

        filtered_data = self.data[mask].reset_index(drop=True)

        if len(filtered_data) == 0:
            return pd.DataFrame()

        mask_indices = np.where(mask)[0]
        filtered_similarities = self.similarity_matrix[mask_indices][:, mask_indices]
        base_scores = filtered_similarities.mean(axis=0)

        like_scores = np.zeros(len(filtered_data))

        for i, (_, row) in enumerate(filtered_data.iterrows()):
            likes = self.subcategory_likes[subcategory][input_key][row['id']]
            like_scores[i] = likes

        if like_scores.max() > 0:
            like_scores = like_scores / like_scores.max()

        n = min(n, len(filtered_data))
        top_indices = np.argsort(base_scores)[::-1][:n]

        item_sims = filtered_similarities[top_indices][:,top_indices]
        diversity_penalty = item_sims.mean(axis=1)

        final_scores = 0.6 * base_scores[top_indices] + 0.3 * like_scores[top_indices] - 0.1 * diversity_penalty + 0.1 * np.random.random(n)

        top_indices = top_indices[np.argsort(final_scores)[::-1]]

        recommendations = filtered_data.iloc[top_indices][[
            'id', 'productDisplayName', 'gender', 'baseColour', 'usage',
            'season', 'masterCategory', 'subCategory'
        ]].copy()
        recommendations['score'] = final_scores

        self.user_recommendations[user_id][subcategory].extend(recommendations['id'].tolist())

        return recommendations

    def get_complete_outfit(self, user_id, skin_tone, gender, usage, season):
        outfit_recommendations = {
            'Recommended Colors': self.skin_color_matches[skin_tone],
            'Outfit': {},
            'Input': {
                'skin_tone': skin_tone,
                'gender': gender,
                'usage': usage,
                'season': season
            }
        }

        subcategories = ['Accessories', 'Headwear', 'Bottomwear', 'Topwear', 'Flip Flops', 'Sandal', 'Shoes', 'Sports Equipment']

        for subcat in subcategories:
            recommendations = self.get_recommendations_by_subcategory(
                user_id, skin_tone, gender, usage, season, subcat
            )
            if not recommendations.empty:
                outfit_recommendations['Outfit'][subcat] = recommendations.to_dict('records')

        return outfit_recommendations